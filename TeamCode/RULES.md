# BioBuzz 20324: Team Code Rules

These rules let several students work in one project on one shared computer without breaking each other's code.

## Branch and folders

The team uses **one branch**. Each student works in **their own folder**.

```
teamcode/
  common/          shared blocks: maintained by the coach, students call but don't edit
    subsystems/    Robot, Launcher, Intake, FlowerCatcher, ShotPreset
    commands/      RobotCommands
    paths/         field numbers shared by both alliances (ShootAndParkPaths)
    Alliance       RED / BLUE, and the red->blue field flip
  students/
    ethan/         Ethan's Autos, TeleOps and paths
    ryder/         Ryder's Autos, TeleOps and paths
  competition/     only OpModes that passed the testing checklist
    auto/          match Autos
    teleop/        match TeleOps
  pedro/           Pedro config and tuning (don't change without the coach)
  tools/           tuners and diagnostics (flywheel PIDF, motor direction test)
  training/        lessons
  archive/         old code, all @Disabled. Don't copy from here
```

Pedro Visualizer exports live outside `TeamCode/`, in `docs/PedroPathing/`, next
to a README explaining the red/blue flip.

OpModes in `competition/` are numbered so they sort to the top of the Driver
Station list: `1 - Official Auto RED`, `1 - Official Auto BLUE`,
`1 - Official Drive`, `2 - BackupPark`, `9 - BACKUP Plain Drive`.

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
| In Auto, `robot.update()` comes **before** `Scheduler.execute()` | The other order makes Auto race through every path doing nothing, with no error message |
| Never drive the wheels anywhere except through `robot.drive()` or `follow(robot.follower, ...)` | Two bits of code setting the same four motors in one loop makes the robot twitch |
| Put `RobotCommands.settle(robot)` between a `follow(...)` and a shot | `follow` finishes just before the robot has actually stopped moving |
| Class names in PascalCase (`RedCloseThreeShot`) | Easy to read; `backupauto` becomes `BackupPark` |
| Comment the *why*, not the *what* | `// 2900 RPM scores from the start tile`, not `// set rpm` |

## Paths from the Pedro Visualizer

1. Lay out the path in the Pedro Visualizer and test it there. **Always lay it out on the red side.**
2. Save the export into `docs/PedroPathing/`, so the path can be reopened and nudged later instead of rebuilt from scratch.
3. Paste the numbers into the paths class in your own folder (for example `students/ethan/EthanPaths.java`).
4. Your Auto calls `follow(robot.follower, paths.toPollen())`, so path numbers stay out of the Auto itself.
5. The start pose in code must match where the robot really sits on the field.
6. Paths everyone uses (start spots, park spots) move to `common/paths/` once they're proven on the field.

**Type red numbers only.** `common/Alliance` flips a red pose to blue for you
(`alliance.poses().of(56, 8, 90)`), so one set of numbers covers both sides.
Two sets drift apart and only one of them ever gets tested. `common/paths/ShootAndParkPaths`
is the worked example; `docs/PedroPathing/README.md` explains the flip.

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
