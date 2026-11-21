package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.DriveBase;
import org.firstinspires.ftc.teamcode.subsystems.Robot;
import org.firstinspires.ftc.teamcode.subsystems.ShotPreset;

/**
 * Same controls as RapidFireDrive, rebuilt from the shared building blocks.
 * Test this side by side with RapidFireDrive before using it in a match.
 *
 * Use this file as the TEMPLATE for new TeleOps.
 *
 * GAMEPAD 1: sticks = drive, Y = reverse drive, left trigger = slow, D-pad up/down = flower catcher
 * GAMEPAD 2: A/B/Y = line/wall/corner speed, X = flywheel off, D-pad up/down = +/-100 RPM,
 *            left bumper = fire, right bumper = clear jam, left trigger = intake in, right trigger = out
 */
@TeleOp(name = "Rapid Fire Drive (blocks)")
public class RapidFireDriveBlocks extends LinearOpMode {

    static final double RPM_STEP = 100;
    static final boolean RUN_INTAKE_WHILE_FIRING = true;

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap);
        DriveBase drive = new DriveBase(hardwareMap);

        double reverse = 1;          // 1 = normal, -1 = reversed
        boolean wasAtSpeed = false;

        waitForStart();

        while (opModeIsActive()) {

            // ----- DRIVE (gamepad 1) -----
            if (gamepad1.yWasPressed()) reverse = -reverse;
            double speed = gamepad1.left_trigger > 0 ? 0.5 : 1.0;

            drive.drive(
                    -gamepad1.left_stick_y * reverse,  // forward
                    gamepad1.left_stick_x * reverse,   // strafe
                    gamepad1.right_stick_x,            // turn (never reversed)
                    speed);

            // ----- LAUNCHER SPEED (gamepad 2) -----
            if (gamepad2.bWasPressed()) robot.launcher.setTarget(ShotPreset.WALL);
            else if (gamepad2.aWasPressed()) robot.launcher.setTarget(ShotPreset.LINE);
            else if (gamepad2.yWasPressed()) robot.launcher.setTarget(ShotPreset.CORNER);
            else if (gamepad2.xWasPressed()) robot.launcher.off();

            if (gamepad2.dpadUpWasPressed()) robot.launcher.adjustTargetRpm(RPM_STEP);
            else if (gamepad2.dpadDownWasPressed()) robot.launcher.adjustTargetRpm(-RPM_STEP);

            // ----- FIRE / CLEAR JAM (gamepad 2 bumpers) -----
            boolean firing = gamepad2.left_bumper;
            robot.launcher.setFiring(firing);
            robot.launcher.setClearingJam(gamepad2.right_bumper);

            // Flywheel speed + feeder happen here
            robot.update();

            // ----- INTAKE (gamepad 2 triggers win; otherwise help while feeding) -----
            if (gamepad2.left_trigger > 0) robot.intake.in();
            else if (gamepad2.right_trigger > 0) robot.intake.out();
            else if (RUN_INTAKE_WHILE_FIRING && robot.launcher.isFeeding()) robot.intake.in();
            else robot.intake.stop();

            // ----- FLOWER CATCHER (gamepad 1 D-pad) -----
            if (gamepad1.dpadUpWasPressed()) robot.catcher.up();
            else if (gamepad1.dpadDownWasPressed()) robot.catcher.down();

            // Rumble gamepad 2 when the flywheel first reaches speed
            boolean atSpeed = robot.launcher.isAtSpeed();
            if (atSpeed && !wasAtSpeed && !firing) gamepad2.rumble(2000);
            wasAtSpeed = atSpeed;

            // ----- TELEMETRY -----
            robot.addTelemetry(telemetry);
            telemetry.addData("Drive mode", reverse > 0 ? "NORMAL" : "REVERSED");
            telemetry.addData("Slow mode", speed < 1 ? "ON" : "OFF");
            telemetry.update();
        }

        robot.stopAll();
        drive.stop();
    }
}
