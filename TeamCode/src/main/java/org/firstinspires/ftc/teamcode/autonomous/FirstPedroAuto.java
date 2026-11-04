package org.firstinspires.ftc.teamcode.autonomous;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.autonomous.util.BioBuzzPaths;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "FirstPedroAuto", group = "Autonomous")
public class FirstPedroAuto extends LinearOpMode {

    private Follower follower;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private BioBuzzPaths bioBuzzPaths;

    // Autonomous routine
    public Command autoRoutine() {
        return sequential(
                follow(follower, bioBuzzPaths.myFirstPath())
        );
    }

    @Override
    public void runOpMode() {
        Scheduler.reset();
        bioBuzzPaths = new BioBuzzPaths();

        follower = Constants.create(hardwareMap);
        follower.setPose(bioBuzzPaths.nonShooting1);
        follower.update();

        waitForStart();
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
}