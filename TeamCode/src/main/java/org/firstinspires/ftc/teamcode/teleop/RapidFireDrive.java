package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Rapid Fire Drive")
public class RapidFireDrive extends LinearOpMode {

    // ===================== LAUNCHER SETTINGS =====================
    // Flywheel motor: REV HD Hex with UltraPlanetary, NO cartridges (1:1).
    // Bare motor = 28 ticks per rev, about 6000 RPM max with no load.
    // If gear cartridges are ever added, multiply 28 by the gear ratio.
    static final double TICKS_PER_REV = 28;

    // Speed presets in RPM. Tuning showed the real top speed is about 3900 RPM,
    // so these match the old power settings (power x 3900).
    // Fine-tune on the field with gamepad2 D-pad up/down and "Flywheel RPM" telemetry.
    // Keep every preset under ~3500 RPM or the motor can't hold it during a volley.
    static final double WALL_RPM = 2800;  // B  wall shot (tested)
    static final double LINE_RPM = 2600;  // A  line shot (tested)
    static final double CORNER_RPM = 3600;  // Y  corner shot (tested)
    static final double RPM_STEP = 100;    // D-pad up/down adjustment

    // Flywheel PIDF: replace with the numbers from "Flywheel PIDF Tuner".
    // (I is always P/10, D stays 0.)
    static final double LAUNCHER_F = 16.0;   // tuned
    static final double LAUNCHER_P = 1.75;   // tuned

    // How close (in RPM) the flywheel must be to its target before feeding
    static final double RPM_TOLERANCE = 50;

    // Once a volley starts, keep feeding unless the wheel drops this far below
    // target (so it doesn't stop-and-start between every pollen).
    static final double KEEP_FIRING_DROP_RPM = 350;

    // Speed safety check. false = the fire button feeds ANYTIME, no waiting.
    static final boolean SPEED_CHECK_ON = true;

    // Spin-up boost: raw full power until within this many RPM of the target,
    // then exact speed control. Still overshooting? Make this BIGGER (stops boost
    // sooner). Spin-up too slow? Make it smaller. Set to 99999 to turn boost off.
    static final double BOOST_WINDOW_RPM = 400;

    // Feeder servo: +1.0 loads pollen INTO the flywheel (tested on robot).
    // Left bumper = fire (speed safety), right bumper = reverse to clear a jam.
    static final double FEED_POWER = 1.0;

    // Set true if the intake must also run to push pollen up into the feeder
    static final boolean RUN_INTAKE_WHILE_FIRING = true;
    // Intake direction: -1.0 pulls pollen IN (tested: +1 spit it out)
    static final double INTAKE_FEED_POWER = -1.0;
    // =============================================================

    private DcMotor frontLeftDrive;
    private DcMotor frontRightDrive;
    private DcMotor backLeftDrive;
    private DcMotor backRightDrive;

    private CRServo launcherServo;
    private DcMotorEx launcherMotor;   // DcMotorEx gives us setVelocity/getVelocity
    private DcMotor intakeMotor;
    private Servo flowerCatcher;
    private Servo rewolfCatcher; //other flower catcher, "rewolf" = "flower" b'wards

    @Override
    public void runOpMode() {

        // Hardware mapping
        frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "frontRightDrive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "backLeftDrive");
        backRightDrive = hardwareMap.get(DcMotor.class, "backRightDrive");

        launcherMotor = hardwareMap.get(DcMotorEx.class, "outtakeMotor");
        launcherServo = hardwareMap.get(CRServo.class, "outtakeServo");
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        flowerCatcher = hardwareMap.get(Servo.class, "flower");
        rewolfCatcher = hardwareMap.get(Servo.class, "rewolf");

        // Motor directions: tested and correct. Keep these the same in BasicAuto.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);

        // Brake mode: stop sharply instead of coasting
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Launcher: the old code used NEGATIVE power to launch, so reverse the
        // motor and use POSITIVE speeds. Velocity control needs the encoder cable plugged in!
        launcherMotor.setDirection(DcMotor.Direction.REVERSE);
        launcherMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launcherMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launcherMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); // flywheel coasts down
        launcherMotor.setVelocityPIDFCoefficients(LAUNCHER_P, LAUNCHER_P / 10.0, 0, LAUNCHER_F);

