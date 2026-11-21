package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * ROBOT = every mechanism except the wheels, in one object.
 * (Wheels: Pedro Follower in Auto, DriveBase in TeleOp.)
 *
 * Every OpMode starts the same way:
 *
 *   Robot robot = new Robot(hardwareMap);   // in init() or runOpMode()
 *   ...
 *   robot.update();                         // once EVERY loop
 *
 * Then use robot.launcher, robot.intake, robot.catcher.
 */
public class Robot {

    public final Launcher launcher;
    public final Intake intake;
    public final FlowerCatcher catcher;

    public Robot(HardwareMap hardwareMap) {
        launcher = new Launcher(hardwareMap);
        intake = new Intake(hardwareMap);
        catcher = new FlowerCatcher(hardwareMap);
    }

    /** Call once every loop. Keeps the flywheel at speed and runs the feeder. */
    public void update() {
        launcher.update();
    }

    /** Stop every mechanism. */
    public void stopAll() {
        launcher.off();
        launcher.update();
        intake.stop();
    }

    public void addTelemetry(Telemetry telemetry) {
        launcher.addTelemetry(telemetry);
        telemetry.addData("Intake power", intake.getPower());
    }
}
