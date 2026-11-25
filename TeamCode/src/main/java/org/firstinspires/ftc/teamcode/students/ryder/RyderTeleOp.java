package org.firstinspires.ftc.teamcode.students.ryder;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.subsystems.Robot;
import org.firstinspires.ftc.teamcode.common.subsystems.ShotPreset;

/**
 * RYDER'S TELEOP -- a template, and about as short as a TeleOp gets.
 *
 * Every TeleOp is this same shape:
 *   read the gamepads  ->  tell the robot what to do  ->  robot.update()
 *
 * IDEAS WORTH TRYING
 *   - One-driver layout: put everything on gamepad1.
 *   - Auto-fire: fire by itself the instant the wheel is READY
 *         robot.launcher.setFiring(robot.launcher.isAtSpeed());
 *   - Field-centric drive: the robot goes where the stick points no matter which
 *     way it is facing. The pose comes from robot.follower.pose().heading().
 *   - Hold position: a button that plants the robot
 *         robot.follower.hold(robot.follower.pose());
 *     (then stop calling robot.drive() while it holds)
 *   - Shot counter on screen: robot.launcher.getShotCount()
 *
 * Delete @Disabled when you want it on the Driver Station.
 */
@Disabled
@TeleOp(name = "Ryder: TeleOp Template", group = "Ryder")
public class RyderTeleOp extends LinearOpMode {

    @Override
    public void runOpMode() {

        Robot robot = new Robot(hardwareMap);

        waitForStart();

        while (opModeIsActive()) {

            // ----- DRIVE -----
            robot.drive(
                    -gamepad1.left_stick_y,   // forward  (the stick reads backwards, hence the minus)
                    gamepad1.left_stick_x,    // strafe
                    gamepad1.right_stick_x);  // turn

            // ----- SHOOT -----
            if (gamepad2.aWasPressed()) robot.launcher.setTarget(ShotPreset.LINE);
            if (gamepad2.bWasPressed()) robot.launcher.setTarget(ShotPreset.WALL);
            if (gamepad2.yWasPressed()) robot.launcher.setTarget(ShotPreset.CORNER);
            if (gamepad2.xWasPressed()) robot.launcher.off();

            robot.launcher.setFiring(gamepad2.left_bumper);

            // ----- INTAKE -----
            if (gamepad2.left_trigger > 0) {
                robot.intake.in();
            } else if (gamepad2.right_trigger > 0) {
                robot.intake.out();
            } else {
                robot.intake.stop();
            }

            // ----- THE LINE THAT MAKES IT ALL HAPPEN -----
            robot.update();

            robot.addTelemetry(telemetry);
            telemetry.update();
        }

        robot.stopAll();
    }
}
