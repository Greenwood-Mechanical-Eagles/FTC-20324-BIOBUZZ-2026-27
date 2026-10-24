package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

/**
 * Measures TICKS_PER_INCH for the drivetrain by hand-pushing the robot a known
 * distance. The motors are left unpowered and in FLOAT, so the robot rolls freely
 * while the encoders still count.
 *
 * Why push instead of reading the motor spec: ticks-per-inch depends on both the
 * gear ratio and the wheel diameter, and REV UltraPlanetary cartridges are labeled
 * with nominal ratios that differ from their actual ones. Measuring the two unknowns
 * together as a single number avoids all of that, and also absorbs real-world error
 * like tread compression.
 *
 * HOW TO USE:
 *   1. Tape a start and end line on the floor a measured distance apart.
 *      48 inches is a good default - long enough to be accurate, easy to measure.
 *   2. Use the D-pad to set "Push distance" to match your taped distance.
 *   3. Line the robot up on the start line. Press A to zero the encoders.
 *   4. Push the robot straight to the end line, as straight as you can.
 *   5. Read "TICKS_PER_INCH" off the screen and copy it into AutoMoveTest.java.
 *
 * Push it twice and confirm you get roughly the same number before trusting it.
 */
@TeleOp(name = "Encoder Calibration (Push Test)")
public class EncoderCalibration extends LinearOpMode {

    private static final double DEFAULT_PUSH_DISTANCE_INCHES = 48.0;
    private static final double DISTANCE_STEP_INCHES = 6.0;
    private static final double MIN_DISTANCE_INCHES = 6.0;

    private DcMotorEx[] driveMotors;
    private String[] motorNames = {"frontLeftDrive", "frontRightDrive", "backLeftDrive", "backRightDrive"};

    @Override
    public void runOpMode() {

        driveMotors = new DcMotorEx[motorNames.length];
        for (int i = 0; i < motorNames.length; i++) {
            driveMotors[i] = hardwareMap.get(DcMotorEx.class, motorNames[i]);
            // No direction reversals here on purpose - this OpMode uses absolute
            // tick counts, so it stays correct regardless of the drive config.
            driveMotors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            driveMotors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            driveMotors[i].setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            driveMotors[i].setPower(0);
        }

        double pushDistance = DEFAULT_PUSH_DISTANCE_INCHES;
        int[] zeroPositions = currentPositions();

        boolean dpadUpWasPressed = false;
        boolean dpadDownWasPressed = false;

        telemetry.addData("Ready", "Press START, then see instructions on screen");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            if (gamepad1.dpad_up && !dpadUpWasPressed) {
                pushDistance += DISTANCE_STEP_INCHES;
            }
            if (gamepad1.dpad_down && !dpadDownWasPressed) {
                pushDistance = Math.max(MIN_DISTANCE_INCHES, pushDistance - DISTANCE_STEP_INCHES);
            }
            dpadUpWasPressed = gamepad1.dpad_up;
            dpadDownWasPressed = gamepad1.dpad_down;

            if (gamepad1.a) {
                zeroPositions = currentPositions();
            }

            int[] positions = currentPositions();
            double sum = 0;
            for (int i = 0; i < driveMotors.length; i++) {
                sum += Math.abs(positions[i] - zeroPositions[i]);
            }
            double averageTicks = sum / driveMotors.length;

            telemetry.addLine("A = zero encoders | D-pad up/down = set distance");
            telemetry.addLine();
            telemetry.addData("Push distance", "%.0f inches", pushDistance);
            telemetry.addData("Average ticks", "%.0f", averageTicks);
            telemetry.addLine();
            telemetry.addData(">> TICKS_PER_INCH", "%.2f", averageTicks / pushDistance);
            telemetry.addLine("   (copy this into AutoMoveTest.java)");
            telemetry.addLine();

            // Per-motor counts: these should all be close. One reading zero means a
            // dead or unplugged encoder; one wildly different means a slipping wheel.
            telemetry.addLine("Per-motor ticks (should be similar):");
            for (int i = 0; i < driveMotors.length; i++) {
                telemetry.addData("  " + motorNames[i], "%d", positions[i] - zeroPositions[i]);
            }
            telemetry.update();
        }
    }

    private int[] currentPositions() {
        int[] positions = new int[driveMotors.length];
        for (int i = 0; i < driveMotors.length; i++) {
            positions[i] = driveMotors[i].getCurrentPosition();
        }
        return positions;
    }
}
