package org.firstinspires.ftc.teamcode.tools;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

/*
 * FLYWHEEL PIDF TUNER  (all controls on GAMEPAD 1)
 *
 *   Hold A ............ full-power test (measures max speed, gives Suggested F)
 *   B ................. flywheel ON / OFF at target speed
 *   D-pad UP / DOWN ... F  +0.5 / -0.5
 *   D-pad RIGHT / LEFT  P  +0.25 / -0.25   (I is set to P/10 automatically)
 *   Right / Left bumper target RPM +100 / -100
 *   Hold RIGHT TRIGGER  feed pollen (feeder + intake) for test shots
 *
 * Values reset when the OpMode stops, so WRITE THEM DOWN.
 */
@TeleOp(name = "Flywheel PIDF Tuner", group = "Tools")
public class FlywheelPIDFTuner extends LinearOpMode {

    static final double TICKS_PER_REV = 28;       // bare REV HD Hex, no cartridges
    static final double FEED_POWER = 1.0;         // same as RapidFireDrive
    static final double INTAKE_FEED_POWER = -1.0; // same as RapidFireDrive

    @Override
    public void runOpMode() {
        DcMotorEx flywheel = hardwareMap.get(DcMotorEx.class, "outtakeMotor");
        CRServo feeder = hardwareMap.get(CRServo.class, "outtakeServo");
        DcMotor intake = hardwareMap.get(DcMotor.class, "intakeMotor");

        flywheel.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        double p = 1.75;   // last tuned value (matches common/subsystems/Launcher)
        double f = 16.0;   // last tuned value (matches common/subsystems/Launcher)
        double targetRPM = 2400;
        boolean running = false;
        boolean inFullPowerTest = false;
        boolean pidfChanged = true;

        double maxTicksPerSec = 0;
        double lowestRPM = 0;
        boolean wasFeeding = false;

        telemetry.addLine("Flywheel PIDF Tuner ready. Press START.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            // ----- ADJUST VALUES -----
            if (gamepad1.dpadUpWasPressed())    { f += 0.5; pidfChanged = true; }
            if (gamepad1.dpadDownWasPressed())  { f = Math.max(0, f - 0.5); pidfChanged = true; }
            if (gamepad1.dpadRightWasPressed()) { p += 0.25; pidfChanged = true; }
            if (gamepad1.dpadLeftWasPressed())  { p = Math.max(0, p - 0.25); pidfChanged = true; }
            if (gamepad1.rightBumperWasPressed()) targetRPM += 100;
            if (gamepad1.leftBumperWasPressed())  targetRPM = Math.max(100, targetRPM - 100);
            if (gamepad1.bWasPressed()) running = !running;

            // ----- FULL-POWER TEST MODE (hold A) -----
            // Must run WITHOUT the encoder to get true raw full power.
            boolean wantFullPower = gamepad1.a;
            if (wantFullPower != inFullPowerTest) {
                flywheel.setMode(wantFullPower
                        ? DcMotor.RunMode.RUN_WITHOUT_ENCODER
                        : DcMotor.RunMode.RUN_USING_ENCODER);
                inFullPowerTest = wantFullPower;
                pidfChanged = true;
            }

            if (pidfChanged) {
                flywheel.setVelocityPIDFCoefficients(p, p / 10.0, 0, f);
                pidfChanged = false;
            }

            double ticksPerSec = Math.abs(flywheel.getVelocity());
            double rpm = ticksPerSec * 60.0 / TICKS_PER_REV;

            if (inFullPowerTest) {
                flywheel.setPower(1.0);
                maxTicksPerSec = Math.max(maxTicksPerSec, ticksPerSec);
            } else if (running) {
                flywheel.setVelocity(targetRPM * TICKS_PER_REV / 60.0);
            } else {
                flywheel.setPower(0);
            }

            // ----- TEST SHOTS (hold right trigger) -----
            boolean feeding = gamepad1.right_trigger > 0.5;
            if (feeding && !wasFeeding) {
                lowestRPM = rpm;   // start tracking a new volley
            }
            if (feeding) {
                feeder.setPower(FEED_POWER);
                intake.setPower(INTAKE_FEED_POWER);
                lowestRPM = Math.min(lowestRPM, rpm);
            } else {
                feeder.setPower(0);
                intake.setPower(0);
            }
            wasFeeding = feeding;

            // ----- TELEMETRY -----
            telemetry.addData("Flywheel", inFullPowerTest ? "FULL POWER TEST"
                    : (running ? "ON" : "OFF  (press B)"));
            telemetry.addData("RPM", "%.0f   target %.0f", rpm, targetRPM);
            telemetry.addData("Error (target - RPM)", "%.0f", targetRPM - rpm);
            telemetry.addLine();
            telemetry.addData("F  (dpad up/down)", "%.2f", f);
            telemetry.addData("P  (dpad right/left)", "%.2f", p);
            telemetry.addData("I  (auto = P/10)", "%.3f", p / 10.0);
            telemetry.addLine();
            telemetry.addData("Max ticks/sec (hold A)", "%.0f", maxTicksPerSec);
            telemetry.addData("Suggested F", maxTicksPerSec > 0
                    ? String.format("%.2f", 32767.0 / maxTicksPerSec) : "hold A first");
            telemetry.addData("Lowest RPM in last volley", "%.0f", lowestRPM);
            telemetry.update();
        }
    }
}