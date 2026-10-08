package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;


    @Autonomous(name = "Basic Auto for BioBuzz")

    public class basicAuto extends LinearOpMode {

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



            // Wait for the user to give input ready for autonomous
            waitForStart();

            double power = .5;

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

           // runMotors(0,0,0,0);
        }

        //public void runMotors(double a, double b, double c, double d) {
            //double[] motorPowers = {a, b, c, d};
            //DcMotor[] motors = {//frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};

           // for (int g = 0; g<4; g++){
                //motors[g].setPower(motorPowers[g]);
           // }
        //}
                                //test stuff (needlessly complicated)
}