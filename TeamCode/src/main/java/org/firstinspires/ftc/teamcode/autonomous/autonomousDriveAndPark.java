package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;


@TeleOp(name = "Basic Drive for BioBuzz")

public class autonomousDriveAndPark extends LinearOpMode {

    @Override
    public void runOpMode() {

        // Hardware mapping
        DcMotor frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        DcMotor frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
        DcMotor backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
        DcMotor backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");

        // Wait for the user to give input ready for autonomous
        waitForStart();

        double power = .5;

        // Move Forward
        frontLeftDrive.setPower(power / 1.25);
        frontRightDrive.setPower(power);
        backLeftDrive.setPower(-power);
        backRightDrive.setPower(power);

        sleep(2000);

        // Move Backward
        frontLeftDrive.setPower(-power);
        frontRightDrive.setPower(-power);
        backLeftDrive.setPower(-power);
        backRightDrive.setPower(-power);
    }

}
