package org.firstinspires.ftc.teamcode.tools;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Motor Test TeleOp", group = "Tools")
public class MotorTestTeleOP extends LinearOpMode {
    // this test allows you to run each motor seperately to test the directions of the motor
    private DcMotor frontLeft, frontRight, backLeft, backRight;

    @Override
    public void runOpMode() {
        frontLeft  = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        frontRight = hardwareMap.get(DcMotor.class, "frontRightDrive");
        backLeft   = hardwareMap.get(DcMotor.class, "backLeftDrive");
        backRight  = hardwareMap.get(DcMotor.class, "backRightDrive");

        telemetry.addLine("Press Play to begin motor test.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            double power = 0.5;

            if (gamepad1.a) {
                frontLeft.setPower(power);
                telemetry.addLine("Front Left running");
            } else {
                frontLeft.setPower(0);
            }

            if (gamepad1.b) {
                frontRight.setPower(power);
                telemetry.addLine("Front Right running");
            } else {
                frontRight.setPower(0);
            }

            if (gamepad1.x) {
                backLeft.setPower(power);
                telemetry.addLine("Back Left running");
            } else {
                backLeft.setPower(0);
            }

            if (gamepad1.y) {
                backRight.setPower(power);
                telemetry.addLine("Back Right running");
            } else {
                backRight.setPower(0);
            }

            telemetry.update();
        }
    }
}
