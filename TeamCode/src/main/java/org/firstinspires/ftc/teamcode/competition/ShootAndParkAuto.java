package org.firstinspires.ftc.teamcode.competition;

import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.common.Alliance;
import org.firstinspires.ftc.teamcode.common.commands.RobotCommands;
import org.firstinspires.ftc.teamcode.common.paths.ShootAndParkPaths;
import org.firstinspires.ftc.teamcode.common.subsystems.Robot;
import org.firstinspires.ftc.teamcode.common.subsystems.ShotPreset;

/**
 * SHOOT AND PARK: drive off the wall, fire the preloaded pollen, cross to the
 * corner, park.
 *
 * This class holds the whole Auto. It does NOT appear on the Driver Station --
 * OfficialAutoRed and OfficialAutoBlue do, and each is a four-line file that
 * says which alliance it is. Both run this exact code, so a fix here fixes both
 * sides, and red and blue can never drift apart.
 *
 * Field numbers live in common/paths/ShootAndParkPaths (red-side, flipped for
 * blue automatically). The path came from the Pedro Visualizer; the export is
 * kept in docs/PedroPathing/ShootAndPark.pp.json.
 *
 * ###########################################################################
 * #  NOT YET RUN ON THE ROBOT. Run competition/BackupPark in matches until    #
 * #  this has gone 3 for 3 on the practice field.                             #
 * #  Check it on a stand first, then read docs/PedroPathing/README.md -- the  #
 * #  shooting heading is the one number most likely to be wrong.              #
 * ###########################################################################
 *
 * This file is meant to be read by students as the example of how an Auto is
 * built, so it is written the long way round with every step named. For the
 * longer patterns -- going out to collect more pollen, or spinning the flywheel
 * up while you drive -- see the Auto template in your own student folder.
 */
public abstract class ShootAndParkAuto extends OpMode {

    // ======================= STRATEGY =======================
    /** Flywheel speed for the opening shot. TUNE THIS ON THE FIELD. */
    private static final ShotPreset OPENING_SHOT = ShotPreset.AUTO_START;

    /** How many preloaded pollen to fire. */
    private static final int OPENING_SHOT_COUNT = 3;
    // ========================================================

    private Robot robot;
    private ShootAndParkPaths paths;

    /** Which side of the field this OpMode is for. Red and blue subclasses answer this. */
    protected abstract Alliance alliance();

    // ---------------- The plan ----------------

    private Command autoRoutine() {
        return sequential(
                // Drive forward off the wall to the shooting spot.
                follow(robot.follower, paths.startToShoot()),

                // follow() finishes a moment before the robot has actually stopped,
                // so let it settle before shooting rather than firing while sliding.
                RobotCommands.settle(robot),

                // Fire the pollen we started the match with.
                RobotCommands.shootVolley(robot, OPENING_SHOT, OPENING_SHOT_COUNT),

                // Back across the field to the corner, then up the field to park.
                follow(robot.follower, paths.shootToCorner()),
                follow(robot.follower, paths.cornerToPark())
        );
    }

    // ---------------- OpMode plumbing ----------------

    @Override
    public void init() {
        // Scheduler is shared across every OpMode, so clear out anything the last
        // one left behind.
        Scheduler.reset();

        paths = new ShootAndParkPaths(alliance());

        // EVERY hardwareMap.get happens inside here, never in a field. Before init()
        // runs, hardwareMap is empty, and touching it crashes the moment you press
        // INIT.
        robot = new Robot(hardwareMap);

        // Tell the follower where on the field we are. Everything else is measured
        // from here, so if this is wrong, every path is wrong.
        robot.follower.setPose(paths.START);
        robot.update();
    }

    @Override
    public void init_loop() {
        // Check this against where the robot is actually standing BEFORE pressing play.
        telemetry.addData("ALLIANCE", alliance());
        telemetry.addLine("Put the robot on the START pose, then press PLAY.");
        telemetry.addData("START pose", "x %.1f  y %.1f  heading %.0f deg",
                paths.START.x(), paths.START.y(), Math.toDegrees(paths.START.heading()));
        telemetry.addLine();
        robot.addTelemetry(telemetry);
        telemetry.update();
    }

    @Override
    public void start() {
        Scheduler.schedule(autoRoutine());
    }

    @Override
    public void loop() {
        // ORDER MATTERS. robot.update() drives the follower and the launcher and
        // works out whether the current path is finished. Run the Scheduler first
        // and Auto races through every path doing nothing. See Robot.update().
        robot.update();
        Scheduler.execute();

        telemetry.addData("ALLIANCE", alliance());
        robot.addTelemetry(telemetry);
        if (robot.follower.currentPath() != null) {
            telemetry.addData("Inches to end of path", "%.1f", robot.follower.distanceToEndpoint());
            telemetry.addData("Path number", robot.follower.pathIndex());
        }
        telemetry.update();
    }

    @Override
    public void stop() {
        robot.stopAll();
        Scheduler.reset();
    }
}
