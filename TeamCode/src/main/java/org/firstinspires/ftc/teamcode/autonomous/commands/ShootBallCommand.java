package org.firstinspires.ftc.teamcode.autonomous.commands;

import com.pedropathing.ivy.behaviors.EndCondition;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

public class ShootBallCommand extends TrackedCommand {
    private final DcMotor launcherMotor;
    private final CRServo launcherServo;

    public ShootBallCommand(DcMotor launcherMotor, CRServo launcherServo) {
        this.launcherMotor = launcherMotor;
        this.launcherServo = launcherServo;
    }


    // Potential bug; The encoder could be used and if so I'm not sure the behavior of setPower directly

    // What needs to be improved, we need to incorporate the logic here that RapidFireDrive uses that
    // takes into account the rpm of the flywheel and
    @Override
    protected void onExecute() {
        if (timer.milliseconds() > 3000) {
            launcherServo.setPower(1);
        } else {
            launcherMotor.setPower(-0.75);
        }
    }

    @Override
    protected boolean isDone() {
        return timer.milliseconds() > 5000;
    }

    @Override
    protected void onEnd(EndCondition interrupted) {
        launcherMotor.setPower(0);
        launcherServo.setPower(0);
    }
}