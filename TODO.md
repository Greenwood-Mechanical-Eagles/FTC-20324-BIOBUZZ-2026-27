# Team TODO / Roadmap

## Confirmed hardware specs (drivetrain)

- **Motors:** REV HD Hex Motor — 28 encoder ticks per revolution of the *bare*
  motor shaft (before any gearbox).
- **Gearbox:** REV UltraPlanetary, stacked 4:1 + 5:1 cartridges.
  **The stamped ratios are nominal, not actual.** Actual ratios are 3.61:1 and
  5.23:1, so the real stack is **18.88:1**, not the 20:1 the labels imply.
  Assuming 20:1 overshoots distance by ~5.6% (~6.7 inches over a 10-foot drive).
- **Ticks per wheel revolution:** 28 × 3.61 × 5.23 = **528.7**
- **Wheels:** measured at roughly 4 inches → 100mm mecanum (3.937"), giving
  `TICKS_PER_INCH = 42.7`. Still worth confirming with the Encoder Calibration
  push test: a hand measurement can't distinguish 100mm (3.94") from 96mm
  (3.78"), and that ~4% gap is about 5 inches over a 10-foot drive.
  Push test reporting ~44.5 would mean the wheels are actually 96mm.


Notes from planning with Claude on 2026-10-24. Captured here because time with the
physical robot is limited — these are the next infrastructure and code changes to
make when there's time to implement and test them.

## 1. Repo split (infra, do first — it's blocking for everything else)

**Problem:** This repo is a GitHub fork of `FIRST-Tech-Challenge/FtcRobotController`.
Every PR's "Compare & pull request" button defaults the base repo to the upstream
FIRST repo instead of our own `main`. There's no settings toggle for this — it's
inherent to being a fork. High schoolers with little git experience will not
reliably remember to manually switch the base repo dropdown every time.

**Plan:** Create a brand-new, non-fork GitHub repo as the team's primary ("origin")
repo. Keep this current repo around as "upstream" — periodically (manually) pull
FIRST's SDK updates from it into the new repo when needed, since we lose the
one-click "Sync fork" button in exchange for safe, foolproof PRs.

**Still to decide before executing:**
- New repo name (default idea: same name, under `Greenwood-Mechanical-Eagles` org)
- Public or private visibility
- Who re-points their local clone (`git remote set-url origin <new-url>`, then
  `git fetch origin` and `git push -u origin <branch>` once per branch)

## 2. Replace raw `setPower()` with encoder-based control

Currently `basicDrive.java` and `starterBotDrive.java` both drive motors with raw
`setPower()` values straight from joystick input, including the `launcherMotor`
flywheel (`setPower(1)` flat-out). This means shot velocity and drive behavior both
drift as the battery voltage sags over a match.

**Plan:**
- Cast motors to `DcMotorEx` and use `setVelocity(ticksPerSecond)` in
  `RUN_USING_ENCODER` mode for the launcher — command an RPM-equivalent target
  instead of a raw power percentage. The motor controller firmware closes the loop
  against actual voltage automatically, which gets us most of the "regulate power
  against battery voltage" benefit for free (no manual voltage-compensation math
  needed for anything running in velocity mode).
- Use `RUN_TO_POSITION` with `setTargetPosition(ticks)` for "move forward N inches"
  style autonomous moves — convert inches to ticks once in a shared helper.

## 3. Finish wiring up Pedro Pathing (odometry / x-y pose already half-built)

`build.dependencies.gradle` already pulls in `com.pedropathing:revhub` and
`com.pedropathing:tuning`, and `TeamCode/.../pedro/procedures/` already has tuning
OpModes (`ThreeWheelTuner`, `ThreeWheelIMUTuner`, `MecanumTuner`, `ForesightTuner`)
for Pedro Pathing's localizer. Nobody has actually run these tuners or wired the
resulting localizer into a drive OpMode yet — `basicDrive`/`starterBotDrive` are
fully manual mecanum math with no connection to Pedro Pathing.

**Plan:** Run the odometry pod tuners on the real robot, then build OpModes on top
of Pedro Pathing's `Follower` (gives x/y/heading `Pose` tracking and path following)
instead of hand-rolling odometry math from scratch.

## 4. AprilTag vision

Not started. Plan: wrap the FTC SDK's `VisionPortal` + `AprilTagProcessor` in a
`Vision` subsystem class, and feed tag detections in as a periodic pose correction
on top of the Pedro Pathing localizer (odometry drifts over time; AprilTags give
periodic absolute correction).

## 5. Centralize shared code into subsystem classes

Right now `basicDrive.java` and `starterBotDrive.java` duplicate identical hardware
mapping and mecanum drive math. Plan to introduce:
- A `Robot` hardware class each OpMode instantiates once, instead of copy-pasting
  `hardwareMap.get(...)` calls across files.
- Subsystem classes (`Drivetrain`, `Launcher`, `Intake`) wrapping the Pedro Pathing
  `Follower` and `DcMotorEx` velocity control, so students call something like
  `launcher.spinTo(rpm)` instead of touching `setPower`/`setVelocity` directly.
- A `Vision` subsystem (see #4) feeding into the same shared robot pose.

Goal: kids write OpModes using these shared tools rather than re-implementing drive
math, encoder conversions, or vision processing in every new OpMode.

## 6. Battery/power telemetry

Once #2 and #3 are in place (velocity control doing the heavy lifting on voltage
sag), the remaining piece is just visibility: log/telemetry battery voltage every
loop, and add a low-voltage warning threshold for the drive team, rather than manual
power-scaling math.