        // Tuning values
        double tuningForward = 1.0;
        double tuningStrafe = 1.0;
        double tuningTurn = 1.0;

        // Drive mode values
        double forwardBackwardMult = 1;   // 1 = normal, -1 = reversed (Y button)
        double precisionMult = 1;         // 0.5 while left trigger is held

        // Launcher state
        double targetRPM = 0;
        boolean wasAtSpeed = false;
        boolean boosting = false;
        boolean volleyActive = false;

        waitForStart();

        while (opModeIsActive()) {

            // ----- DRIVE MODES -----

            // Y toggles reverse driving once per press
            if (gamepad1.yWasPressed()) {
                forwardBackwardMult = -forwardBackwardMult;
            }

            // Hold left trigger for slow, precise driving
            if (gamepad1.left_trigger > 0) {
                precisionMult = 0.5;
            } else {
                precisionMult = 1;
            }

            // ----- DRIVE -----

            // Reverse mode flips forward and strafe, but NOT turning
            double Forward = -gamepad1.left_stick_y * forwardBackwardMult;
            double Strafe = gamepad1.left_stick_x * forwardBackwardMult;
            double Turn = gamepad1.right_stick_x;

            // Mecanum drive calculations
            double powerLF = tuningForward * Forward + (tuningTurn * Turn + tuningStrafe * Strafe);
            double powerRF = tuningForward * Forward - (tuningTurn * Turn + tuningStrafe * Strafe);
            double powerLR = tuningForward * Forward + (tuningTurn * Turn - tuningStrafe * Strafe);
            double powerRR = tuningForward * Forward - (tuningTurn * Turn - tuningStrafe * Strafe);

            // Normalize powers (keeps values between -1 and 1)
            double max = Math.max(Math.abs(powerLF), Math.max(Math.abs(powerRF),
                    Math.max(Math.abs(powerLR), Math.abs(powerRR))));
            if (max > 1.0) {
                powerLF /= max;
                powerRF /= max;
                powerLR /= max;
                powerRR /= max;
            }

            frontLeftDrive.setPower(powerLF * precisionMult);
            frontRightDrive.setPower(powerRF * precisionMult);
            backLeftDrive.setPower(powerLR * precisionMult);
            backRightDrive.setPower(powerRR * precisionMult);

            // ----- LAUNCHER SPEED (gamepad 2 buttons) -----
            // Tap once to pick a speed; the flywheel STAYS spinning until X.
            // Spin it up while driving so it's ready when you arrive.

            if (gamepad2.bWasPressed()) {
                targetRPM = WALL_RPM;
            } else if (gamepad2.aWasPressed()) {
                targetRPM = LINE_RPM;
            } else if (gamepad2.yWasPressed()) {
                targetRPM = CORNER_RPM;
            } else if (gamepad2.xWasPressed()) {
                targetRPM = 0;
            }

            // Fine-tune the current speed with D-pad (for finding good presets)
            if (targetRPM > 0) {
                if (gamepad2.dpadUpWasPressed()) {
                    targetRPM += RPM_STEP;
                } else if (gamepad2.dpadDownWasPressed()) {
                    targetRPM = Math.max(RPM_STEP, targetRPM - RPM_STEP);
                }
            }

            double currentRPM = launcherMotor.getVelocity() * 60.0 / TICKS_PER_REV;
            boolean atSpeed;
            boolean wantBoost;

            // BOOST: far below target, use RAW full power (no speed control).
            // Raw power avoids "wind-up" in the hub's controller, which was
            // causing the big overshoot and slow settle.
            wantBoost = targetRPM > 0 && currentRPM < targetRPM - BOOST_WINDOW_RPM;
            if (wantBoost != boosting) {
                launcherMotor.setMode(wantBoost
                        ? DcMotor.RunMode.RUN_WITHOUT_ENCODER
                        : DcMotor.RunMode.RUN_USING_ENCODER);
                if (!wantBoost) {
                    // fresh start for the speed controller after boosting
                    launcherMotor.setVelocityPIDFCoefficients(
                            LAUNCHER_P, LAUNCHER_P / 10.0, 0, LAUNCHER_F);
                }
                boosting = wantBoost;
            }

            if (boosting) {
                launcherMotor.setPower(1.0);
            } else if (targetRPM > 0) {
                // Close to target: the control hub HOLDS this exact speed,
                // so battery level no longer changes the shot.
                launcherMotor.setVelocity(targetRPM * TICKS_PER_REV / 60.0);
            } else {
                launcherMotor.setPower(0);
            }

            atSpeed = targetRPM > 0
                    && Math.abs(currentRPM - targetRPM) <= RPM_TOLERANCE;


            // ----- RAPID FIRE FEEDER (gamepad 2 bumpers) -----
            // HOLD LEFT bumper = fire. The feeder only runs while the flywheel
            // is at speed. After each shot the wheel dips, the feeder pauses
            // for a split second, the wheel recovers, and it fires again.
            // RIGHT bumper = reverse feeder to clear a jam (runs anytime).

            boolean firing = gamepad2.left_bumper;

            // Volley starts the first time the wheel is READY while firing,
            // and lasts until the fire button is released.
            if (!firing || targetRPM == 0) {
                volleyActive = false;
            } else if (atSpeed) {
                volleyActive = true;
            }

            boolean canFeed;
            if (!SPEED_CHECK_ON) {
                canFeed = true;
            } else if (volleyActive) {
                canFeed = currentRPM >= targetRPM - KEEP_FIRING_DROP_RPM;
            } else {
                canFeed = false;   // waiting for the first READY
            }

            if (firing && canFeed) {
                launcherServo.setPower(FEED_POWER);
            } else if (gamepad2.right_bumper) {
                launcherServo.setPower(-FEED_POWER);
            } else {
                launcherServo.setPower(0);
            }

            // Short rumble on gamepad 2 when the flywheel first reaches speed
            if (atSpeed && !wasAtSpeed && !firing) {
                gamepad2.rumble(2000);
            }
            wasAtSpeed = atSpeed;

            // ----- INTAKE (gamepad 2 triggers) -----
            // Triggers always win. Optionally the intake helps push pollen while firing.

            // Left trigger = intake IN, right trigger = push OUT (away from flywheel)
            if (gamepad2.left_trigger > 0) {
                intakeMotor.setPower(INTAKE_FEED_POWER);
            } else if (gamepad2.right_trigger > 0) {
                intakeMotor.setPower(-INTAKE_FEED_POWER);
            } else if (RUN_INTAKE_WHILE_FIRING && firing && canFeed) {
                intakeMotor.setPower(INTAKE_FEED_POWER);
            } else {
                intakeMotor.setPower(0);
            }

            // ----- FLOWER CATCHER (gamepad 1 D-pad) -----

            if (gamepad1.dpadUpWasPressed()) {
                flowerCatcher.setPosition(1);
                rewolfCatcher.setPosition(0);
            } else if (gamepad1.dpadDownWasPressed()) {
                flowerCatcher.setPosition(0);
                rewolfCatcher.setPosition(1);
            }

            // ----- TELEMETRY -----

            telemetry.addData("Launcher", targetRPM == 0 ? "OFF"
                    : (atSpeed ? "READY" : "SPINNING UP"));
            telemetry.addData("Flywheel RPM", "%.0f / target %.0f", currentRPM, targetRPM);
            telemetry.addData("Drive mode", forwardBackwardMult > 0 ? "NORMAL" : "REVERSED");
            telemetry.addData("Slow mode", precisionMult < 1 ? "ON" : "OFF");
            telemetry.addData("LF / RF", "%.2f / %.2f", powerLF, powerRF);
            telemetry.addData("LR / RR", "%.2f / %.2f", powerLR, powerRR);
            telemetry.addData("Intake power", intakeMotor.getPower());
            telemetry.addData("Flywheel Wants Boost", wantBoost);
            telemetry.update();
        }
    }
}