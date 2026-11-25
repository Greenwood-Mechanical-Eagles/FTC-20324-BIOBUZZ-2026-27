package org.firstinspires.ftc.teamcode.students.ethan;

import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.common.commands.RobotCommands;
import org.firstinspires.ftc.teamcode.common.subsystems.Robot;
import org.firstinspires.ftc.teamcode.common.subsystems.ShotPreset;

/**
 * ETHAN'S AUTO -- a template. Copy this file, rename it, and build your own plan.
 *
 * THE ONLY PART YOU NEED TO WRITE IS autoRoutine(). Everything below it is
 * plumbing that is the same in every Auto; read it once, then leave it alone.
 *
 * THE BUILDING BLOCKS
 *   follow(robot.follower, paths.toPollen())      drive a path
 *   RobotCommands.shootVolley(robot, speed, n)    spin up, wait, fire n pollen, off
 *   RobotCommands.spinUp(robot, speed)            start the wheel, carry on instantly
 *   RobotCommands.fire(robot, n, timeoutMs)       fire n pollen with the wheel already up
 *   RobotCommands.launcherOff(robot)              stop the wheel
 *   RobotCommands.settle(robot)                   wait for the robot to stop drifting
 *   RobotCommands.intakeOn(robot) / intakeOff     intake
 *   RobotCommands.catcherUp(robot) / catcherDown  flower catcher
 *   RobotCommands.waitMs(500)                     do nothing for a moment
 *
 * TWO RULES THAT WILL SAVE YOU AN AFTERNOON
 *   1. Shoot AFTER a settle(), not straight after a follow(). A follow finishes
 *      when the robot reaches the end of the path on paper, which is slightly
 *      before it has actually stopped there.
 *   2. spinUp() finishes instantly, so put it BEFORE a follow() and the flywheel
 *      comes up to speed while you drive. That is free time.
 *
 * Remember: @Disabled keeps this off the Driver Station. Delete that line when
 * your Auto is ready to try, and put it back if you leave it half-finished.
 */
@Disabled
@Autonomous(name = "Ethan: Auto Template", group = "Ethan")
public class EthanAuto extends OpMode {

    private final EthanPaths paths = new EthanPaths();
    private Robot robot;

    // =========================================================
    //  YOUR PLAN GOES HERE
    // =========================================================
    private Command autoRoutine() {
        return sequential(
                // Shoot the pollen we started with, from the start tile.
                RobotCommands.shootVolley(robot, ShotPreset.AUTO_START, 3),

                // Go and collect more.
                RobotCommands.intakeOn(robot),
                follow(robot.follower, paths.toPollen()),
                RobotCommands.intakeOff(robot),

                // Drive back, spinning the wheel up on the way so no time is wasted.
                RobotCommands.spinUp(robot, ShotPreset.WALL),
                follow(robot.follower, paths.backToShoot()),

                // Let the robot stop moving, THEN shoot.
                RobotCommands.settle(robot),
                RobotCommands.fire(robot, 3, 4000),
                RobotCommands.launcherOff(robot),

                // Park.
                follow(robot.follower, paths.toPark())
        );
    }
    // =========================================================

    @Override
    public void init() {
        Scheduler.reset();
        robot = new Robot(hardwareMap);        // every hardwareMap.get happens in here
        robot.follower.setPose(paths.START);   // tell the follower where we are
        robot.update();
    }

    @Override
    public void init_loop() {
        telemetry.addLine("Put the robot on the START pose, then press PLAY.");
        robot.addTelemetry(telemetry);
        telemetry.update();
    }

    @Override
    public void start() {
        Scheduler.schedule(autoRoutine());
    }

    @Override
    public void loop() {
        robot.update();        // follower AND launcher. Must come before the Scheduler.
        Scheduler.execute();   // runs your plan

        robot.addTelemetry(telemetry);
        telemetry.update();
    }

    @Override
    public void stop() {
        robot.stopAll();
        Scheduler.reset();
    }
}
