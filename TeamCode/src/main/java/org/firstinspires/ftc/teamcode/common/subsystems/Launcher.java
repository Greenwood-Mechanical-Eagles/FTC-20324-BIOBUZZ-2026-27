package org.firstinspires.ftc.teamcode.common.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * LAUNCHER = flywheel motor + feeder servo.
 *
 * This is the ONE place that knows how to shoot. TeleOp and Auto both use it,
 * so a shot tuned in TeleOp is the same shot Auto takes.
 * The control logic came from the TeleOp that ran at the meet; it was moved here
 * unchanged, then shot counting was added on top.
 *
 * How to use it:
 *   1. Make one:            launcher = new Launcher(hardwareMap);   (inside init / runOpMode)
 *   2. Pick a speed:        launcher.setTarget(ShotPreset.WALL);
 *   3. Fire / stop firing:  launcher.setFiring(true / false);
 *   4. EVERY loop call:     launcher.update();   (nothing happens without this!)
 *
 * The feeder only runs once the flywheel is at speed, so every shot leaves at
 * the same speed instead of the first one dribbling out.
 *
 * COUNTING SHOTS
 * Every pollen that hits the spinning wheel steals energy from it, so the RPM
 * dips for a moment and then recovers. We watch for that dip, which means the
 * launcher can tell you how many pollen actually left the robot. Auto uses this
 * to fire "3 shots" instead of "feed for 2 seconds", and it is also how you find
 * out a silent failure happened: telemetry says FIRING but Shots stays 0.
 */
@Config
public class Launcher {

    // ===================== SETTINGS =====================
    /**
     * WHICH WAY THE POLLEN LEAVE, relative to the front of the robot.
     *
     * 180 = out the BACK. Confirmed by shooting it on 2026-11-25.
     *
     * This matters for aiming and nothing else, but it matters a lot: to put a
     * shot on a target the robot has to sit at
     *
     *     heading = bearing_to_target - AIM_OFFSET_DEG
     *
     * so a rear-firing shooter must face almost exactly AWAY from the goal. Get
     * this wrong and the robot aims perfectly in the opposite direction, which
     * is a hard mistake to spot from telemetry because every number looks sane.
     * Mounted differently later? Change this, not the path headings.
     */
    public static final double AIM_OFFSET_DEG = 180;

    // Bare REV HD Hex, no gear cartridges = 28 ticks per rev.
    public static final double TICKS_PER_REV = 28;

    // Flywheel PIDF from "Flywheel PIDF Tuner" (I is always P/10, D stays 0).
    // These are the values the TeleOp ran at the meet with.
    public static double P = 1.75;
    public static double F = 16.0;

    // How close (RPM) to target counts as "at speed" / READY.
    public static double RPM_TOLERANCE = 50;

    // Once a volley starts, keep feeding unless the wheel drops this far below target.
    public static double KEEP_FIRING_DROP_RPM = 350;

    // Full power until within this many RPM of target, then exact speed control.
    // Overshooting? Make BIGGER. Spin-up too slow? Make smaller. 99999 = boost off.
    public static double BOOST_WINDOW_RPM = 400;

    // -1.0 loads pollen INTO the flywheel.
    // MEASURED ON THE ROBOT 2026-11-25: the old note claimed +1.0 fed inwards
    // and driving it proved that backwards -- the left bumper was pushing pollen
    // back out. setClearingJam() uses -FEED_POWER, so this one sign flips both
    // feeding and jam-clearing together.
    public static double FEED_POWER = -1.0;

    // Smallest speed the D-pad fine-tune can go down to.
    public static double MIN_ADJUST_RPM = 100;

    // ---- Shot counting (the RPM dip) ----
    // A pollen hitting the wheel pulls the RPM down by at least this much.
    // TUNE: run "Flywheel PIDF Tuner", hold the right trigger for a few shots and
    // read "Lowest RPM in last volley". Set this to about half of (target - lowest).
    // Too HIGH and real shots are missed; too LOW and ordinary wobble counts as a shot.
    public static double SHOT_DIP_RPM = 150;

    // The wheel has to climb back to within this much of target before the next
    // dip can count. Stops one long sag from counting as ten shots.
    public static double SHOT_RECOVER_RPM = 75;

