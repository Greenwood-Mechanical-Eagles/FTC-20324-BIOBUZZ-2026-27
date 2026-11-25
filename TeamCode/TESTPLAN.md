# BioBuzz 20324: Bring-up test plan

How to find out what actually works, in an order where each step tests **one**
thing and a failure tells you *which* thing.

Nothing in `common/`, `competition/teleop/OfficialDrive` or the official Autos has ever
run on the robot. They compile. That is all we know.

## The rule that makes this worth doing

**Each gate must pass before the next one means anything.** Skipping ahead
doesn't save time, it costs it: if the shot counter is miscounting and you run
Auto, Auto fires the wrong number of pollen and you'll spend the afternoon
tuning paths to fix a launcher problem.

Record results as you go — the bottom of this file has a table. "It worked" with
no numbers can't be compared against next week.

## Two things that look like bugs and are not

Read these before you start, or you will debug them for an hour.

**1. Auto on a stand will stall at the first path, forever.** With the wheels off
the ground the odometry pod never moves, so the follower never believes it
reached the end of path 1, so it never moves on. That is correct behaviour.
It also makes the stand test *useful*: see gate 8.

**2. `Shots: 0` with `Launcher: FIRING (not at speed!)` is a signal, not a
fault.** It means the flywheel never reached its target, so the safety net fed
anyway and shot counting deliberately stayed off. The message is telling you the
preset is higher than this motor and battery can reach. Lower it.

---

## Gate 0 — Robot configuration names (5 min, no robot motion)

Every OpMode crashes at INIT if one name is wrong. Check the config on the
Driver Station against this list. These are the exact strings the code asks for.

| Name | Type | Used by |
| --- | --- | --- |
| `frontLeftDrive` | DcMotor | Pedro drive |
| `frontRightDrive` | DcMotor | Pedro drive |
| `backLeftDrive` | DcMotor | Pedro drive |
| `backRightDrive` | DcMotor | Pedro drive |
| `pinpoint` | odometry | Pedro localisation |
| `outtakeMotor` | DcMotorEx | `Launcher` flywheel |
| `outtakeServo` | CRServo | `Launcher` feeder |
| `intakeMotor` | DcMotor | `Intake` |
| `flower` | Servo | `FlowerCatcher` |
| `rewolf` | Servo | `FlowerCatcher` (mirror side) |

`outtakeMotor` **must** be a motor with its encoder cable plugged in. All RPM
control, and therefore all shot counting, depends on it.

**Quickest way to do this gate:** run `tools/Encoder Check`. It looks each motor
up separately and prints `NOT IN CONFIG` for any that is missing instead of
crashing, so it names the broken one rather than making you guess from a stack
trace. It also answers the encoder question in the same pass — turn each motor
by hand (or hold a bumper for the intake and flywheel) and watch the Ticks
column. Ticks moving means the encoder works; stuck on 0 means no cable.

**Fail** → a "device not found" style crash naming the missing device. Fix the
config, not the code.

---

## Gate 1 — INIT without crashing (10 min, wheels off the ground)

Press INIT (not PLAY) on each of: `1 - Official Drive`, `1 - Official Auto RED`,
`1 - Official Auto BLUE`.

**Proves:** config names are right, no `hardwareMap` call happens too early
(the crash the old `ShootAndPark` had), and the Pinpoint is alive — Pedro can't
initialise without it.

**Pass:** all three reach INIT and show telemetry.

### Free mirror test — do this now, it costs nothing

On the two Autos, read the `START pose` line at INIT:

| OpMode | Expected |
| --- | --- |
| `1 - Official Auto RED` | `x 56.0  y 8.0  heading 90 deg` |
| `1 - Official Auto BLUE` | `x 88.0  y 8.0  heading 90 deg` |

**Pass:** blue reads 88 where red reads 56, everything else identical.
That is the whole red↔blue mirror confirmed without moving the robot.

**Fail** → `common/Alliance.MIRROR_AXIS_X`.

---

## Gate 2 — Motor directions (10 min, wheels off the ground)

Run `tools/Motor Test TeleOp`. Gamepad 1: A = front left, B = front right,
X = back left, Y = back right. One wheel at a time.

**Proves** wiring independently of any drive maths. Do this before any driving
test, or you'll be guessing whether a reversed axis is code or a connector.

**Pass:** each button spins the named wheel, and all four spin the direction
that would drive the robot forward.

**Fail** → motor direction in the config or `pedro/Constants`. Coach only.

---

## Gate 3 — Flywheel and PIDF (20 min, on a stand, with pollen)

Run `tools/Flywheel PIDF Tuner`. All controls on gamepad 1.

1. **B** to spin up. Watch `RPM  xxxx  target yyyy`.
   **Pass:** reaches target and holds within ~50 RPM without hunting up and down.
   **Fail** → `Error (target - RPM)` stuck high means F too low; oscillating
   means P too high. Current values are P 1.75 / F 16.
