package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Starter Bot Drive")
public class starterBotDrive extends LinearOpMode {

    private DcMotor frontLeftDrive;
    private DcMotor frontRightDrive;
    private DcMotor backLeftDrive;
    private DcMotor backRightDrive;

    private CRServo launcherServo;
    private DcMotor launcherMotor;
    private DcMotor intakeMotor;
    private Servo flowerCatcher;

    @Override
    public void runOpMode() {

        // Hardware mapping
        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");

        launcherMotor = hardwareMap.get(DcMotor.class, "outtakeMotor");
        launcherServo = hardwareMap.get(CRServo.class, "outtakeServo");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        flowerCatcher = hardwareMap.get(Servo.class, "flower");

        // Motor directions: tested and correct. Keep these the same in BasicAuto.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);

        // Brake mode: stop sharply instead of coasting
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Tuning values
        double tuningForward = 1.0;
        double tuningStrafe = 1.0;
        double tuningTurn = 1.0;

        // Drive mode values
        double forwardBackwardMult = 1;   // 1 = normal, -1 = reversed (Y button)
        double precisionMult = 1;         // 0.5 while left trigger is held

        waitForStart();

        while (opModeIsActive()) {

            // ----- DRIVE MODES -----

            // Y toggles reverse driving once per press
            if (gamepad1.yWasPressed()) {
                forwardBackwardMult = -forwardBackwardMult;
            }

            // Hold left trigger for slow, precise driving
            if (gamepad1.left_trigger > 0) {
                precisionMult = 0.5;
            } else {
                precisionMult = 1;
            }

            // ----- DRIVE -----

            // Reverse mode flips forward and strafe, but NOT turning
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

            frontLeftDrive.setPower(powerLF * precisionMult);
            frontRightDrive.setPower(powerRF * precisionMult);
            backLeftDrive.setPower(powerLR * precisionMult);
            backRightDrive.setPower(powerRR * precisionMult);

            // ----- INTAKE (gamepad 2 triggers) -----

            if (gamepad2.left_trigger > 0) {
                intakeMotor.setPower(-1);
            } else if (gamepad2.right_trigger > 0) {
                intakeMotor.setPower(1);
            } else {
                intakeMotor.setPower(0);
            }

            // ----- FEEDER SERVO (gamepad 2 bumpers) -----

            if (gamepad2.left_bumper) {
                launcherServo.setPower(1);
            } else if (gamepad2.right_bumper) {
                launcherServo.setPower(-1);
            } else {
                launcherServo.setPower(0);
            }

            // ----- LAUNCHER (gamepad 2 buttons) -----

            if (gamepad2.b) {
                launcherMotor.setPower(-0.55);   // from wall, high battery (12)
            } else if (gamepad2.y) {
                launcherMotor.setPower(-0.75);   // corner / wall, medium battery (~11)
            } else if (gamepad2.a) {
                launcherMotor.setPower(-0.60);   // from corner, high battery (12.4)
            } else if (gamepad2.x) {
                launcherMotor.setPower(0);       // stop launcher
            }

            // ----- FLOWER CATCHER (gamepad 1 D-pad) -----

            if (gamepad1.dpadUpWasPressed()) {
                flowerCatcher.setPosition(1);
            } else if (gamepad1.dpadDownWasPressed()) {
                flowerCatcher.setPosition(0);
            }

            // ----- TELEMETRY -----

            telemetry.addData("Drive mode", forwardBackwardMult > 0 ? "NORMAL" : "REVERSED");
            telemetry.addData("Slow mode", precisionMult < 1 ? "ON" : "OFF");
            telemetry.addData("LF / RF", "%.2f / %.2f", powerLF, powerRF);
            telemetry.addData("LR / RR", "%.2f / %.2f", powerLR, powerRR);
            telemetry.addData("Launcher power", launcherMotor.getPower());
            telemetry.addData("Intake power", intakeMotor.getPower());
            telemetry.update();
        }
    }
}