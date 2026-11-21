package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * FLOWER CATCHER = the two catcher servos ("flower" and "rewolf").
 * They are mirror images, so one goes to 1 while the other goes to 0.
 */
public class FlowerCatcher {

    private final Servo flower;
    private final Servo rewolf; // "rewolf" = "flower" backwards (mirror side)

    public FlowerCatcher(HardwareMap hardwareMap) {
        flower = hardwareMap.get(Servo.class, "flower");
        rewolf = hardwareMap.get(Servo.class, "rewolf");
    }

    /** Same as gamepad1 D-pad UP in RapidFireDrive. */
    public void up() {
        flower.setPosition(1);
        rewolf.setPosition(0);
    }

    /** Same as gamepad1 D-pad DOWN in RapidFireDrive. */
    public void down() {
        flower.setPosition(0);
        rewolf.setPosition(1);
    }
}
