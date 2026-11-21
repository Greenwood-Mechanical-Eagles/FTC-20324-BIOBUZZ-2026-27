# BioBuzz 20324: Team Code Rules

**Status: proposed.** These rules let several students work in one project on one shared computer without breaking each other's code.

## Branch and folders

The team uses **one branch**. Each student works in **their own folder**.

```
teamcode/
  common/          shared blocks: maintained by the coach, students call but don't edit
    subsystems/    Robot, Launcher, Intake, FlowerCatcher, ShotPreset
    commands/      RobotCommands
  students/
    ethan/         Ethan's Autos, TeleOps and paths
    ryder/         Ryder's Autos, TeleOps and paths
  competition/     only OpModes that passed the testing checklist
  pedro/           Pedro config and tuning (don't change without the coach)
```

1. **Work only in your own folder.** Don't edit another student's files or `common/`.
2. **Copy a template; don't start from nothing.** Each student folder has an Auto, a TeleOp and a paths template.
3. **Build before you commit.** One broken file stops everyone's build. If Android Studio shows red, fix it or don't commit it.
4. **Mark unfinished work `@Disabled`.** It still builds but stays off the Driver Station.
5. **Put your name on every OpMode:** `@Autonomous(name = "Ethan: Red Close 3", group = "Ethan")`.
6. **Changing `common/` goes through the coach.** A good idea from your folder gets reviewed and then moved into `common/`.
7. **Only touch files under `TeamCode/`.** The FTC SDK (`FtcRobotController`, Gradle files) is updated by the coach only.

## Writing code

| Rule | Why |
| --- | --- |
| Hardware is only used through `Robot` (`robot.launcher`, `robot.intake`...) | Fix a mechanism once, and every OpMode gets the fix |
| Speeds, powers and directions live in one place (`ShotPreset`, the SETTINGS block in each subsystem) | Tune once, and Auto and TeleOp both follow |
| Never call `hardwareMap.get` outside `init()` / `runOpMode()` | `hardwareMap` is empty before that; it crashes on INIT |
| Create the `Robot` in `init()` / `runOpMode()` | Same reason |
| Call `robot.update()` every loop | Otherwise the drive, flywheel and feeder do nothing, and Auto commands never finish |
| Class names in PascalCase (`RedCloseThreeShot`) | Easy to read; `backupauto` becomes `BackupPark` |
| Comment the *why*, not the *what* | `// 2900 RPM scores from the start tile`, not `// set rpm` |

## Paths from the Pedro Visualizer

1. Lay out the path in the Pedro Visualizer and test it there.
2. Export it and paste it into the paths class in your own folder (for example `students/ethan/EthanPaths.java`).
3. Your Auto calls `follow(robot.follower, paths.toPollen())`, so path numbers stay out of the Auto itself.
4. The start pose in code must match where the robot really sits on the field.
5. Paths everyone uses (start spots, park spots) move to `common/` once they're proven on the field.

## Testing checklist (before anything moves to `competition/`)

- [ ] Builds in Android Studio with no errors
- [ ] INIT runs with no crash, and telemetry shows sensible values
- [ ] Runs 3 times in a row on the practice field with the same result
- [ ] A second student has read the change
- [ ] Committed with a message that says what changed and why

## Match day

- Only OpModes in `competition/` run in matches.
- The match-proven TeleOp and a plain backup TeleOp always stay in `competition/`.
- No code changes at an event without the coach.
