package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.VoltageSensor;

@TeleOp(name = "Basic Drive for BioBuzz")
public class basicDrive extends LinearOpMode {

    private DcMotor frontLeftDrive;
    private DcMotor frontRightDrive;
    private DcMotor backLeftDrive;
    private DcMotor backRightDrive;
    private VoltageSensor battery;

    @Override
    public void runOpMode() {

        // Hardware mapping
        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");
        battery = hardwareMap.voltageSensor.iterator().next();

        // Motor directions: tested and correct. Keep these the same in
        // Starter Bot Drive and BasicAuto.
        frontLeftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotorSimple.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightDrive.setDirection(DcMotorSimple.Direction.FORWARD);

        // Brake mode: stop sharply instead of coasting
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Tuning values
        double tuningForward = 1.0;
        double tuningStrafe = 1.0;
        double tuningTurn = 1.0;

        // Drive direction: -1 makes "stick forward" drive toward the end
        // the drivers treat as the front. Flips forward and strafe only;
        // turning is unaffected. Do not change motor directions to fix this,
        // because that would also change BasicAuto and Starter Bot Drive.
        final double DRIVE_DIRECTION = -1;

        // Precision mode multiplier
        double precisionMult = 1;

        telemetry.addData("Status", "Ready");
        telemetry.addData("Battery V", "%.2f", battery.getVoltage());
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // Gamepad input
            double Forward = -gamepad1.left_stick_y * DRIVE_DIRECTION;
            double Strafe = gamepad1.left_stick_x * DRIVE_DIRECTION;
            double Turn = gamepad1.right_stick_x;

            // Mecanum drive calculations
            double powerLF = tuningForward * Forward + (tuningTurn * Turn + tuningStrafe * Strafe);
            double powerRF = tuningForward * Forward - (tuningTurn * Turn + tuningStrafe * Strafe);
            double powerLR = tuningForward * Forward + (tuningTurn * Turn - tuningStrafe * Strafe);
            double powerRR = tuningForward * Forward - (tuningTurn * Turn - tuningStrafe * Strafe);

            // Normalize powers (keeps values between -1 and 1)
            double max = Math.max(Math.abs(powerLF), Math.max(Math.abs(powerRF),
                    Math.max(Math.abs(powerLR), Math.abs(powerRR))));
            if (max > 1.0) {
                powerLF /= max;
                powerRF /= max;
                powerLR /= max;
                powerRR /= max;
            }

            // Hold left trigger for slow, precise driving
            if (gamepad1.left_trigger > 0.1) {
                precisionMult = 0.5;
            } else {
                precisionMult = 1;
            }

            // Set motor power
            frontLeftDrive.setPower(powerLF * precisionMult);
            frontRightDrive.setPower(powerRF * precisionMult);
            backLeftDrive.setPower(powerLR * precisionMult);
            backRightDrive.setPower(powerRR * precisionMult);

            // Telemetry
            telemetry.addData("Battery V", "%.2f", battery.getVoltage());
            telemetry.addData("Forward", "%.2f", Forward);
            telemetry.addData("Strafe", "%.2f", Strafe);
            telemetry.addData("Turn", "%.2f", Turn);
            telemetry.addData("LF / RF", "%.2f / %.2f", powerLF, powerRF);
            telemetry.addData("LR / RR", "%.2f / %.2f", powerLR, powerRR);
            telemetry.addData("Precision Mode", precisionMult < 1 ? "ON" : "OFF");
            telemetry.update();
        }
    }
}