2. **Hold A** for the full-power test. Read `Max ticks/sec` and `Suggested F`.
   **Pass:** `Suggested F` lands near 16. A wildly different number means the
   encoder is reporting something unexpected — stop and investigate, because
   every RPM number downstream is then wrong.
3. **Set a realistic target** (right/left bumper, ±100) and **hold right
   trigger** to feed several pollen.
4. Read **`Lowest RPM in last volley`**.

### Then set `SHOT_DIP_RPM`

```
SHOT_DIP_RPM  ≈  (target − lowest) / 2
```

Write it into `common/subsystems/Launcher.SHOT_DIP_RPM` (currently 150, an
untested guess). This single number decides whether Auto's "fire 3 pollen"
works at all, because `RobotCommands.fire()` finishes on the shot count.

---

## Gate 4 — Shot counting (20 min, on a stand, with pollen)

**This is the gate for Auto.** Run `1 - Official Drive`. Gamepad 2: **B** to
spin up to WALL, hold **left bumper** to fire. Feed ~10 pollen.

Watch `Shots` and count the pollen you actually see leave the robot.

**Pass:** they match exactly, over 10 pollen.

| Symptom | Meaning | Fix |
| --- | --- | --- |
| `Shots` climbs with no pollen | ordinary RPM wobble counts as a shot | raise `SHOT_DIP_RPM` |
| `Shots` misses pollen | dips too shallow to detect | lower `SHOT_DIP_RPM` |
| One pollen counts as several | wheel sags and recovers slowly | raise `SHOT_RECOVER_RPM` |
| `Shots` stays 0, state `FIRING (not at speed!)` | never reached target — see trap 2 | lower the preset |

Use `Deepest dip this volley` to pick the number rather than guessing; it prints
the measurement and the current setting side by side.

**Do not proceed to Auto until this is exact.**

---

## Gate 4b — Intake stall protection (10 min, on a stand)

New behaviour, so it needs proving once. The intake now watches its own encoder
and cuts power rather than pushing full power into a locked rotor.

Run `1 - Official Drive`, hold gamepad 2 **left trigger** to run the intake in,
and watch the `Intake` telemetry line.

1. **Normal running:** reads `in`, and `Intake speed` shows a healthy ticks/sec.
2. **Block it deliberately** — hold the intake so it cannot turn. Within about
   half a second it should read `STALLED - power cut, retrying`, and
   `Intake stalls` should start counting.
3. **Let go.** It should pick back up on its own within a second.

**Pass:** it cuts power when blocked, recovers when freed, and the stall count
reflects how many times you blocked it.

| Symptom | Fix |
| --- | --- |
| Stalls while running normally | `STALL_TICKS_PER_SEC` too high, or `STALL_GRACE_MS` too short |
| Never notices a hard block | `STALL_TICKS_PER_SEC` too low |
| Stutters when a pollen catches briefly | raise `STALL_GRACE_MS` |
| Recovers too slowly for a driver | lower `STALL_COOLDOWN_MS` |

Under a sustained block the motor ends up at roughly a 30% duty cycle (about
400 ms on, 1000 ms off), which is the whole point — it will sit there safely
rather than cooking.

Note `Intake stalls` only appears once it is non-zero, so a clean run shows
nothing. Seeing it climb during a match is the signal that something mechanical
needs attention.

---

## Gate 5 — TeleOp drive parity (20 min, on the field, wheels down)

The claim to test: `OfficialDrive` puts the same power on the wheels as the
TeleOp that ran at the meet. That's arithmetic, not evidence.

1. Run `9 - BACKUP Plain Drive` first. This is the proven one — it is the
   **baseline**. Note how forward, strafe and turn respond.
2. Run `1 - Official Drive`. Same stick inputs, back to back.

**Pass:** indistinguishable to the driver. Check each axis separately:
forward/back, strafe left/right, turn left/right.

| Symptom | Fix |
| --- | --- |
| strafe inverted | `Robot.STRAFE_SIGN` |
| turn inverted | `Robot.TURN_SIGN` |
| forward inverted | stop — that's a motor direction problem, back to gate 2 |

3. **Precision mode:** hold gamepad 1 left trigger **and push two sticks at
   once** (e.g. forward + turn). `Slow mode` should read `ON` and the robot
   should stay slow. This specifically tests the scaling order in
   `Robot.drive()`; getting it wrong cancels slow mode exactly when a driver
   needs it.

4. Press **Y** to flip drive direction, confirm `Drive mode: REVERSED` and that
   forward/strafe invert while **turn does not**.

---

## Gate 6 — Can we trust the odometry? (15 min, on the field)

Auto is 100% dependent on this and no amount of path tuning survives a bad pod.
Still in `1 - Official Drive`, watching the `Pose` line.

1. Drive forward a measured distance — two tiles, 48 in. Compare the pose change
   to a tape measure.
   **Pass:** within ~1 in over 48.
2. Drive a square and return to where you started, by eye.
   **Pass:** pose returns within ~2 in and ~5° of where it began.
3. Spin in place several full turns and stop facing the original direction.
   **Pass:** heading returns within a few degrees.

