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
- [ ] **Confirm the shooting heading on a stand.** The shooter fires out the BACK (measured 2026-11-25, encoded as `Launcher.AIM_OFFSET_DEG = 180`), so pollen leave along `SHOOT_HEADING + 180` and the robot must face *away* from the HIVE. `SHOOT_HEADING` is now 282.8, derived from the HIVE as drawn (`102.8 - 180`), which puts RED dead on and BLUE within 1.5 in. The old 90 was sending shots 167 degrees off — almost exactly backwards. Still verify: put the robot on the shoot pose, fire, rotate by hand until it scores, read the heading off telemetry, write that number in.
- [x] **Auto is now one motion, no turning.** Robot is placed facing `START_HEADING` (= `SHOOT_HEADING`, 282.8) with the shooter already aimed up the field; all three legs hold that one heading, so it reverses off the wall, fires, and reverses on to park. Replaced the version that spun 167 degrees at the shooting spot.
- [ ] **Watch the middle leg on the field.** Holding one heading makes the 37 in trip toward the corner almost pure sideways crab (77 degrees off the shooter). Mecanum handles it and it costs ~13% in speed, but if it drifts, move PARK to about (42, 94.5) in the Visualizer and the robot can reverse straight from the shooting spot to park, dropping that leg. Backing straight up currently lands at x~42 against PARK's x=14.7 — that 27 in gap is the only reason the corner leg exists.
- [ ] `competition/auto/OfficialAutoRed` on the field, 3 times in a row with the same result.
- [ ] `competition/auto/OfficialAutoBlue` on the field — re-test after the transform fix. The alliance flip was a left-right mirror and that was **wrong**; on 2026-11-25 red ran close but blue crabbed right where it should have crabbed left and hit the teeter totter. It is now a 180° rotation about the field centre, which starts blue at the far wall (y=136, not y=8) and keeps the handedness. Cheapest check is at INIT, no driving: blue must read `y 136.0  heading 103`.

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
- [x] **Every motor on the robot has an encoder** — 4 drive, flywheel and intake. The flywheel's runs RPM control and shot counting; the intake's now runs stall protection. The 4 drive ones are unused: `pedro/Constants` sets only motor names and directions, position comes entirely from the Pinpoint pods, and the only `RUN_USING_ENCODER` / `setVelocity` calls in the project are on the flywheel. Two things follow — drive velocity control is available whenever we want it (ROADMAP Phase 4), and a failed drive encoder cable costs us nothing today, so it is not a thing to suspect when the robot misbehaves.
- [ ] First Gradle sync on a fresh computer needs the internet: `com.pedropathing.ivy:pedro:1.1.1` comes from Maven Central, and `com.acmerobotics.dashboard:dashboard:0.6.0` from `maven.brott.dev`. Neither is cached in the repo. Sync at home, not at an event.
- [x] **FTC Dashboard added** — live constant tuning at `http://192.168.43.1:8080/dash`. `Launcher`, `Intake`, `Robot`, `RobotCommands` and `ShootAndParkPaths` are `@Config`. Note dashboard 0.6.0 is published against SDK 10.0.0, so the dependency excludes `org.firstinspires.ftc` to stop it pulling older SDK artifacts in beside our 12.0.0 — verified all eight stay at 12.0.0. Path headings need a re-INIT to apply; see TESTPLAN.

## Still to clean up

- [ ] Merge `ryder/working` and `ryder/Working` into one branch (Windows treats them as the same name)
- [ ] Check the old StudioProjects folders `20324 2026-2027 Biobuzz` and `30324 2026-2027 Biobuzz`; archive if unused
- [ ] `students/ryder/RyderIntakeTest.java` — Ryder's old intake test, moved into his folder. Keep or delete, his call. **If it's kept it needs a fix:** line 33 asks for a motor called `"Motor"`, while every other file in the project uses `"intakeMotor"`. It is `@Disabled` so it's harmless now, but it will crash at INIT the moment that line is removed.
- [ ] `tools/XMovementTest` and `tools/YMovementTest` still use the old `sleep()` style. Fine as diagnostics; rewrite or delete eventually
- [ ] **Measure `Intake.TICKS_PER_REV`.** It is still 28 (a bare REV HD Hex), which is almost certainly wrong for a geared intake — a 40:1 cartridge makes it 1120. Run `tools/Encoder Check`, hold the left bumper, read ticks/sec, and work back from the motor's spec. Until this is right the intake RPM *display* is wrong, but stall protection is unaffected because it only asks "is it turning at all", which needs no calibration.
- [ ] **Decide whether the intake should hold an RPM.** `Intake.TARGET_RPM` is 0, which means full power, open loop — the proven behaviour. Setting it above 0 holds a steady speed and makes the "STRUGGLING" jam warning meaningful. Measure free speed with `tools/Encoder Check` first and pick something under it. Note closed loop is not gentler: the controller pushes *harder* under load to hold the number.
