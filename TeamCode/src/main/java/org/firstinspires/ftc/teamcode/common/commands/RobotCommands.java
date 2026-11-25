package org.firstinspires.ftc.teamcode.common.commands;

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
 *       RobotCommands.shootVolley(robot, ShotPreset.AUTO_START, 2000),
 *       follow(follower, myPath()),
 *       RobotCommands.intakeOn(robot),
 *       follow(follower, pickupPath()),
 *       RobotCommands.intakeOff(robot)
 *   )
 *
 * IMPORTANT: the OpMode's loop() must call robot.update() and Scheduler.execute()
 * every loop, or these commands will never finish.
 *
 * Every command here only TELLS the Robot what to do. The Robot (Launcher) does the
 * real work, so TeleOp and Auto always shoot the same way.
 */
public final class RobotCommands {

    private RobotCommands() {} // just a toolbox, never "new" this

    // ----------------- Launcher -----------------

    /** Start the flywheel at a preset speed. Finishes right away (wheel keeps spinning). */
    public static Command spinUp(Robot robot, ShotPreset preset) {
        return Command.build()
                .setStart(() -> robot.launcher.setTarget(preset))
                .setExecute(() -> {})
                .setDone(() -> true)
                .setEnd((interrupted) -> {});
    }

    /** Wait until the flywheel is at speed, but give up after timeoutMs so Auto never gets stuck. */
    public static Command waitUntilReady(Robot robot, double timeoutMs) {
        ElapsedTime timer = new ElapsedTime();
        return Command.build()
                .setStart(timer::reset)
                .setExecute(() -> {})
                .setDone(() -> robot.launcher.isAtSpeed() || timer.milliseconds() > timeoutMs)
                .setEnd((interrupted) -> {});
    }

    /**
     * Fire for fireMs milliseconds. The feeder only runs while the wheel is at speed
     * (same rule as TeleOp), and the intake helps push pollen up while feeding.
     */
    public static Command fire(Robot robot, double fireMs) {
        ElapsedTime timer = new ElapsedTime();
        return Command.build()
                .setStart(() -> {
                    timer.reset();
                    robot.launcher.setFiring(true);
                })
                .setExecute(() -> {
                    if (robot.launcher.isFeeding()) {
                        robot.intake.in();
                    } else {
                        robot.intake.stop();
                    }
                })
                .setDone(() -> timer.milliseconds() > fireMs)
                .setEnd((interrupted) -> {
                    robot.launcher.setFiring(false);
                    robot.intake.stop();
                });
    }

    /** Stop the flywheel and feeder. */
    public static Command launcherOff(Robot robot) {
        return Command.build()
                .setStart(robot.launcher::off)
                .setExecute(() -> {})
                .setDone(() -> true)
                .setEnd((interrupted) -> {});
    }

    /**
     * The whole shot in one block: spin up, wait for READY (max 3 s), fire, turn off.
     * This is the one most Autos need.
     */
    public static Command shootVolley(Robot robot, ShotPreset preset, double fireMs) {
        return sequential(
                spinUp(robot, preset),
                waitUntilReady(robot, 3000),
                fire(robot, fireMs),
                launcherOff(robot)
        );
    }

    // ----------------- Intake -----------------

    public static Command intakeOn(Robot robot) {
        return Command.build()
                .setStart(robot.intake::in)
                .setExecute(() -> {})
                .setDone(() -> true)
                .setEnd((interrupted) -> {});
    }

    public static Command intakeOff(Robot robot) {
        return Command.build()
                .setStart(robot.intake::stop)
                .setExecute(() -> {})
                .setDone(() -> true)
                .setEnd((interrupted) -> {});
    }

    // ----------------- Flower catcher -----------------

    public static Command catcherUp(Robot robot) {
        return Command.build()
                .setStart(robot.catcher::up)
                .setExecute(() -> {})
                .setDone(() -> true)
                .setEnd((interrupted) -> {});
    }

    public static Command catcherDown(Robot robot) {
        return Command.build()
                .setStart(robot.catcher::down)
                .setExecute(() -> {})
                .setDone(() -> true)
                .setEnd((interrupted) -> {});
    }

    // ----------------- Timing -----------------

    /** Do nothing for ms milliseconds (let pollen settle, wait for a partner robot...). */
    public static Command waitMs(double ms) {
        ElapsedTime timer = new ElapsedTime();
        return Command.build()
                .setStart(timer::reset)
                .setExecute(() -> {})
                .setDone(() -> timer.milliseconds() > ms)
                .setEnd((interrupted) -> {});
    }
}
