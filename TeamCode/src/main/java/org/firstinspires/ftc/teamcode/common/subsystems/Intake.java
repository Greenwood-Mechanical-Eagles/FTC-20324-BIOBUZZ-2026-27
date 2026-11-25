package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * INTAKE = the motor that pulls pollen in and pushes it up to the feeder.
 *
 *   intake.in();    pull pollen in
 *   intake.out();   spit pollen out
 *   intake.stop();
 *   intake.update();   EVERY loop -- Robot.update() already does this for you
 *
 * Those three only say what you WANT. update() is what actually drives the
 * motor, which is what lets it refuse to keep pushing into a jam. Same pattern
 * as Launcher.
 *
 * ---------------------------------------------------------------------------
 * WHY THIS WATCHES THE ENCODER
 *
 * A stuck intake used to sit there at full power until something burned or the
 * match ended, and from the driver's seat it looked identical to an empty
 * robot. Now it notices.
 *
 * STALL PROTECTION (burnout). If the motor is told to run and the encoder says
 * it is barely turning, that is a stall: full power into a locked rotor, which
 * is where motors cook. After STALL_GRACE_MS it cuts power, waits
 * STALL_COOLDOWN_MS, and tries again, counting each one. Deliberately a retry
 * rather than a latch -- a jam often frees itself once the robot moves, and an
 * intake that stays dead for the rest of the match is its own kind of failure.
 * The retry count is the thing to watch: one stall is a pollen catching, ten
 * stalls is a mechanical problem.
 *
 * This part needs NO calibration. "Is it turning at all" is true in any unit,
 * so it works correctly even before TICKS_PER_REV has been measured.
 *
 * JAM WARNING (the softer signal). Running, but well below the speed it should
 * be, means something is dragging. Needs TARGET_RPM set to mean anything.
 * ---------------------------------------------------------------------------
 */
public class Intake {

    // ===================== SETTINGS =====================
    // +1 pulls pollen IN. MEASURED ON THE ROBOT 2026-11-25: the old note in here
    // claimed -1 pulled in, and driving it proved that backwards -- left trigger
    // was spitting pollen out. This is the one place that decides intake
    // direction, so flipping this sign flips in() and out() together.
    public static double IN_SIGN = 1.0;

    /** Power used when running open loop (TARGET_RPM = 0). */
    public static double FULL_POWER = 1.0;

    /**
     * MEASURE THIS before trusting any RPM number on screen.
     *
     * It only affects the RPM *display* and TARGET_RPM -- stall protection does
     * not use it. 28 is a bare REV HD Hex with no gearbox, which is almost
     * certainly wrong for a geared intake: a 40:1 cartridge makes it 1120.
     * Run tools/Encoder Check, hold the left bumper, and read ticks/sec.
     */
    public static double TICKS_PER_REV = 28;

    /**
     * 0 = full power, open loop. This is the proven behaviour and the default.
     *
     * Set it above 0 to hold a steady speed instead. Worth knowing before you
     * do: closed loop does not mean gentler. The controller pushes HARDER as
     * load rises, up to full power, to hold the number -- so a target near free
     * speed behaves like today, and a lower target runs calmer and makes the
     * jam warning below actually meaningful.
     *
     * Measure free speed with tools/Encoder Check first, then pick something
     * under it.
     */
    public static double TARGET_RPM = 0;

    // ---- stall protection (burnout) ----
    /** Below this, in ticks/sec, counts as "not really turning". */
    public static double STALL_TICKS_PER_SEC = 20;

    /** Must look stalled this long before cutting power. Covers spin-up. */
    public static double STALL_GRACE_MS = 400;

    /** How long to stay off before trying again. */
    public static double STALL_COOLDOWN_MS = 1000;

    // ---- jam warning (needs TARGET_RPM > 0) ----
    /** Running below this fraction of target counts as struggling. */
    public static double JAM_WARN_FRACTION = 0.6;
    // ====================================================

    private enum Want { STOP, IN, OUT }

    private final DcMotorEx motor;

    private Want want = Want.STOP;
    private boolean usingVelocity;

