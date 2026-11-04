package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name = "Basic Auto for BioBuzz")
public class BasicAuto extends LinearOpMode {

    private DcMotor frontLeftDrive;
    private DcMotor frontRightDrive;
    private DcMotor backLeftDrive;
    private DcMotor backRightDrive;

    @Override
    public void runOpMode() {
        // Map hardware once, here (not as field initializers, where hardwareMap is still null)
        frontLeftDrive  = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
        backLeftDrive   = hardwareMap.get(DcMotor.class, "backLeftDrive");
        backRightDrive  = hardwareMap.get(DcMotor.class, "backRightDrive");

        // Motor directions: tested and correct. Keep these the same in the TeleOp.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);

        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addLine("Ready");
        telemetry.update();

        waitForStart();

        // drive(forward, strafe, turn, milliseconds)
        // Positive forward = forward, positive strafe = right, positive turn = clockwise
        drive(0.5, 0, 0, 800);   // drive forward off the start line (LEAVE)
        // drive(0, 0.5, 0, 600); // example: strafe right
        // drive(0, 0, 0.4, 500); // example: turn right
    }

    // Same mecanum math as the TeleOp, then stops the robot
    private void drive(double forward, double strafe, double turn, long ms) {
        if (!opModeIsActive()) return;

        double lf = forward + strafe + turn;
        double rf = forward - strafe - turn;
        double lr = forward - strafe + turn;
        double rr = forward + strafe - turn;

        double max = Math.max(1.0, Math.max(Math.abs(lf), Math.max(Math.abs(rf),
                Math.max(Math.abs(lr), Math.abs(rr)))));

        frontLeftDrive.setPower(lf / max);
        frontRightDrive.setPower(rf / max);
        backLeftDrive.setPower(lr / max);
        backRightDrive.setPower(rr / max);

        sleep(ms);
        stopDrive();
    }

    private void stopDrive() {
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);
    }
}