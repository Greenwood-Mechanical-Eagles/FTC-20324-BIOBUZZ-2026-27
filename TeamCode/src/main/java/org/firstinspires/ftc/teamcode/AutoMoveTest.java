package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * Drive forward, turn left 90 degrees, back up - using velocity (ticks/sec) control
 * instead of raw setPower, so speed holds steady as battery voltage sags.
 *
 * READ BEFORE RUNNING: run the "Encoder Calibration (Push Test)" OpMode first and
 * set TICKS_PER_INCH below from its reading. That constant scales every distance
 * here, and the error is NOT self-correcting - the encoder target and the encoder
 * measurement are both wrong by the same factor, so a bad constant silently drives
 * the wrong distance. The distances below are kept short until it is verified.
 */
@Autonomous(name = "Auto Move Test")
public class AutoMoveTest extends LinearOpMode {

    // ---- CALIBRATION ------------------------------------------------------
    // Drivetrain: REV HD Hex motor (28 ticks per bare motor revolution) with
    // stacked UltraPlanetary 4:1 + 5:1 cartridges - CONFIRMED from the hardware.
    //
    // Watch out: those cartridges are stamped with NOMINAL ratios. Their actual
    // ratios are 3.61:1 and 5.23:1, so the stack is 18.88:1, not the 20:1 the
    // labels imply. Using 20:1 would overshoot by ~5.6% (about 6.7" over 10 feet).
    //     ticks per wheel rev = 28 * 3.61 * 5.23 = 528.7
    //
    // Wheels measured at roughly 4 inches, i.e. 100mm mecanum (3.937").
    //     TICKS_PER_INCH = 528.7 / (PI * 3.937) = 42.7
    //
    // TODO: a hand measurement of "roughly 4 inches" cannot tell 100mm (3.94")
    // apart from 96mm (3.78"), and that difference is ~4% - about 5 inches over a
    // 10-foot drive. Run the "Encoder Calibration (Push Test)" OpMode to settle
    // it and paste the measured value here. If it reports near 44.5 the wheels are
    // actually 96mm; near 42.7 confirms 100mm.
    private static final double TICKS_PER_INCH = 42.7;

    // ---- ROUTINE ----------------------------------------------------------
    // Short distances for calibration. Once the constants above are verified,
    // set these to 120 / 10.0 and 60 / 5.0 for the real "10 ft, turn, back 5 ft"
    // routine. Seconds are chosen to keep speed at 1 ft/sec in both cases.
    private static final double FORWARD_INCHES = 24;
    private static final double FORWARD_SECONDS = 2.0;
    private static final double BACKWARD_INCHES = 12;
    private static final double BACKWARD_SECONDS = 1.0;
    private static final double TURN_DEGREES = 90;

    // ---- TUNING / SAFETY --------------------------------------------------
    private static final double TURN_VELOCITY_TICKS_PER_SEC = 400;
    // Floor on turn speed: the proportional slowdown would otherwise command a
    // speed below stiction near the target, stalling short of the tolerance.
    private static final double TURN_MIN_VELOCITY_TICKS_PER_SEC = 120;
    private static final double TURN_TOLERANCE_DEGREES = 1.5;
    private static final double TURN_SLOWDOWN_DEGREES = 20.0;
    private static final double TURN_TIMEOUT_SECONDS = 5.0;
    // If heading error grows this far past where it started, we are turning the
    // wrong way (inverted IMU sign or miswired motors) - abort instead of spinning.
    private static final double TURN_DIVERGENCE_MARGIN_DEGREES = 10.0;

    private static final double DRIVE_TIMEOUT_MULTIPLIER = 2.0;
    // Stall check: if the encoders have not registered real movement this soon
    // after commanding velocity, something is unplugged - abort before running away.
    private static final double STALL_CHECK_SECONDS = 1.0;
    private static final double STALL_MIN_TICKS = 20;

    private static final long SETTLE_MILLIS = 250;

