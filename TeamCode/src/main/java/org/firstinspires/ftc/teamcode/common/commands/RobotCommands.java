package org.firstinspires.ftc.teamcode.common.commands;

import com.acmerobotics.dashboard.config.Config;
import static com.pedropathing.ivy.groups.Groups.sequential;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.common.subsystems.Robot;
import org.firstinspires.ftc.teamcode.common.subsystems.ShotPreset;

/**
 * COMMANDS = building blocks for Autonomous.
 *
 * Snap them together with sequential(...) next to Pedro's follow(...):
 *
 *   sequential(
 *       RobotCommands.shootVolley(robot, ShotPreset.AUTO_START, 3),
 *       RobotCommands.intakeOn(robot),
 *       follow(robot.follower, paths.toPollen()),
 *       RobotCommands.intakeOff(robot),
 *       follow(robot.follower, paths.backToShoot()),
 *       RobotCommands.settle(robot),
 *       RobotCommands.shootVolley(robot, ShotPreset.WALL, 3),
 *       follow(robot.follower, paths.toPark())
 *   )
 *
 * IMPORTANT: the OpMode's loop() must call robot.update() and then
 * Scheduler.execute(), in that order, every single loop. Nothing here finishes
 * otherwise. See the comment on Robot.update() for why the order matters.
 *
 * EVERY COMMAND HERE HAS A TIME LIMIT. A command that waits for something that
 * never happens would eat the rest of Autonomous, so each one gives up and moves
 * on instead. Telemetry tells you afterwards whether it worked or timed out.
 *
 * Every command only TELLS the Robot what to do; the Robot does the real work.
 * That is what keeps Auto and TeleOp shooting the same way.
 */
@Config
public final class RobotCommands {

    private RobotCommands() {} // just a toolbox, never "new" this

    // ===================== SETTINGS =====================
    // Worst case for one shootVolley is SPIN_UP_TIMEOUT_MS + VOLLEY_TIMEOUT_MS,
    // about 7 seconds of a 30 second Auto. Shrink these if the Auto is running out
    // of time; a healthy flywheel spins up in well under 2 seconds.

    /** How long to let the flywheel spin up before firing anyway. */
    public static double SPIN_UP_TIMEOUT_MS = 4000;

    /** How long a volley may take before we give up on the pollen that are left. */
    public static double VOLLEY_TIMEOUT_MS = 9000;

    /** Below this speed (inches/sec) the robot counts as stopped. */
    public static double SETTLED_INCHES_PER_SEC = 2.0;

    /** How long to wait for the robot to stop drifting before shooting anyway. */
    public static double SETTLE_TIMEOUT_MS = 1000;
    // ====================================================

    // ----------------- Launcher -----------------

    /**
     * Start the flywheel at a preset speed. Finishes right away and the wheel keeps
     * spinning, so put this BEFORE a follow(...) and the shot is ready on arrival.
     */
    public static Command spinUp(Robot robot, ShotPreset preset) {
        return Command.build()
                .setStart(() -> robot.launcher.setTarget(preset))
                .setDone(() -> true);
    }

    /** Wait until the flywheel is at speed, giving up after timeoutMs. */
    public static Command waitUntilReady(Robot robot, double timeoutMs) {
        ElapsedTime timer = new ElapsedTime();
        return Command.build()
                .setStart(timer::reset)
                .setDone(() -> robot.launcher.isAtSpeed() || timer.milliseconds() > timeoutMs);
    }

    /**
     * Fire until the launcher has counted {@code shots} pollen leaving the robot,
     * or until timeoutMs runs out.
     *
     * Counting beats timing: "fire 3 pollen" takes exactly as long as it takes,
     * where "feed for 2 seconds" either cuts the last shot off or stands there
     * doing nothing. The launcher spots each shot by the dip in flywheel RPM.
     *
     * The intake runs whenever the feeder is actually feeding, to push the next
     * pollen up into the feeder.
     */
    public static Command fire(Robot robot, int shots, double timeoutMs) {
        ElapsedTime timer = new ElapsedTime();
        return Command.build()
                .setStart(() -> {
                    timer.reset();
                    robot.launcher.resetShotCount();
                    robot.launcher.setFiring(true);
                })
                .setExecute(() -> {
                    if (robot.launcher.isFeeding()) {
                        robot.intake.in();
                    } else {
                        robot.intake.stop();
                    }
                })
                .setDone(() -> robot.launcher.getShotCount() >= shots
                        || timer.milliseconds() > timeoutMs)
                .setEnd(endCondition -> {
                    robot.launcher.setFiring(false);
                    robot.intake.stop();
                });
    }

