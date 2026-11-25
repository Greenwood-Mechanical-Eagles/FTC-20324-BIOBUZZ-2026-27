package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * LAUNCHER = flywheel motor + feeder servo.
 *
 * This is the ONE place that knows how to shoot. TeleOp and Auto both use it.
 * The logic is copied from RapidFireDrive (tested), just moved into its own class.
 *
 * How to use it:
 *   1. Make one:            launcher = new Launcher(hardwareMap);   (inside init / runOpMode)
 *   2. Pick a speed:        launcher.setTarget(ShotPreset.WALL);
 *   3. Fire / stop firing:  launcher.setFiring(true / false);
 *   4. EVERY loop call:     launcher.update();   (nothing happens without this!)
 *
 * Feeder only runs when the flywheel is at speed, so shots stay consistent.
 */
public class Launcher {

    // ===================== SETTINGS =====================
    // Bare REV HD Hex, no gear cartridges = 28 ticks per rev.
    public static final double TICKS_PER_REV = 28;

    // Flywheel PIDF from "Flywheel PIDF Tuner" (I is always P/10, D stays 0).
    public static double P = 1.75;
    public static double F = 16.0;

    // How close (RPM) to target counts as "at speed" / READY.
    public static double RPM_TOLERANCE = 50;

    // Once a volley starts, keep feeding unless the wheel drops this far below target.
    public static double KEEP_FIRING_DROP_RPM = 350;

    // Full power until within this many RPM of target, then exact speed control.
    // Overshooting? Make BIGGER. Spin-up too slow? Make smaller. 99999 = boost off.
    public static double BOOST_WINDOW_RPM = 400;

    // false = feeder runs ANY time you fire (no speed check). Leave true.
    public static boolean SPEED_CHECK_ON = true;

    // +1.0 loads pollen INTO the flywheel (tested on robot).
    public static double FEED_POWER = 1.0;

    // Smallest speed the D-pad fine-tune can go down to.
    public static double MIN_ADJUST_RPM = 100;
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

        // Volley starts the first time the wheel is READY while firing,
        // and lasts until firing stops.
        if (!firing || targetRpm == 0) {
            volleyActive = false;
        } else if (isAtSpeed()) {
            volleyActive = true;
        }

        boolean canFeed;
        if (!SPEED_CHECK_ON) {
            canFeed = true;
        } else if (volleyActive) {
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
    }

    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Launcher", targetRpm == 0 ? "OFF"
                : (isAtSpeed() ? "READY" : (boosting ? "BOOSTING" : "SPINNING UP")));
        telemetry.addData("Flywheel RPM", "%.0f / target %.0f", currentRpm, targetRpm);
    }
}
