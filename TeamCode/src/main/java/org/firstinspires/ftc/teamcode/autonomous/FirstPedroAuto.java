package org.firstinspires.ftc.teamcode.autonomous;

import com.pedropathing.api.Paths;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@Disabled
@Autonomous(name = "AutoPath", group = "Autonomous")
public class FirstPedroAuto extends LinearOpMode {

    private Follower follower;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(83.8934, 134.9426, 90);
    private final Pose startToOtherSide = poseFactory.of(82.8707, 23.4222, 180);
    private final Pose toPark = poseFactory.of(137.3504, 24.2483, -179.1313);
    private DcMotor launcherMotor;

    private CRServo launcherServo;

    boolean doneThing;




    // Autonomous routine

    /*public Command startWait() {
        waitForStart();
        //return;
        return null;
    } */

    /*
    public Command launchBall() {
        return Command.build()
                .setExecute(() -> {
                launcherMotor.setPower(-0.75);
                sleep(3000);

                launcherServo.setPower(1);
                sleep(2000);

                launcherMotor.setPower(0);
                launcherServo.setPower(0);
                });
    } */

    Command launchBall = Command.build()
            .setStart(() -> {
                doneThing = false;
            })
            .setExecute(() -> {
                launcherMotor.setPower(-0.75);
                sleep(3000);

                launcherServo.setPower(1);
                sleep(2000);

                launcherMotor.setPower(0);
                launcherServo.setPower(0);
            })
           /* .setDone(() -> {
                return doneThing = true;
            }) */
            .setEnd(endCondition -> {
                // executed on end
            })
            .requiring(launcherMotor, launcherServo);

    public Command autoRoutine() {
        return sequential(
                //startWait(),
                //command for launching ball goes here?
                follow(follower, startToOtherSide()),
                follow(follower, toPark())
        );
    }

    @Override
    public void runOpMode() {

        //hardware mapping
        launcherMotor = hardwareMap.get(DcMotor.class, "outtakeMotor");
        launcherServo = hardwareMap.get(CRServo.class, "outtakeServo");

        //contain chunk within init() function?

        //put this stuff outside the function or maybe still here in the in-between space if that works

            //public Command startWait() {
            waitForStart();
             //return;
            //}

        launcherMotor.setPower(-0.7);
        sleep(2000);

        launcherServo.setPower(1);
        sleep(2000);

        launcherMotor.setPower(0);
        launcherServo.setPower(0);

        schedule(autoRoutine());

        while (opModeIsActive()) {
            follower.update();
            Scheduler.execute();



            telemetry.addData("x", follower.pose().x());
            telemetry.addData("y", follower.pose().y());
            telemetry.addData("heading", follower.pose().heading());

            if (follower.currentPath() != null) {
                telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
                telemetry.addData("Path number", follower.pathIndex());
            }

            telemetry.update();
        }
    }

    public Path startToOtherSide() {
        return Paths.line(start, startToOtherSide).linear(start, startToOtherSide);
    }

    public Path toPark() {
        return Paths.line(startToOtherSide, toPark).reverseTangent();
    }
}
