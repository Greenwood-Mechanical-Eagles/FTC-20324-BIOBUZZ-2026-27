# BioBuzz 20324: TODO

What's still needed before the proposed architecture is built. See ROADMAP.md for the plan and RULES.md for the proposed team rules.

Last updated: 2026-11-21

## Decisions (coach)

- [ ] Agree on the architecture: `Robot` owns the Pedro follower, launcher, intake and flower catcher; Auto uses `RobotCommands`
- [ ] Pick the launcher numbers. The two copies of `RapidFireDrive.java` disagree:

  | Setting | `teleop/RapidFireDrive.java` | `RapidFireDrive.java` (project root) | `FlywheelPIDFTuner.java` |
  | --- | --- | --- | --- |
  | P | 1.75 | 1.75 | 1.75 |
  | F | 16 | 18 | 18 |
  | Wall / Line / Corner RPM | 2800 / 2600 / 3600 | 2600 / 2400 / 3400 | n/a |
  | Ready tolerance | ±50 RPM | ±150 RPM | n/a |
  | Ready rumble | 2000 ms | 200 ms | n/a |

- [ ] Pick the one team branch (`leslie-coach` or `tking/Working`)
- [ ] Confirm the student list (folders so far: `ethan`, `ryder`)
- [ ] Decide whether the "one branch, folder per student" rule is adopted

## Things to check

- [ ] Confirm Pedro's TeleOp drive is in our version: press Shift twice in Android Studio, search `ManualDrive` (include non-project items)
- [ ] Get one exported file from the Pedro Visualizer, so the paths template matches it
- [ ] Are encoder cables plugged into all 4 drive motors? (Only matters if we ever want drive velocity control)

## Cleanup

- [ ] Delete leftovers from the rejected first attempt:
  - `teamcode/subsystems/` (6 files)
  - `teamcode/commands/`
  - `teamcode/teleop/RapidFireDriveBlocks.java`
- [ ] Delete or archive the extra `RapidFireDrive.java` in the project root once its numbers are decided (Android Studio does not build it)
- [ ] Merge `ryder/working` and `ryder/Working` into one branch (Windows treats them as the same name)
- [ ] Check the old StudioProjects folders `20324 2026-2027 Biobuzz` and `30324 2026-2027 Biobuzz`; archive if unused

## Build (coach, the "90%")

- [ ] Create the folders: `common/subsystems`, `common/commands`, `students/ethan`, `students/ryder`, `competition`
- [ ] `common/subsystems/ShotPreset` (enum of RPMs)
- [ ] `common/subsystems/Launcher` (logic moved from RapidFireDrive: boost, RPM hold, feed only at speed)
- [ ] `common/subsystems/Intake`, `FlowerCatcher`
- [ ] `common/subsystems/Robot`: follower + launcher + intake + catcher, `update()`, `drive()`, `stopAll()`, telemetry
- [ ] `common/commands/RobotCommands`: `spinUp`, `waitUntilReady`, `fire`, `shootVolley`, `launcherOff`, `intakeOn/Off`, `catcherUp/Down`, `waitMs`
- [ ] Carry the robot pose from the end of Auto into TeleOp
- [ ] Fix ShootAndPark's INIT crash (`hardwareMap.get` runs before `init()`)
- [ ] Templates: one Auto, one TeleOp and one paths class in each student folder
- [ ] `competition/`: move in RapidFireDrive (proven) and a plain `setPower` backup TeleOp
- [ ] Build in Android Studio and test on the robot before students start