    // SAFETY NET. If we are told to fire but the wheel never quite lands inside
    // RPM_TOLERANCE, feed anyway after this long rather than sit there doing
    // nothing for the whole match. 0 = never feed until READY (old behavior).
    // This is the difference between "a bit off speed" and "scored nothing".
    //
    // It only rescues a wheel that is CLOSE: KEEP_FIRING_DROP_RPM still applies, so
    // a wheel more than that far below target stays blocked on purpose, because
    // pushing pollen into a wheel at half speed jams the feeder and scores nothing
    // anyway. When that happens telemetry says "WHEEL TOO SLOW", which means the
    // target is set higher than this motor and battery can actually reach -- pick a
    // lower ShotPreset.
    public static double FEED_ANYWAY_AFTER_MS = 1200;
    // ====================================================

    private final DcMotorEx flywheel;
    private final CRServo feeder;

    private double targetRpm = 0;
    private double currentRpm = 0;
    private boolean boosting = false;
    private boolean firing = false;
    private boolean clearingJam = false;
    private boolean volleyActive = false;
    private boolean feeding = false;

    // Shot counting state
    private final ElapsedTime firingTimer = new ElapsedTime();
    private boolean reachedSpeedThisVolley = false;
    private boolean fedAnyway = false;
    private boolean inDip = false;
    private int shotCount = 0;
    private double deepestDipRpm = 0;

