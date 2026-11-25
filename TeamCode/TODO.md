# BioBuzz 20324: TODO

See ROADMAP.md for the plan and RULES.md for the team rules.

Last updated: 2026-11-25

## Blocking — nothing else matters until these are done

- [x] **Real poses for the official Auto.** Done — laid out in the Pedro Visualizer on 2026-11-25, export kept in `docs/PedroPathing/ShootAndPark.pp.json`, numbers in `common/paths/ShootAndParkPaths`. Red and blue come from the one set of red numbers (`common/Alliance` mirrors them). Still **not driven**, so keep running `competition/auto/BackupPark` in matches until it goes 3 for 3.
- [ ] **Decide the Auto opening shot speed.** `ShotPreset.AUTO_START` is 2900 RPM, which was worked out from "the old auto shot at -0.75 power". But the only Auto that ever ran (`archive/launchAuto`) used `setPower(-0.5)`, nearer 2000 RPM, and it didn't work anyway. So 2900 is a guess. Easiest check: in `OfficialDrive`, stand the robot on the start tile, nudge the speed with the gamepad 2 D-pad until pollen go in, then write that number into `ShotPreset`.
- [ ] **Tune `Launcher.SHOT_DIP_RPM`** (currently 150). Run `tools/FlywheelPIDFTuner`, hold the right trigger for a few shots, read "Lowest RPM in last volley", and set this to about half of (target − lowest). Then check in `OfficialDrive` that the "Shots" number goes up by exactly one per pollen. The Auto fires "3 pollen" rather than "feed for 2 seconds", so this number decides whether it works.

## Robot testing, in this order

- [ ] `competition/teleop/OfficialDrive` — does it drive and shoot the same as `BackupPlainDrive`? Check forward, strafe and turn directions first. The wheel powers should be identical, but that's arithmetic, not a test.
- [ ] `competition/teleop/OfficialDrive` — does telemetry "Shots" count correctly?
- [ ] `competition/auto/OfficialAutoRed` on a stand first, watching telemetry, before letting it drive.
- [ ] **Fix the shooting heading.** The shooter is fixed and fires along robot-forward (confirmed), so `ShootAndParkPaths.SHOOT_HEADING` is literally where the pollen go. It's 90°, which the export carried over from the wall start without aiming at anything — that line runs parallel to the HIVE wall about 8 in to its side and never crosses it. `AIM_AT_HIVE_HEADING` (102.8) aims at the HIVE as drawn. Confirm on a stand first: the HIVE shapes in the export were placed by hand and aren't self-consistent, so measure rather than trust 102.8. One constant, fixes both alliances. Write-up in `docs/PedroPathing/README.md`.
- [ ] `competition/auto/OfficialAutoRed` on the field, 3 times in a row with the same result.
- [ ] `competition/auto/OfficialAutoBlue` on the field — confirm the mirror is right (same shape of run, other side).

## Decisions (coach)

- [x] Architecture agreed: `Robot` owns the Pedro follower, launcher, intake and flower catcher; Auto uses `RobotCommands`
- [x] Flywheel PIDF: **P = 1.75, F = 16**. `common/subsystems/Launcher` and `tools/FlywheelPIDFTuner` now agree
- [x] Old files moved to `archive/` and `@Disabled`
- [x] The proven TeleOp survives untouched as `competition/teleop/BackupPlainDrive`, for the day the odometry pod dies
- [ ] Shot presets, ready tolerance and rumble are **for the students to tinker with and choose** (ROADMAP Phase 2). Current starting points, from the TeleOp that ran at the meet (except CORNER, which ran as 3600 and was capped to 3500 to stay inside the "motor holds speed through a volley" limit — check it still scores from the corner):

  | Setting | Value | Where |
  | --- | --- | --- |
  | Wall / Line / Corner RPM | 2800 / 2600 / 3500 | `common/subsystems/ShotPreset` |
  | Ready tolerance | ±50 RPM | `Launcher.RPM_TOLERANCE` |
  | Ready rumble | 2000 ms | `OfficialDrive.READY_RUMBLE_MS` |

- [ ] Pick the one team branch (`leslie-coach`, `tking/Working`, or this one). Everything above is on `tking/ProposedArchitecture`
- [ ] Confirm the student list (folders so far: `ethan`, `ryder`)

## Things to check

- [x] Pedro's TeleOp drive is in our version — `Follower.manual(forward, lateral, heading)` exists in Pedro 3.0.1 and `Robot.drive()` uses it
- [x] Get one exported file from the Pedro Visualizer — `docs/PedroPathing/ShootAndPark.pp.json`. Format is `.pp.json`; `common/paths/ShootAndParkPaths` shows how it maps into code
- [ ] Are encoder cables plugged into all 4 drive motors? (Only matters if we ever want drive velocity control. The *flywheel* encoder is definitely plugged in, or RPM control in TeleOp wouldn't have worked at the meet.)
- [ ] First Gradle sync on a fresh computer needs the internet: `com.pedropathing.ivy:pedro:1.1.1` comes from Maven Central and isn't cached in the repo

## Still to clean up

- [ ] Merge `ryder/working` and `ryder/Working` into one branch (Windows treats them as the same name)
- [ ] Check the old StudioProjects folders `20324 2026-2027 Biobuzz` and `30324 2026-2027 Biobuzz`; archive if unused
- [ ] `students/ryder/RyderIntakeTest.java` — Ryder's old intake test, moved into his folder. Keep or delete, his call
- [ ] `tools/XMovementTest` and `tools/YMovementTest` still use the old `sleep()` style. Fine as diagnostics; rewrite or delete eventually
