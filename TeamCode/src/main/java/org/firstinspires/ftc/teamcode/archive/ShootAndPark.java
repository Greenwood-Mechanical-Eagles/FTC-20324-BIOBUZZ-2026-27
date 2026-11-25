package org.firstinspires.ftc.teamcode.archive;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;

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
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@Disabled
@Autonomous(name = "Shoots ball, and then parks", group = "Autonomous")
public class ShootAndPark extends OpMode {

    private Follower follower;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(83.8934, 134.9426, 90);
    private final Pose startToOtherSide = poseFactory.of(82.8707, 23.4222, 180);
    private final Pose toPark = poseFactory.of(137.3504, 24.2483, -179.1313);

    private final DcMotor launcherMotor = hardwareMap.get(DcMotor.class, "outtakeMotor");
    private final CRServo launcherServo = hardwareMap.get(CRServo.class, "outtakeServo");
    private ShootBallCommand shootBallCommand;


    public Path startToOtherSide() {
        return Paths.line(start, startToOtherSide).linear(start, startToOtherSide);
    }

    public Path toPark() {
        return Paths.line(startToOtherSide, toPark).reverseTangent();
    }



    public Command autoRoutine() {
        return sequential(
                shootBallCommand.execute(),
                follow(follower, startToOtherSide()),
                follow(follower, toPark())
        );
    }

    @Override
    public void init() {
        shootBallCommand = new ShootBallCommand(launcherMotor, launcherServo);

        //hardware mapping
        //contain chunk within init() function?
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(start);
        follower.update();
    }


    @Override
    public void start() {
        schedule(autoRoutine());
    }


    @Override
    public void loop() {
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
/*
    public Path startToOtherSide() {
        return Paths.line(start, startToOtherSide).linear(start, startToOtherSide);
    }

    public Path toPark() {
        return Paths.line(startToOtherSide, toPark).reverseTangent();
    } */

