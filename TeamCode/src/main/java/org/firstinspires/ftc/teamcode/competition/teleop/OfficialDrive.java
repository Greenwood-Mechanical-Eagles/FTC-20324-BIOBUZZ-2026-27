package org.firstinspires.ftc.teamcode.competition.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.subsystems.Robot;
import org.firstinspires.ftc.teamcode.common.subsystems.ShotPreset;

/**
 * OFFICIAL DRIVE: the TeleOp we drive in matches.
 *
 * This is the "Rapid Fire Drive" that ran at the meet, rebuilt on top of Robot so
 * that Auto and TeleOp shoot with the same code. The controls, the speeds and the
 * wheel powers are all unchanged.
 *
 * WHAT IS GENUINELY DIFFERENT FROM THE VERSION THAT RAN AT THE MEET
 *   - The wheels go through the Pedro follower instead of setPower. The four wheel
 *     powers work out identical (see Robot.drive), but Pedro needs the odometry pod
 *     to start up. If the pod is unplugged or broken this OpMode will not run at
 *     all -- use competition/BackupPlainDrive, which talks to the motors directly.
 *   - Telemetry now counts shots, by spotting the dip in flywheel RPM as each
 *     pollen hits the wheel.
 *   - Holding the fire button when the wheel is stuck just outside its target
 *     speed now feeds anyway after about a second, instead of never feeding at
 *     all. The old version would stand there doing nothing and give the shooter
 *     no clue why. Telemetry says "FIRING (not at speed!)" when this happens.
 *     The knob is Launcher.FEED_ANYWAY_AFTER_MS; set it to 0 for the old
 *     behavior.
 *
 * ------------------------------- CONTROLS -------------------------------
 * GAMEPAD 1 (driver)
 *   left stick ........ drive and strafe
 *   right stick ....... turn
 *   Y ................. flip forward/backward (for driving the other way round)
 *   left trigger ...... hold for half speed
 *   D-pad up / down ... flower catcher up / down
 *
 * GAMEPAD 2 (shooter)
 *   A / B / Y ......... spin up to LINE / WALL / CORNER speed (stays spinning)
 *   X ................. flywheel off
 *   D-pad up / down ... nudge the current speed +/- 100 RPM (for finding presets)
 *   left bumper ....... HOLD to fire; only feeds once the wheel is at speed
 *   right bumper ...... run the feeder backwards to clear a jam
 *   left trigger ...... intake in
 *   right trigger ..... intake out
 * ------------------------------------------------------------------------
 */
@TeleOp(name = "1 - Official Drive", group = "A Competition")
public class OfficialDrive extends LinearOpMode {

    // ===================== SETTINGS =====================
    /** Speed multiplier while gamepad 1's left trigger is held. */
    static final double PRECISION_SPEED = 0.5;

    /** How much the gamepad 2 D-pad nudges the flywheel speed. */
    static final double RPM_STEP = 100;

    /** Rumble gamepad 2 for this long when the flywheel first reaches speed. */
    static final int READY_RUMBLE_MS = 2000;
    // ====================================================

    @Override
    public void runOpMode() {

        // One object, built here, where hardwareMap is actually populated.
        Robot robot = new Robot(hardwareMap);

        double forwardBackwardMult = 1;   // 1 = normal, -1 = reversed (gamepad 1 Y)
        boolean wasAtSpeed = false;

        telemetry.addLine("Official Drive ready. Press PLAY.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            // ----- DRIVE (gamepad 1) -----

            if (gamepad1.yWasPressed()) {
                forwardBackwardMult = -forwardBackwardMult;
            }

            // Reversed mode flips forward and strafe, but NOT turning, so the robot
            // still turns the way the stick points.
            double forward = -gamepad1.left_stick_y * forwardBackwardMult;
            double strafe = gamepad1.left_stick_x * forwardBackwardMult;
            double turn = gamepad1.right_stick_x;

            double speed = gamepad1.left_trigger > 0 ? PRECISION_SPEED : 1.0;

            robot.drive(forward, strafe, turn, speed);

            // ----- FLOWER CATCHER (gamepad 1 D-pad) -----

            if (gamepad1.dpadUpWasPressed()) {
                robot.catcher.up();
            } else if (gamepad1.dpadDownWasPressed()) {
                robot.catcher.down();
            }

            // ----- FLYWHEEL SPEED (gamepad 2 buttons) -----
            // Tap once to pick a speed; the wheel STAYS spinning until X.
            // Spin it up while driving so it's ready when you arrive.

            if (gamepad2.bWasPressed()) {
                robot.launcher.setTarget(ShotPreset.WALL);
            } else if (gamepad2.aWasPressed()) {
                robot.launcher.setTarget(ShotPreset.LINE);
            } else if (gamepad2.yWasPressed()) {
                robot.launcher.setTarget(ShotPreset.CORNER);
            } else if (gamepad2.xWasPressed()) {
                robot.launcher.off();
            }

            // Fine-tune the current speed to find good presets. Write down what works
            // and put it in ShotPreset.
            if (gamepad2.dpadUpWasPressed()) {
                robot.launcher.adjustTargetRpm(RPM_STEP);
            } else if (gamepad2.dpadDownWasPressed()) {
                robot.launcher.adjustTargetRpm(-RPM_STEP);
            }

            // ----- RAPID FIRE (gamepad 2 bumpers) -----
            // HOLD left bumper to fire. The feeder waits for the wheel to be at speed,
            // pauses while it recovers after each shot, then fires again.

            robot.launcher.setFiring(gamepad2.left_bumper);
            robot.launcher.setClearingJam(gamepad2.right_bumper);

            // ----- INTAKE (gamepad 2 triggers) -----
            // Triggers win; otherwise the intake helps push pollen up while firing.

            if (gamepad2.left_trigger > 0) {
                robot.intake.in();
            } else if (gamepad2.right_trigger > 0) {
                robot.intake.out();
            } else if (robot.launcher.isFeeding()) {
                robot.intake.in();
            } else {
                robot.intake.stop();
            }

            // ----- ONE UPDATE, EVERY LOOP -----
            // Drives the wheels and the flywheel. Without this line the robot sits
            // there and nothing above has any effect.
            robot.update();

            // Buzz gamepad 2 the moment the wheel is ready, so the shooter doesn't
            // have to watch the screen.
            boolean atSpeed = robot.launcher.isAtSpeed();
            if (atSpeed && !wasAtSpeed && !robot.launcher.isFeeding()) {
                gamepad2.rumble(READY_RUMBLE_MS);
            }
            wasAtSpeed = atSpeed;

            // ----- TELEMETRY -----

            robot.addTelemetry(telemetry);
            telemetry.addData("Drive mode", forwardBackwardMult > 0 ? "NORMAL" : "REVERSED");
            telemetry.addData("Slow mode", speed < 1 ? "ON" : "OFF");
            telemetry.update();
        }

        robot.stopAll();
    }
}
