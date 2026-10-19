package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;


@Autonomous(name = "Launching Auto for BioBuzz")

public class launchAuto extends LinearOpMode {

  //  DcMotor frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
//      DcMotor frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
//    DcMotor backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
//        DcMotor backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");

    @Override
    public void runOpMode() {



        // Hardware mapping
        DcMotor frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        DcMotor frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
        DcMotor backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
        DcMotor backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");

        DcMotor launcherMotor = hardwareMap.get(DcMotor.class, "outtakeMotor");
        CRServo launcherServo = hardwareMap.get(CRServo.class, "outtakeServo");



        // Wait for the user to give input ready for autonomous
        waitForStart();

        double power = .5;

        //shoot ball

        launcherMotor.setPower(0.75);
        sleep(500);

        launcherServo.setPower(1);
        sleep(500);

        launcherServo.setPower(0);
        launcherMotor.setPower(0);

        sleep(500);

        // Corkscrew
        frontLeftDrive.setPower(power);
        frontRightDrive.setPower(power);
        backLeftDrive.setPower(power);
        backRightDrive.setPower(power);

        sleep(1000);

        // Move Backward
        frontLeftDrive.setPower(power);
        frontRightDrive.setPower(power);
        backLeftDrive.setPower(-power);
        backRightDrive.setPower(power);

        sleep(750);
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);

        runMotors(0,0,0,0,10, true);
    }

    public void runMotors(double a, double b, double c, double d, long time, boolean last) {
        DcMotor frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        DcMotor frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
        DcMotor backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
        DcMotor backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");

        frontLeftDrive.setPower(a);
        frontRightDrive.setPower(b);
        backLeftDrive.setPower(-c); //account for reversed motor
        backRightDrive.setPower(d);
        sleep(time);
        if (last) {

        }

    }
                            //test stuff (needlessly complicated)
}