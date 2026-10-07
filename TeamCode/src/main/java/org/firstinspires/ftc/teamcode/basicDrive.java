package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.CoreHex;


@TeleOp(name = "Basic Drive for BioBuzz")

public class basicDrive extends LinearOpMode {

    private DcMotor frontLeftDrive;
    private DcMotor frontRightDrive;
    private DcMotor backLeftDrive;
    private DcMotor backRightDrive;


    @Override
    public void runOpMode() {

        // Hardware mapping
        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");

        // Reverse left side motors if needed
        //frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        //backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);


        // Tuning values
        double tuningForward = 1.0;
        double tuningStrafe = 1.0;
        double tuningTurn = 1.0;

        waitForStart();

        while (opModeIsActive()) {

            // Gamepad input
            double Forward = -gamepad1.left_stick_y;
            double Strafe = gamepad1.left_stick_x;
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

            // Set motor power
            frontLeftDrive.setPower(powerLF);
            frontRightDrive.setPower(powerRF);
            backLeftDrive.setPower(powerLR);
            backRightDrive.setPower(powerRR);

            // Telemetry
            telemetry.addData("Forward", Forward);
            telemetry.addData("Strafe", Strafe);
            telemetry.addData("Turn", Turn);
            telemetry.addData("LF", powerLF);
            telemetry.addData("RF", powerRF);
            telemetry.addData("LR", powerLR);
            telemetry.addData("RR", powerRR);
            telemetry.update();
        }
    }

}
