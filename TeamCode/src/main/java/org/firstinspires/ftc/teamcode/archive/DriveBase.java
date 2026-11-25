package org.firstinspires.ftc.teamcode.archive;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * DRIVE BASE for TeleOp (driver control).
 * In Auto, Pedro's Follower drives the wheels instead, so DON'T make one of these in Auto.
 *
 *   drive.drive(forward, strafe, turn, speed);   all -1..1, speed 0..1
 */
public class DriveBase {

    private final DcMotor frontLeft, frontRight, backLeft, backRight;

    public DriveBase(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        frontRight = hardwareMap.get(DcMotor.class, "frontRightDrive");
        backLeft = hardwareMap.get(DcMotor.class, "backLeftDrive");
        backRight = hardwareMap.get(DcMotor.class, "backRightDrive");

        // Tested and correct. Matches pedro/Constants.java.
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.REVERSE);

        // Stop sharply instead of coasting
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    /** Mecanum drive. Same math as RapidFireDrive. */
    public void drive(double forward, double strafe, double turn, double speed) {
        double lf = forward + turn + strafe;
        double rf = forward - turn - strafe;
        double lr = forward + turn - strafe;
        double rr = forward - turn + strafe;

        // Keep all powers between -1 and 1
        double max = Math.max(Math.abs(lf), Math.max(Math.abs(rf),
                Math.max(Math.abs(lr), Math.abs(rr))));
        if (max > 1.0) {
            lf /= max;
            rf /= max;
            lr /= max;
            rr /= max;
        }

        frontLeft.setPower(lf * speed);
        frontRight.setPower(rf * speed);
        backLeft.setPower(lr * speed);
        backRight.setPower(rr * speed);
    }

    public void stop() {
        drive(0, 0, 0, 0);
    }
}
