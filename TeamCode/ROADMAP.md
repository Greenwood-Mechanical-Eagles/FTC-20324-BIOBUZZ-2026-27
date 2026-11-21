# BioBuzz 20324: Code Roadmap

**Status: proposal for review.** Nothing here is in the code yet.

## Goal

Give students building blocks that do about 90% of the work, so each student can build their own Auto or TeleOp and spend their time on strategy ideas instead of plumbing. Shooting, driving and mechanisms work the same way in every OpMode.

## Where we are (2026-11-21)

| File | Type | Status |
| --- | --- | --- |
| `teleop/RapidFireDrive` | TeleOp | Tested. RPM flywheel presets, rapid fire, intake, flower catcher |
| `teleop/FlywheelPIDFTuner` | Tuning | Tunes P and F. Team values: P = 1.75, F = 16 (the tuner's starting F is still 18) |
| `autonomous/ShootAndPark` | Auto (Pedro) | Crashes on INIT (`hardwareMap` used too early) |
| `autonomous/backupauto` | Auto (Pedro) | Simple park, about 17 in. |
| `autonomous/launchAuto` | Auto (timed) | Old `sleep()` style; retire later |
| `autonomous/util/ShootBallCommandBuilder` | Helper | Shoots on raw power, not RPM |
| `pedro/Constants` | Config | Pedro 3 + Pinpoint, tuned |

The main problem today: TeleOp shoots by RPM and Auto shoots by raw power, so the two behave differently.

## Proposed architecture

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

- **Robot**: one object that holds every mechanism. `new Robot(hardwareMap)` once, then `robot.update()` every loop.
- **Pedro Follower** (inside Robot) drives in both modes: `follow(robot.follower, path)` in Auto, `robot.drive(forward, strafe, turn)` in TeleOp through Pedro's manual drive. That means one drive setup, one set of tuning, and a robot pose that carries from Auto into TeleOp (enabling field-centric and hold-position).
- **Launcher** is the only place that knows how to shoot. Both modes shoot the same way.
- **ShotPreset** is one list of RPMs that both modes use.
- **RobotCommands** are Ivy commands for Auto, snapped together with `sequential(...)`.
- **No interfaces (strategy pattern) for now.** With one launcher there is nothing to swap. Revisit if a second shooter design or a simulator appears.
- **Fallback:** a plain `setPower` TeleOp stays in `competition/` in case odometry fails during a match.

## Phases

### Phase 0: Agree and set up (coach)

- [ ] Review this architecture and RULES.md; settle the decisions in TODO.md
- [ ] Clean up leftovers and pick the one team branch

### Phase 1: Build the 90% (coach)

- [ ] Build `common/` (Robot, Launcher, Intake, FlowerCatcher, ShotPreset, RobotCommands)
- [ ] Fix ShootAndPark using the new blocks
- [ ] Add templates to each student folder
- [ ] Test on the robot: the new TeleOp must drive and shoot the same as RapidFireDrive

### Phase 2: Every student builds one OpMode (students)

- [ ] Each student lays out a path in the Pedro Visualizer, exports it, and builds an Auto in their own folder
- [ ] One or more students build a TeleOp variant (one-driver layout, auto-fire when READY, field-centric, hold-position)
- [ ] Students tune and choose the shot presets (`ShotPreset`), ready tolerance and rumble on the field, and record what they picked and why
- [ ] Peer review in pairs, then coach review before anything moves to `competition/`

### Phase 3: Score more in Auto

- [ ] Pickup cycle: intake on, follow to pollen, return, shoot again
- [ ] Spin up while driving (`spinUp` before `follow`) so the wheel is ready on arrival
- [ ] Pick the best Auto for each alliance side; retire `launchAuto`

### Phase 4: Polish and stretch goals

- [ ] Choose the Auto from the Driver Station during INIT, without redeploying
- [ ] TeleOp button that drives to a shooting spot with Pedro
- [ ] Ideas that prove useful move from student folders into `common/`

## Example: what a student's Auto looks like

```java
public Command autoRoutine() {
    return sequential(
            RobotCommands.shootVolley(robot, ShotPreset.AUTO_START, 2000),
            RobotCommands.intakeOn(robot),
            follow(robot.follower, paths.toPollen()),
            RobotCommands.intakeOff(robot),
            follow(robot.follower, paths.backToShoot()),
            RobotCommands.shootVolley(robot, ShotPreset.WALL, 2500),
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
