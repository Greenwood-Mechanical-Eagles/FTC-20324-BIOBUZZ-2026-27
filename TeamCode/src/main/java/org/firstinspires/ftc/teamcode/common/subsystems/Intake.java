package org.firstinspires.ftc.teamcode.common.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * INTAKE = the motor that pulls pollen in and pushes it up to the feeder.
 *
 *   intake.in();    pull pollen in
 *   intake.out();   spit pollen out
 *   intake.stop();
 */
public class Intake {

    // -1.0 pulls pollen IN (tested: +1 spits it out)
    public static double IN_POWER = -1.0;

    private final DcMotor motor;

    public Intake(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotor.class, "intakeMotor");
    }

    public void in() { motor.setPower(IN_POWER); }

    public void out() { motor.setPower(-IN_POWER); }

    public void stop() { motor.setPower(0); }

    public double getPower() { return motor.getPower(); }
}