    /**
     * Fire for a fixed number of milliseconds, ignoring the shot counter.
     * Use this only if shot counting is misbehaving on the field; fire(...) is better.
     */
    public static Command fireFor(Robot robot, double fireMs) {
        return fire(robot, Integer.MAX_VALUE, fireMs);
    }

    /** Stop the flywheel and feeder. */
    public static Command launcherOff(Robot robot) {
        return Command.build()
                .setStart(robot.launcher::off)
                .setDone(() -> true);
    }

    /**
     * The whole shot in one block: spin up, wait for READY, fire that many pollen,
     * turn off. This is the one most Autos need.
     */
    public static Command shootVolley(Robot robot, ShotPreset preset, int shots) {
        return shootVolley(robot, preset, shots, VOLLEY_TIMEOUT_MS);
    }

    public static Command shootVolley(Robot robot, ShotPreset preset, int shots, double timeoutMs) {
        return sequential(
                spinUp(robot, preset),
                waitUntilReady(robot, SPIN_UP_TIMEOUT_MS),
                fire(robot, shots, timeoutMs),
                launcherOff(robot)
        );
    }

    /**
     * Same as shootVolley but leaves the flywheel spinning, so a second volley a
     * moment later doesn't pay for a whole spin-up again. Remember to finish the
     * Auto with launcherOff(robot).
     */
    public static Command shootVolleyKeepSpinning(Robot robot, ShotPreset preset, int shots) {
        return sequential(
                spinUp(robot, preset),
                waitUntilReady(robot, SPIN_UP_TIMEOUT_MS),
                fire(robot, shots, VOLLEY_TIMEOUT_MS)
        );
    }

    // ----------------- Driving -----------------

    /**
     * Wait for the robot to stop drifting before doing something that needs it to
     * be still, like shooting.
     *
     * A follow(...) command finishes the moment the robot reaches the END of the
     * path on paper, which is a little before it has actually stopped there. Shoot
     * on that exact loop and the pollen leaves a robot that is still sliding, which
     * is how an Auto that works on the practice field misses at a meet. Pedro holds
     * the end pose for us, so this just waits for the sliding to finish.
     */
    public static Command settle(Robot robot) {
        return settle(robot, SETTLED_INCHES_PER_SEC, SETTLE_TIMEOUT_MS);
    }

    public static Command settle(Robot robot, double maxInchesPerSec, double timeoutMs) {
        ElapsedTime timer = new ElapsedTime();
        return Command.build()
                .setStart(timer::reset)
                .setDone(() -> robot.follower.velocity().toVector2D().magnitude() < maxInchesPerSec
                        || timer.milliseconds() > timeoutMs);
    }

    /** Hold the pose the robot is standing on right now (resists being pushed). */
    public static Command holdHere(Robot robot) {
        return Command.build()
                .setStart(() -> robot.follower.hold(robot.follower.pose()))
                .setDone(() -> true);
    }

    // ----------------- Intake -----------------

    public static Command intakeOn(Robot robot) {
        return Command.build()
                .setStart(robot.intake::in)
                .setDone(() -> true);
    }

    public static Command intakeOff(Robot robot) {
        return Command.build()
                .setStart(robot.intake::stop)
                .setDone(() -> true);
    }

    /** Spit pollen back out (clearing a stuck one). */
    public static Command intakeReverse(Robot robot) {
        return Command.build()
                .setStart(robot.intake::out)
                .setDone(() -> true);
    }

    // ----------------- Flower catcher -----------------

    public static Command catcherUp(Robot robot) {
        return Command.build()
                .setStart(robot.catcher::up)
                .setDone(() -> true);
    }

    public static Command catcherDown(Robot robot) {
        return Command.build()
                .setStart(robot.catcher::down)
                .setDone(() -> true);
    }

    // ----------------- Timing -----------------

    /** Do nothing for ms milliseconds (let pollen settle, wait for a partner robot...). */
    public static Command waitMs(double ms) {
        ElapsedTime timer = new ElapsedTime();
        return Command.build()
                .setStart(timer::reset)
                .setDone(() -> timer.milliseconds() > ms);
    }
}
