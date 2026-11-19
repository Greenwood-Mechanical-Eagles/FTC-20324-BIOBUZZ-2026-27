package org.firstinspires.ftc.teamcode.autonomous.util;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

public class ShootBallCommandBuilder {
    private final DcMotor launcherMotor;
    private final CRServo launcherServo;

    public ShootBallCommandBuilder(DcMotor launcherMotor, CRServo launcherServo) {
        this.launcherMotor = launcherMotor;
        this.launcherServo = launcherServo;
    }

    private final ElapsedTime launchBallElapsedTime = new ElapsedTime();
    public Command launchBall() {
        return Command.build()
                // When this starts make sure the timer is starting from Fresh
                .setStart(launchBallElapsedTime::reset)
                // The actions that this command will do
                .setExecute(() -> {
                    launcherMotor.setPower(-0.75);
                    if (launchBallElapsedTime.milliseconds() > 3000) launcherServo.setPower(1);

                })
                // A statement to evaluate if the command should be considered done
                .setDone(() -> launchBallElapsedTime.milliseconds() > 5000)
                // Handles what should happen if the program is forced to stop unexpectantly
                .setEnd((interrupted) -> {
                    launcherMotor.setPower(0);
                    launcherServo.setPower(0);
                });
    }
}