**Fail** → `pedro/Constants` pod offsets, or a loose pod. Stop here; Auto is
meaningless until this passes.

---

## Gate 7 — The shot, on the field (30 min)

Two unknowns here, and the first one is the number most likely to be wrong in
the whole project.

### 7a. `SHOOT_HEADING` — where the shot actually goes

The shooter is fixed and fires along robot-forward, so the robot's heading *is*
the aim. The exported 90° was never aimed at anything (see
`docs/PedroPathing/README.md`).

First establish the field origin: open `docs/PedroPathing/ShootAndPark.pp.json`
in the Visualizer and identify which physical corner is (0,0), then mark it.
Every number below depends on it. Sanity check: the red start is 8 in off the
wall and 16 in to one side of field centre.

1. Place the robot by hand at the red shoot pose: **(56.3, 31.1)**.
2. Run `1 - Official Drive`, spin up, and fire a few pollen.
3. **Where do they go?**

Then — rather than trusting either 90 or 102.8 — **rotate the robot by hand
until it scores, and read the heading straight off the `Pose` telemetry line.**
That measured number is what belongs in
`common/paths/ShootAndParkPaths.SHOOT_HEADING`.

It's one constant and it fixes both alliances at once.

### 7b. `ShotPreset.AUTO_START` — the opening shot speed

Still at the shoot pose. 2900 RPM is a guess derived from an old raw-power
number. Nudge with gamepad 2 **D-pad up/down** (±100) until pollen score
reliably, then write that value into `common/subsystems/ShotPreset`.

### 7c. Re-check `CORNER`

It was changed from 3600 (what ran at the meet) to 3500 to respect the
"motor holds speed through a volley" limit. Shoot from the corner. If it comes
up short, put 3600 back — that commit is deliberately on its own so it reverts
cleanly.

---

## Gate 8 — Autonomous

### 8a. On a stand first (10 min)

Run `1 - Official Auto RED` with the wheels off the ground.

Expect it to **stall on path 1 forever** (trap 1). That stall is the test:

| What you see | Meaning |
| --- | --- |
| `Path number` stays 0, `Inches to end of path` roughly constant, wheels turning | **correct.** `robot.update()` is running before `Scheduler.execute()` |
| `Path number` jumps to the last path in a fraction of a second | **the ordering bug.** Auto is racing through every path doing nothing |

Also confirm the wheels turn in the direction that *would* drive it toward the
shooting spot.

### 8b. On the field, one run, hand on STOP (20 min)

Place the robot on the START pose. Watch for:

1. drives forward off the wall and stops at the shooting spot
2. settles, then fires — `Shots` reaches 3
3. backs across to the corner, then up the field to park
4. ends inside the park zone

Stop it the moment anything looks wrong and work out which gate the failure
belongs to rather than changing numbers at random.

### 8c. Three consecutive identical runs

Only after 3 for 3 does this replace `2 - BackupPark` in matches. Record the end
position each time — "it parked" three times with 10 in of scatter is not the
same result three times.

### 8d. Then `1 - Official Auto BLUE`

Same run, mirrored. Because both alliances run identical code, this is really a
test of the field numbers and the mirror axis, not of the Auto logic.

**Pass:** same shape of run on the other side, scoring and parking equivalently.
**Fail, consistently offset by an inch or two** → `Alliance.MIRROR_AXIS_X`
(currently 72; the export's hand-drawn shapes imply 71.25).

---

## Gate 9 — Failure drills (20 min)

The things that will actually happen at an event. Worth proving once in a
practice session rather than discovering in a match.

1. **Odometry dies.** Unplug the Pinpoint. `1 - Official Drive` should fail to
   start — and `9 - BACKUP Plain Drive` should still drive perfectly. This is
   the entire reason that OpMode exists and shares no code with the new stack.
2. **Feeder jam.** Jam a pollen deliberately, press gamepad 2 **right bumper**
   to run the feeder backwards and clear it.
3. **Tired battery.** Run the presets on a low battery. Confirm telemetry says
   `WHEEL TOO SLOW - not feeding, lower the preset` rather than silently doing
   nothing. A launcher that quietly refuses to fire is our worst failure mode,
   because from the driver's seat it looks identical to a jam.

---

## Record results here

| Date | Gate | Result | Numbers measured |
| --- | --- | --- | --- |
| | 3 | | `Suggested F` = , lowest RPM = , `SHOT_DIP_RPM` set to |
| | 4 | | pollen fired = , `Shots` = |
| | 6 | | 48 in drive read as , square return error = |
| | 7a | | measured scoring heading = |
| | 7b | | `AUTO_START` set to |
| | 8b | | |
| | 8c | | run 1 / 2 / 3 park positions |
| | 8d | | |

## Fastest useful subset

If you only get 30 minutes with the robot, do gates **0, 1, 3 and 4**. All of it
is on a stand, none of it needs a field, and it settles the two numbers
everything else depends on (`SHOT_DIP_RPM`) plus the whole red/blue mirror
(gate 1's pose readout).
