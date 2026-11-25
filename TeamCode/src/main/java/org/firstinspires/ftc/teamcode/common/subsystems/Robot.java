package org.firstinspires.ftc.teamcode.common.subsystems;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * ROBOT = the whole robot in one object: wheels, launcher, intake, flower catcher.
 *
 * Every OpMode starts the same way:
 *
 *   Robot robot = new Robot(hardwareMap);   // in init() or runOpMode(), NEVER as a field
 *   ...
 *   robot.update();                         // once EVERY loop
 *
 * Then use robot.launcher, robot.intake, robot.catcher, robot.drive(...), robot.follower.
 *
 * ---------------------------------------------------------------------------
 * WHY THERE IS ONLY ONE DRIVE
 *
 * The Pedro follower drives the wheels in BOTH Auto and TeleOp:
 *   Auto    follow(robot.follower, myPath())
 *   TeleOp  robot.drive(forward, strafe, turn)
 *
 * One drive setup, one set of tuning numbers, and the robot's position on the
 * field carries from the end of Auto straight into TeleOp. Do NOT add a second
 * class that calls setPower on the drive motors while this one is running: both
 * would fight for the same four motors every loop and the robot would twitch.
 * (The old setPower drive still exists on its own in competition/BackupPlainDrive
 * for the day the odometry pod dies. It does not use Robot at all, on purpose.)
 * ---------------------------------------------------------------------------
 */
public class Robot {

    // ===================== SETTINGS =====================
    // Pedro's wheel math uses the opposite sign for strafe and turn than our old
    // TeleOp did. With both of these at -1, robot.drive() puts EXACTLY the same
    // power on all four wheels as the TeleOp that ran at the meet, so the sticks
    // feel identical. If a driver says strafe or turn is backwards, flip the one
    // they complain about to +1 (and tell the coach, because it means something
    // else changed).
    public static double STRAFE_SIGN = -1;
    public static double TURN_SIGN = -1;
    // ====================================================

    /** The wheels. Auto hands this to follow(...); TeleOp goes through drive(...). */
    public final Follower follower;

    public final Launcher launcher;
    public final Intake intake;
    public final FlowerCatcher catcher;

    public Robot(HardwareMap hardwareMap) {
        follower = Constants.create(hardwareMap);
        launcher = new Launcher(hardwareMap);
        intake = new Intake(hardwareMap);
        catcher = new FlowerCatcher(hardwareMap);
    }

    /**
     * Call once every loop, BEFORE Scheduler.execute() in Auto.
     *
     * That order is not a style choice. A follow(...) command finishes when the
     * follower reports it reached the end of its path, and the follower only works
     * that out inside follower.update(). Run the Scheduler first and it asks the
     * question before the follower has looked, which makes Auto skip straight
     * through every path in a fraction of a second with no error message at all.
     * Keeping follower.update() inside this method is what stops that happening.
     */
    public void update() {
        follower.update();
        launcher.update();
        intake.update();
    }

    /** Driver control. forward/strafe/turn are -1..1, straight off the sticks. */
    public void drive(double forward, double strafe, double turn) {
        drive(forward, strafe, turn, 1.0);
    }

    /** Driver control with a speed multiplier (0..1) for precision mode. */
    public void drive(double forward, double strafe, double turn, double speed) {
        double f = forward;
        double s = STRAFE_SIGN * strafe;
        double t = TURN_SIGN * turn;

        // Scale down only if a wheel would be asked for more than full power, THEN
        // apply the speed multiplier. Pedro would also scale down on its own, but it
        // does that after the multiplier, which quietly cancels precision mode any
        // time you push two sticks at once.
        double max = Math.max(
                Math.max(Math.abs(f - s - t), Math.abs(f + s + t)),
                Math.max(Math.abs(f + s - t), Math.abs(f - s + t)));
        if (max > 1.0) {
            f /= max;
            s /= max;
            t /= max;
        }

        follower.manual(f * speed, s * speed, t * speed);
    }

    /** Stop every mechanism and the wheels. Good for the OpMode's stop(). */
    public void stopAll() {
        launcher.off();
        launcher.update();
        intake.stop();
        intake.update();   // stop() only sets intent; update() is what cuts the power
        follower.stop();
    }

    public void addTelemetry(Telemetry telemetry) {
        launcher.addTelemetry(telemetry);
        intake.addTelemetry(telemetry);
        telemetry.addData("Pose", "x %.1f  y %.1f  heading %.1f deg",
                follower.pose().x(),
                follower.pose().y(),
                Math.toDegrees(follower.pose().heading()));
    }
}
