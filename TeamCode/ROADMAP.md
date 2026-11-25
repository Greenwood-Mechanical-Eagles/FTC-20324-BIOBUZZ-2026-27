# BioBuzz 20324: Code Roadmap

**Status: Phase 1 built, not yet run on the robot.** Last updated 2026-11-25.

## Goal

Give students building blocks that do about 90% of the work, so each student can build their own Auto or TeleOp and spend their time on strategy ideas instead of plumbing. Shooting, driving and mechanisms work the same way in every OpMode.

## Where we are

```
teamcode/
  common/
    subsystems/   Robot, Launcher, Intake, FlowerCatcher, ShotPreset
    commands/     RobotCommands
    paths/        ShootAndParkPaths (red numbers, mirrored for blue)
    Alliance      RED / BLUE and the field flip
  competition/
    auto/         OfficialAutoRed/Blue, ShootAndParkAuto, BackupPark
    teleop/       OfficialDrive, BackupPlainDrive
  students/
    ethan/        EthanAuto, EthanTeleOp, EthanPaths      (templates, @Disabled)
    ryder/        RyderAuto, RyderTeleOp, RyderPaths      (templates, @Disabled)
  pedro/          Pedro config and tuning procedures
  tools/          FlywheelPIDFTuner, MotorTestTeleOP, X/Y movement tests
  training/       Lesson 01
  archive/        old OpModes, all @Disabled -- see archive/readme.md
```

| File | Status |
| --- | --- |
| `competition/teleop/OfficialDrive` | **Not yet driven.** The meet TeleOp, rebuilt on `Robot`. Wheel powers are identical to the proven version (see below); the launcher logic is the same code moved into `Launcher` |
| `competition/teleop/BackupPlainDrive` | **Proven at the meet.** The old Rapid Fire Drive, untouched, raw `setPower`, no odometry. The fallback if the Pinpoint dies |
| `competition/auto/OfficialAutoRed` / `OfficialAutoBlue` | **Not yet run.** Real poses in from the Visualizer (`docs/PedroPathing/`). Both sides come from one set of red numbers. Check the shooting heading first -- see `docs/PedroPathing/README.md` |
| `competition/auto/BackupPark` | **Field tested.** Drives forward about 17 in. and parks. The Auto to run until the official one has been tested |
| `common/*` | Built and compiling. Untested on hardware |
| `tools/FlywheelPIDFTuner` | Now starts at the team's real values, P = 1.75, F = 16 |

The problem this architecture set out to fix — TeleOp shooting by RPM while Auto shot by raw power — is fixed. `Launcher` is the only code that knows how to shoot, and both modes call it.

## Architecture

```
   Auto OpMode                     TeleOp OpMode
       |   \                            |
       |    RobotCommands               |  (calls Robot directly)
       |    (shootVolley, intakeOn...)  |
       v        v                       v
  +------------------------ Robot ------------------------+
  |  Pedro Follower   Launcher    Intake    FlowerCatcher  |
  |  (drives Auto     (RPM +      (in/out/  (up/down)      |
  |   and TeleOp)      feeder)     stop)                   |
  |              robot.update() every loop                 |
  +--------------------------------------------------------+
```

- **Robot**: one object that holds every mechanism *and* the wheels. `new Robot(hardwareMap)` once, then `robot.update()` every loop.
- **Pedro Follower** (inside Robot) drives in both modes: `follow(robot.follower, path)` in Auto, `robot.drive(forward, strafe, turn)` in TeleOp. One drive setup, one set of tuning, and a robot pose that carries from Auto into TeleOp.
- **Launcher** is the only place that knows how to shoot, and it counts shots by watching the flywheel RPM dip as each pollen hits.
- **ShotPreset** is one list of RPMs that both modes use.
- **RobotCommands** are Ivy commands for Auto, snapped together with `sequential(...)`. Every one has a time limit.
- **No interfaces (strategy pattern) for now.** With one launcher there is nothing to swap. Revisit if a second shooter design or a simulator appears.
- **Fallback:** `competition/teleop/BackupPlainDrive` is a plain `setPower` TeleOp that doesn't touch Pedro, in case odometry fails during a match.

### Three things in here that are easy to get wrong