    private DcMotorEx frontLeftDrive;
    private DcMotorEx frontRightDrive;
    private DcMotorEx backLeftDrive;
    private DcMotorEx backRightDrive;
    private DcMotorEx[] driveMotors;
    private IMU imu;

    @Override
    public void runOpMode() {

        frontLeftDrive = hardwareMap.get(DcMotorEx.class, "frontLeftDrive");
        frontRightDrive = hardwareMap.get(DcMotorEx.class, "frontRightDrive");
        backLeftDrive = hardwareMap.get(DcMotorEx.class, "backLeftDrive");
        backRightDrive = hardwareMap.get(DcMotorEx.class, "backRightDrive");
        driveMotors = new DcMotorEx[]{frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};

        // TODO: copied from basicDrive.java, but starterBotDrive.java reverses a
        // different set of motors - they disagree, so this is an unverified guess.
        // Verify first by running the basicDrive TeleOp: push the left stick forward
        // and confirm the robot drives straight forward, not curving or spinning.
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);

        for (DcMotorEx motor : driveMotors) {
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        // TODO: verify this orientation matches how the Control/Expansion Hub is
        // actually mounted on the robot - this assumes it's mounted flat with the
        // REV logo facing up and the USB ports facing the front of the robot.
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)));

        telemetry.addData("Status", "Initialized - ticks/inch = %.2f", TICKS_PER_INCH);
        telemetry.addData("Routine", "fwd %.0f\" / turn %.0f deg / back %.0f\"",
                FORWARD_INCHES, TURN_DEGREES, BACKWARD_INCHES);
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        if (!driveStraightInches(FORWARD_INCHES, FORWARD_SECONDS)) return;
        sleep(SETTLE_MILLIS);

        if (!turnLeftDegrees(TURN_DEGREES)) return;
        sleep(SETTLE_MILLIS);

        if (!driveStraightInches(-BACKWARD_INCHES, BACKWARD_SECONDS)) return;

        telemetry.addData("Status", "Done");
        telemetry.addData("CALIBRATION", "Measure actual forward distance vs %.0f\" commanded",
                FORWARD_INCHES);
        telemetry.update();
        sleep(3000);
    }

    /**
     * Drives straight by encoder distance. Positive inches drives forward, negative
     * backward. Returns false if the move aborted (stop pressed, stall, or timeout),
     * in which case the caller should not continue the routine.
     */
    private boolean driveStraightInches(double inches, double durationSeconds) {
        double targetTicks = Math.abs(inches * TICKS_PER_INCH);
        double velocityTicksPerSec = (inches * TICKS_PER_INCH) / durationSeconds;

        int[] startPositions = currentPositions();

        for (DcMotorEx motor : driveMotors) {
            motor.setVelocity(velocityTicksPerSec);
        }

        ElapsedTime timer = new ElapsedTime();
        double timeoutSeconds = durationSeconds * DRIVE_TIMEOUT_MULTIPLIER;
        boolean stallChecked = false;

        while (opModeIsActive()) {
            double traveled = averageTicksTraveled(startPositions);

            if (traveled >= targetTicks) {
                stopDriveMotors();
                return true;
            }

            // An encoder reporting nothing means no feedback for the velocity PID,
            // which drives the motor to full power - catch it early, not at timeout.
            if (!stallChecked && timer.seconds() > STALL_CHECK_SECONDS) {
                if (traveled < STALL_MIN_TICKS) {
                    stopDriveMotors();
                    telemetry.addData("ABORT", "No encoder movement after %.1fs", STALL_CHECK_SECONDS);
                    telemetry.addData("Check", "Encoder cables + motor config names");
                    telemetry.update();
                    sleep(5000);
                    return false;
                }
                stallChecked = true;
            }

            if (timer.seconds() >= timeoutSeconds) {
                stopDriveMotors();
                telemetry.addData("ABORT", "Drive timed out at %.0f of %.0f ticks", traveled, targetTicks);
                telemetry.addData("Likely", "TICKS_PER_INCH too high, or motor stalled");
                telemetry.update();
                sleep(5000);
                return false;
            }

            telemetry.addData("Driving", "%.0f / %.0f ticks (%.1f\")", traveled, targetTicks,
                    traveled / TICKS_PER_INCH);
            telemetry.update();
        }

        stopDriveMotors();
        return false;
    }

    /**
     * Turns left to a heading using the IMU. Returns false if the move aborted
     * (stop pressed, diverging, or timeout).
     */
    private boolean turnLeftDegrees(double degrees) {
        double startHeading = currentHeading();
        double targetHeading = normalizeDegrees(startHeading + degrees);
        double initialAbsError = Math.abs(normalizeDegrees(targetHeading - startHeading));

        ElapsedTime timer = new ElapsedTime();

        while (opModeIsActive()) {
            double currentHeading = currentHeading();
            double error = normalizeDegrees(targetHeading - currentHeading);
            double absError = Math.abs(error);

            if (absError <= TURN_TOLERANCE_DEGREES) {
                stopDriveMotors();
                return true;
            }

            // Error should shrink. If it grows well past where it started, we are
            // commanding the wrong direction - stop rather than spin out the timeout.
            if (absError > initialAbsError + TURN_DIVERGENCE_MARGIN_DEGREES) {
                stopDriveMotors();
                telemetry.addData("ABORT", "Turning wrong way (error grew to %.1f deg)", absError);
                telemetry.addData("Check", "IMU mount orientation + motor reversals");
                telemetry.update();
                sleep(5000);
                return false;
            }

            if (timer.seconds() >= TURN_TIMEOUT_SECONDS) {
                stopDriveMotors();
                telemetry.addData("ABORT", "Turn timed out %.1f deg short", absError);
                telemetry.update();
                sleep(5000);
                return false;
            }

            double speed = TURN_VELOCITY_TICKS_PER_SEC;
            if (absError < TURN_SLOWDOWN_DEGREES) {
                speed *= absError / TURN_SLOWDOWN_DEGREES;
            }
            speed = Math.max(speed, TURN_MIN_VELOCITY_TICKS_PER_SEC);
            double turnVelocity = Math.copySign(speed, error);

            // Positive turnVelocity (increasing yaw) = turn left: left side back, right side forward.
            frontLeftDrive.setVelocity(-turnVelocity);
            backLeftDrive.setVelocity(-turnVelocity);
            frontRightDrive.setVelocity(turnVelocity);
            backRightDrive.setVelocity(turnVelocity);

            telemetry.addData("Turning", "target=%.1f current=%.1f error=%.1f",
                    targetHeading, currentHeading, error);
            telemetry.update();
        }

        stopDriveMotors();
        return false;
    }

    private int[] currentPositions() {
        int[] positions = new int[driveMotors.length];
        for (int i = 0; i < driveMotors.length; i++) {
            positions[i] = driveMotors[i].getCurrentPosition();
        }
        return positions;
    }

    /**
     * Average absolute ticks traveled across all four encoders. Averaging means one
     * dead encoder degrades accuracy instead of hanging the move; absolute value per
     * motor keeps a reversed motor from cancelling out the others.
     */
    private double averageTicksTraveled(int[] startPositions) {
        double sum = 0;
        for (int i = 0; i < driveMotors.length; i++) {
            sum += Math.abs(driveMotors[i].getCurrentPosition() - startPositions[i]);
        }
        return sum / driveMotors.length;
    }

    private double currentHeading() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private void stopDriveMotors() {
        for (DcMotorEx motor : driveMotors) {
            motor.setVelocity(0);
        }
    }

    private static double normalizeDegrees(double degrees) {
        double result = degrees % 360.0;
        if (result > 180.0) result -= 360.0;
        if (result < -180.0) result += 360.0;
        return result;
    }
}