    public Launcher(HardwareMap hardwareMap) {
        flywheel = hardwareMap.get(DcMotorEx.class, "outtakeMotor");
        feeder = hardwareMap.get(CRServo.class, "outtakeServo");

        // Old code used NEGATIVE power to launch, so reverse the motor and use POSITIVE speeds.
        // Velocity control needs the encoder cable plugged in!
        flywheel.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); // coasts down
        flywheel.setVelocityPIDFCoefficients(P, P / 10.0, 0, F);
    }

    // ----------------- Commands you give it -----------------

    /** Spin up to a preset speed. The wheel keeps spinning until off(). */
    public void setTarget(ShotPreset preset) {
        targetRpm = preset.rpm;
    }

    /** Spin up to an exact RPM (0 = off). */
    public void setTargetRpm(double rpm) {
        targetRpm = Math.max(0, rpm);
    }

    /** Nudge the current speed up or down (only while the wheel is on). */
    public void adjustTargetRpm(double deltaRpm) {
        if (targetRpm > 0) {
            targetRpm = Math.max(MIN_ADJUST_RPM, targetRpm + deltaRpm);
        }
    }

    /** Stop the flywheel and the feeder. */
    public void off() {
        targetRpm = 0;
        firing = false;
        clearingJam = false;
    }

    /** true = feed pollen whenever the wheel is ready. false = stop feeding. */
    public void setFiring(boolean firing) {
        if (firing && !this.firing) {
            // A new volley is starting: restart the clock and the shot detector.
            firingTimer.reset();
            reachedSpeedThisVolley = false;
            fedAnyway = false;
            inDip = false;
            deepestDipRpm = 0;
        }
        this.firing = firing;
    }

    /** true = run feeder backwards to clear a jam (works any time). */
    public void setClearingJam(boolean clearingJam) {
        this.clearingJam = clearingJam;
    }

    // ----------------- Questions you can ask it -----------------

    public double getRpm() { return currentRpm; }

    public double getTargetRpm() { return targetRpm; }

    public boolean isOn() { return targetRpm > 0; }

    /** True when the flywheel is within RPM_TOLERANCE of its target. */
    public boolean isAtSpeed() {
        return targetRpm > 0 && Math.abs(currentRpm - targetRpm) <= RPM_TOLERANCE;
    }

    /** True while the feeder is actually pushing pollen into the flywheel. */
    public boolean isFeeding() { return feeding; }

    /** How many pollen we have seen leave the robot since the last resetShotCount(). */
    public int getShotCount() { return shotCount; }

    /** Call before a volley if you want to count only that volley's shots. */
    public void resetShotCount() { shotCount = 0; }

    /** True if the wheel ever landed inside RPM_TOLERANCE during this volley. */
    public boolean reachedSpeedThisVolley() { return reachedSpeedThisVolley; }

    /** True when the safety net kicked in: we fed without ever reaching target speed. */
    public boolean isFeedingAnyway() { return fedAnyway; }

    /** Biggest RPM dip seen in this volley. Use it to tune SHOT_DIP_RPM on the field. */
    public double getDeepestDip() { return deepestDipRpm; }

    // ----------------- Call this EVERY loop -----------------

    public void update() {
        currentRpm = flywheel.getVelocity() * 60.0 / TICKS_PER_REV;

        // BOOST: far below target, use raw full power (avoids controller wind-up/overshoot).
        boolean wantBoost = targetRpm > 0 && currentRpm < targetRpm - BOOST_WINDOW_RPM;
        if (wantBoost != boosting) {
            flywheel.setMode(wantBoost
                    ? DcMotor.RunMode.RUN_WITHOUT_ENCODER
                    : DcMotor.RunMode.RUN_USING_ENCODER);
            if (!wantBoost) {
                // fresh start for the speed controller after boosting
                flywheel.setVelocityPIDFCoefficients(P, P / 10.0, 0, F);
            }
            boosting = wantBoost;
        }

        if (boosting) {
            flywheel.setPower(1.0);
        } else if (targetRpm > 0) {
            // Control Hub holds this exact speed, so battery level doesn't change the shot.
            flywheel.setVelocity(targetRpm * TICKS_PER_REV / 60.0);
        } else {
            flywheel.setPower(0);
        }

        // A volley starts the first time the wheel is READY while firing, and lasts
        // until firing stops. FEED_ANYWAY_AFTER_MS is the safety net for the case
        // where the wheel hovers just outside RPM_TOLERANCE forever.
        if (!firing || targetRpm == 0) {
            volleyActive = false;
            reachedSpeedThisVolley = false;
            fedAnyway = false;
            inDip = false;
        } else {
            if (isAtSpeed()) {
                reachedSpeedThisVolley = true;
            } else if (!reachedSpeedThisVolley
                    && FEED_ANYWAY_AFTER_MS > 0
                    && firingTimer.milliseconds() > FEED_ANYWAY_AFTER_MS) {
                fedAnyway = true;
            }
            if (reachedSpeedThisVolley || fedAnyway) {
                volleyActive = true;
            }
        }

        boolean canFeed;
        if (volleyActive) {
            // Pause between shots while the wheel climbs back, but don't dribble
            // pollen out at half speed either.
            canFeed = currentRpm >= targetRpm - KEEP_FIRING_DROP_RPM;
        } else {
            canFeed = false; // waiting for the first READY
        }

        feeding = firing && canFeed;

        if (feeding) {
            feeder.setPower(FEED_POWER);
        } else if (clearingJam) {
            feeder.setPower(-FEED_POWER);
        } else {
            feeder.setPower(0);
        }

        countShots();
    }

    /**
     * Watch for the RPM dip a pollen makes when it hits the wheel.
     *
     * Only counts once the wheel has genuinely been at speed this volley, because
     * during spin-up the RPM is hundreds below target and every loop would look
     * like a shot. That also means a volley fired by the FEED_ANYWAY safety net
     * reports 0 shots, which is exactly the signal you want: something is wrong.
     */
    private void countShots() {
        if (!volleyActive || !reachedSpeedThisVolley) {
            inDip = false;
            return;
        }

        double dip = targetRpm - currentRpm;
        deepestDipRpm = Math.max(deepestDipRpm, dip);

        if (!inDip && dip >= SHOT_DIP_RPM) {
            inDip = true;
            shotCount++;
        } else if (inDip && dip <= SHOT_RECOVER_RPM) {
            inDip = false;
        }
    }

    public void addTelemetry(Telemetry telemetry) {
        // If the robot is doing nothing, this has to say WHY. A launcher that
        // quietly refuses to feed is the worst failure we have, because from the
        // driver's seat it looks the same as a jam, a flat battery or a bad preset.
        String state;
        if (targetRpm == 0) {
            state = "OFF";
        } else if (feeding) {
            state = fedAnyway ? "FIRING (not at speed!)" : "FIRING";
        } else if (firing && currentRpm < targetRpm - KEEP_FIRING_DROP_RPM
                && firingTimer.milliseconds() > FEED_ANYWAY_AFTER_MS) {
            state = "WHEEL TOO SLOW - not feeding, lower the preset";
        } else if (isAtSpeed()) {
            state = "READY";
        } else if (boosting) {
            state = "BOOSTING";
        } else {
            state = "SPINNING UP";
        }

        telemetry.addData("Launcher", state);
        telemetry.addData("Flywheel RPM", "%.0f / target %.0f", currentRpm, targetRpm);
        telemetry.addData("Shots", shotCount);
        telemetry.addData("Deepest dip this volley", "%.0f RPM (SHOT_DIP_RPM = %.0f)",
                deepestDipRpm, SHOT_DIP_RPM);
    }
}
