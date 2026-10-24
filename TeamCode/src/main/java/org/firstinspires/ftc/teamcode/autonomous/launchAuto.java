package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


@Autonomous(name = "Launching Auto for BioBuzz")

public class launchAuto extends LinearOpMode {

  //  DcMotor frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
//      DcMotor frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
//    DcMotor backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
//        DcMotor backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");

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




        // Wait for the user to give input ready for autonomous
        waitForStart();

        double power = .5;

        //shoot ball

        launcherMotor.setPower(-0.5);
        sleep(2000);

        launcherServo.setPower(1);
        sleep(2000);

        launcherServo.setPower(0);
        launcherMotor.setPower(0);

        sleep(700);

        //forward
        runMotors(power,power,power,power,500,false);

        // Corkscrew
        frontLeftDrive.setPower(power * 3/4);
        frontRightDrive.setPower(power);
        backLeftDrive.setPower(power);
        backRightDrive.setPower(power);

        sleep(1000);

        // Move Backward
        frontLeftDrive.setPower(power);
        frontRightDrive.setPower(power);
        backLeftDrive.setPower(-power);
        backRightDrive.setPower(power);

        sleep(700);
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);

        runMotors(0,0,0,0,100, true);

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
            frontLeftDrive.setPower(0);
            frontRightDrive.setPower(0);
            backLeftDrive.setPower(0);
            backRightDrive.setPower(0);
        }

    }
                            //test stuff (needlessly complicated)
}