    // Time since the motor was last turning properly. Reset whenever it IS
    // turning, so this measures how long it has been slow -- not how long since
    // we told it to run. That difference matters: a pollen catching for a moment
    // must be tolerated, while a genuine stall must still be caught.
    private final ElapsedTime sinceMoving = new ElapsedTime();
    private final ElapsedTime sinceCutOff = new ElapsedTime();
    private boolean cutOff = false;
    private int stallCount = 0;
    private double ticksPerSec = 0;

    public Intake(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotorEx.class, "intakeMotor");

        // Every motor on this robot has an encoder. Reset it so tick counts mean
        // something, then start open loop; update() switches mode if TARGET_RPM
        // gets set. getVelocity() reads the encoder in either mode, so stall
        // protection works regardless.
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        usingVelocity = false;
    }

    // ----------------- What you want it to do -----------------

    public void in() { setWant(Want.IN); }

    public void out() { setWant(Want.OUT); }

    public void stop() { setWant(Want.STOP); }

    private void setWant(Want next) {
        if (next != want) {
            // Only on a CHANGE: callers set this every loop, and restarting the
            // timer every loop would mean a stall was never held long enough to
            // notice.
            want = next;
            sinceMoving.reset();
            cutOff = false;
        }
    }

    // ----------------- Questions you can ask it -----------------

    /** Measured speed, RPM. Only as accurate as TICKS_PER_REV. */
    public double getRpm() { return ticksPerSec * 60.0 / TICKS_PER_REV; }

    /** Measured speed in ticks/sec. True regardless of TICKS_PER_REV. */
    public double getTicksPerSec() { return ticksPerSec; }

    /** True while power is cut because of a stall. */
    public boolean isStalled() { return cutOff; }

    /** How many times it has stalled. One is a pollen catching; ten is mechanical. */
    public int getStallCount() { return stallCount; }

    /** Told to run and turning, but well below target. Needs TARGET_RPM > 0. */
    public boolean isStruggling() {
        return want != Want.STOP
                && !cutOff
                && TARGET_RPM > 0
                && Math.abs(getRpm()) < TARGET_RPM * JAM_WARN_FRACTION;
    }

    public double getPower() { return motor.getPower(); }

    // ----------------- Call this EVERY loop -----------------

    public void update() {
        ticksPerSec = Math.abs(motor.getVelocity());

        if (want == Want.STOP) {
            cutOff = false;
            apply(0);
            return;
        }

        if (cutOff) {
            // Resting after a stall. Try again once the cooldown is up.
            if (sinceCutOff.milliseconds() >= STALL_COOLDOWN_MS) {
                cutOff = false;
                sinceMoving.reset();
            } else {
                apply(0);
                return;
            }
        }

        if (ticksPerSec >= STALL_TICKS_PER_SEC) {
            sinceMoving.reset();   // it's turning, so nothing is stuck
        } else if (sinceMoving.milliseconds() > STALL_GRACE_MS) {
            // Barely turning for a sustained stretch = full power into a locked
            // rotor. Cut it. The grace window covers both ordinary spin-up and a
            // pollen catching for a moment.
            cutOff = true;
            stallCount++;
            sinceCutOff.reset();
            apply(0);
            return;
        }

        apply(want == Want.IN ? IN_SIGN : -IN_SIGN);
    }

    /** direction is -1, 0 or +1. Honours TARGET_RPM if it is set. */
    private void apply(double direction) {
        boolean wantVelocity = TARGET_RPM > 0 && direction != 0;

        if (wantVelocity != usingVelocity) {
            motor.setMode(wantVelocity
                    ? DcMotor.RunMode.RUN_USING_ENCODER
                    : DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            usingVelocity = wantVelocity;
        }

        if (wantVelocity) {
            motor.setVelocity(direction * TARGET_RPM * TICKS_PER_REV / 60.0);
        } else {
            motor.setPower(direction * FULL_POWER);
        }
    }

    public void addTelemetry(Telemetry telemetry) {
        String state;
        if (cutOff) {
            state = "STALLED - power cut, retrying";
        } else if (want == Want.STOP) {
            state = "off";
        } else if (isStruggling()) {
            state = "STRUGGLING - something is dragging";
        } else {
            state = want == Want.IN ? "in" : "out";
        }

        telemetry.addData("Intake", state);
        telemetry.addData("Intake speed", "%.0f ticks/sec  (%.0f rpm)",
                ticksPerSec, getRpm());
        if (stallCount > 0) {
            telemetry.addData("Intake stalls", stallCount);
        }
    }
}
