package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

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

        // Motor directions: tested and correct. Keep these the same in
        // Starter Bot Drive and BasicAuto.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.FORWARD);

        // Brake mode: stop sharply instead of coasting
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Tuning values
        double tuningForward = 1.0;
        double tuningStrafe = 1.0;
        double tuningTurn = 1.0;

        //precision values
        double precisionMult = 1;
        double forwardBackwardMult = 1;

        waitForStart();

        //c.maunualBrakeMode.set(true)

        while (opModeIsActive()) {

            // Gamepad input
            double Forward = -gamepad1.left_stick_y * forwardBackwardMult;
            double Strafe = gamepad1.left_stick_x * forwardBackwardMult;
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


            if (gamepad1.yWasPressed()) {
                forwardBackwardMult = -forwardBackwardMult;
            }

            // Hold left trigger for slow, precise driving
            if (gamepad1.left_trigger > 0) {
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
            telemetry.addData("Forward", Forward);
            telemetry.addData("Strafe", Strafe);
            telemetry.addData("Turn", Turn);
            telemetry.addData("LF / RF", "%.2f / %.2f", powerLF, powerRF);
            telemetry.addData("LR / RR", "%.2f / %.2f", powerLR, powerRR);
            telemetry.addData("Current Power Mult (affects speed and direction)", precisionMult * forwardBackwardMult);
            telemetry.update();
        }
    }
}