**1. `robot.update()` goes before `Scheduler.execute()`.** A `follow(...)` command finishes when the follower reports it reached the end of its path, and the follower only works that out inside `follower.update()`. Run the Scheduler first and Auto skips through every path in a fraction of a second, with no error. `Robot.update()` exists partly to make that ordering hard to get wrong.

**2. `follow(...)` finishes before the robot has stopped.** It ends when the robot reaches the path end *on paper*, which is slightly before it has actually settled there. Pedro holds the end pose, so put `RobotCommands.settle(robot)` between a `follow` and a shot.

**3. Pedro's wheel math uses the opposite sign for strafe and turn** than the old TeleOp did. `Robot.drive()` flips them (`STRAFE_SIGN`, `TURN_SIGN`), which makes the four wheel powers come out identical to the proven TeleOp. If a driver says an axis is backwards, that's the knob.

## Phases

### Phase 0: Agree and set up (coach) — done

- [x] Review this architecture and RULES.md
- [x] Clean up leftovers and move them to `archive/`

### Phase 1: Build the 90% (coach) — built, needs a robot

- [x] Build `common/` (Robot, Launcher, Intake, FlowerCatcher, ShotPreset, RobotCommands)
- [x] Rebuild ShootAndPark as `competition/auto/ShootAndParkAuto` using the new blocks
- [x] Add Auto / TeleOp / paths templates to each student folder
- [x] **Put real poses in from the Pedro Visualizer** -- `docs/PedroPathing/ShootAndPark.pp.json` -> `common/paths/ShootAndParkPaths`, mirrored for both alliances by `common/Alliance`
- [ ] **Test on the robot:** `OfficialDrive` must drive and shoot the same as `BackupPlainDrive`
- [ ] **Test on the robot:** `OfficialAutoRed` runs 3 times in a row, then check `OfficialAutoBlue`
- [ ] Tune `Launcher.SHOT_DIP_RPM` so the shot counter matches what you see leave the robot

### Phase 2: Every student builds one OpMode (students)

- [ ] Each student lays out a path in the Pedro Visualizer, exports it into their paths class, and builds an Auto in their own folder
- [ ] One or more students build a TeleOp variant (one-driver layout, auto-fire when READY, field-centric, hold-position)
- [ ] Students tune and choose the shot presets (`ShotPreset`), ready tolerance and rumble on the field, and record what they picked and why
- [ ] Peer review in pairs, then coach review before anything moves to `competition/`

### Phase 3: Score more in Auto

- [ ] Pickup cycle: intake on, follow to pollen, return, shoot again (the template already shows the pattern)
- [ ] Spin up while driving (`spinUp` before `follow`) so the wheel is ready on arrival
- [ ] Pick the best Auto for each alliance side; retire `BackupPark` once the official Auto is proven

### Phase 4: Polish and stretch goals

- [ ] Choose the Auto from the Driver Station during INIT, without redeploying
- [ ] Carry the pose from the end of Auto into TeleOp (the follower already tracks it; it needs saving across OpModes)
- [ ] TeleOp button that drives to a shooting spot with Pedro
- [ ] Ideas that prove useful move from student folders into `common/`

## Example: what a student's Auto looks like

```java
private Command autoRoutine() {
    return sequential(
            RobotCommands.shootVolley(robot, ShotPreset.AUTO_START, 3),
            RobotCommands.intakeOn(robot),
            follow(robot.follower, paths.toPollen()),
            RobotCommands.intakeOff(robot),
            RobotCommands.spinUp(robot, ShotPreset.WALL),   // spins up while driving
            follow(robot.follower, paths.backToShoot()),
            RobotCommands.settle(robot),                    // stop drifting, THEN shoot
            RobotCommands.fire(robot, 3, 4000),
            RobotCommands.launcherOff(robot),
            follow(robot.follower, paths.toPark())
    );
}
```

## Example: what a student's TeleOp looks like

```java
Robot robot = new Robot(hardwareMap);
waitForStart();

while (opModeIsActive()) {
    robot.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

    if (gamepad2.bWasPressed()) robot.launcher.setTarget(ShotPreset.WALL);
    robot.launcher.setFiring(gamepad2.left_bumper);

    robot.update();                 // follower + launcher, every loop
    robot.addTelemetry(telemetry);
    telemetry.update();
}
```
