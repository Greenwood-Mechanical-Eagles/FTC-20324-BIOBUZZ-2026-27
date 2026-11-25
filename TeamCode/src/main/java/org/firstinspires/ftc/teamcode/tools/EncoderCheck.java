package org.firstinspires.ftc.teamcode.tools;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * ENCODER CHECK -- which motors are in the config, and which ones have a
 * working encoder?
 *
 * Answers two questions you otherwise have to guess at, and it does it on a
 * bench in about two minutes.
 *
 * 1. IS EVERY MOTOR IN THE CONFIG?
 *    Each motor is looked up separately and a missing one is reported as
 *    "NOT IN CONFIG" instead of crashing the OpMode. So if an OpMode is dying
 *    at INIT with a "device not found", run this first and it will name the
 *    one that is wrong. (This is gate 0 of TESTPLAN.md.)
 *
 * 2. DOES IT HAVE AN ENCODER?
 *    Turn each motor by hand, or hold a bumper to spin the intake or flywheel,
 *    and watch its Ticks column.
 *      Ticks change  -> encoder is plugged in and working
 *      Ticks stay 0  -> no encoder cable, or a dead one
 *
 * WHAT NEEDS AN ENCODER AND WHAT DOESN'T
 *   outtakeMotor (flywheel)  REQUIRED. All RPM control and all shot counting
 *                            depend on it. If this one reads 0, nothing about
 *                            shooting will work.
 *   the 4 drive motors       Not used today. Position comes from the Pinpoint
 *                            pods, and pedro/Constants only sets names and
 *                            directions. Encoders here are spare capacity for
 *                            drive velocity control later.
 *   intakeMotor              Not used today. common/subsystems/Intake asks for
 *                            a plain DcMotor and only calls setPower, so an
 *                            encoder on it is currently ignored even if fitted.
 *
 * Nothing here moves the robot unless you hold a bumper, and the bumpers only
 * run the intake and the flywheel at low power. Safe to run on a bench.
 */
@TeleOp(name = "Encoder Check", group = "Tools")
public class EncoderCheck extends LinearOpMode {

    // ===================== SETTINGS =====================
    /** Power used by the hold-to-spin buttons. Low on purpose. */
    static final double NUDGE_POWER = 0.25;
    // ====================================================

    /** The motors we expect, in the order they are worth reading. */
    private static final String[] NAMES = {
            "outtakeMotor",     // flywheel -- the one that MUST have an encoder
            "intakeMotor",
            "frontLeftDrive",
            "frontRightDrive",
            "backLeftDrive",
            "backRightDrive",
    };

    private final DcMotorEx[] motors = new DcMotorEx[NAMES.length];
    private final int[] startTicks = new int[NAMES.length];

    @Override
    public void runOpMode() {

        // Look each one up on its own, so one bad name doesn't hide the others.
        for (int i = 0; i < NAMES.length; i++) {
            try {
                motors[i] = hardwareMap.get(DcMotorEx.class, NAMES[i]);
            } catch (Exception e) {
                motors[i] = null;   // reported as NOT IN CONFIG below
            }
        }

        telemetry.addLine("Encoder Check");
        telemetry.addLine();
        reportConfig(telemetry);
        telemetry.addLine();
        telemetry.addLine("Press PLAY, then turn each motor BY HAND and watch Ticks.");
        telemetry.update();

        waitForStart();

        zero();

        while (opModeIsActive()) {

            // Hold to spin the two that are hard to turn by hand.
            if (motors[0] != null) {
                motors[0].setPower(gamepad1.right_bumper ? NUDGE_POWER : 0);
            }
            if (motors[1] != null) {
                motors[1].setPower(gamepad1.left_bumper ? NUDGE_POWER : 0);
            }

            if (gamepad1.aWasPressed()) {
                zero();
            }

            telemetry.addLine("Turn a motor by hand, or:");
            telemetry.addLine("  RIGHT bumper = spin flywheel    LEFT bumper = spin intake");
            telemetry.addLine("  A = zero the counts");
            telemetry.addLine();
            telemetry.addLine("Ticks moving = encoder works.  Stuck on 0 = no encoder.");
            telemetry.addLine();

            for (int i = 0; i < NAMES.length; i++) {
                if (motors[i] == null) {
                    telemetry.addData(NAMES[i], "NOT IN CONFIG");
                    continue;
                }
                int moved = motors[i].getCurrentPosition() - startTicks[i];
                telemetry.addData(NAMES[i], "ticks %+d   vel %.0f   %s",
                        moved,
                        motors[i].getVelocity(),
                        moved == 0 ? "-- not moved yet" : "ENCODER OK");
            }

            telemetry.update();
        }

        // Leave nothing spinning.
        for (DcMotorEx m : motors) {
            if (m != null) {
                m.setPower(0);
            }
        }
    }

    /** Remember where every encoder started, so the display shows change, not raw counts. */
    private void zero() {
        for (int i = 0; i < NAMES.length; i++) {
            startTicks[i] = motors[i] == null ? 0 : motors[i].getCurrentPosition();
        }
    }

    private void reportConfig(Telemetry telemetry) {
        int found = 0;
        for (DcMotorEx m : motors) {
            if (m != null) {
                found++;
            }
        }
        telemetry.addData("Motors found", "%d of %d", found, NAMES.length);
        for (int i = 0; i < NAMES.length; i++) {
            telemetry.addData(NAMES[i], motors[i] == null ? "NOT IN CONFIG" : "in config");
        }
    }
}
