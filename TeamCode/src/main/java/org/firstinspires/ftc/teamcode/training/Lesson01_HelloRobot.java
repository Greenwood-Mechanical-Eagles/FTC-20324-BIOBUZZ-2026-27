package org.firstinspires.ftc.teamcode.training;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
@TeleOp(name = "Lesson 01 - Hello Robot")
public class Lesson01_HelloRobot extends LinearOpMode {

    @Override
    public void runOpMode() {

        telemetry.addLine("Hello, Mechanical Eagles!");
        telemetry.addLine("Robot is ready.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            telemetry.addLine("Lesson 01 is running!");
            telemetry.addData("Left Stick Y", gamepad1.left_stick_y);
            telemetry.update();
        }
    }
}
