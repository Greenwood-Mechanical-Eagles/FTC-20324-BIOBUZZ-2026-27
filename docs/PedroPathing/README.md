# Pedro Visualizer paths

Exports from the [Pedro Visualizer](https://pedropathing.com/) live in this
folder, next to the code that uses them. Keeping the `.pp.json` means you can
reopen a path a month later and move one point, instead of laying the whole
thing out again from scratch.

| File | Used by | Status |
| --- | --- | --- |
| `ShootAndPark.pp.json` | `common/paths/ShootAndParkPaths` → `competition/OfficialAutoRed` / `OfficialAutoBlue` | **Not yet driven.** See the two notes at the bottom |

## Workflow

1. Lay the path out in the Visualizer, on the **red** side, and run it there.
2. Export it and drop the `.pp.json` in this folder.
3. Put the numbers into a paths class — `common/paths/` if both alliances use it,
   or `students/<you>/<You>Paths.java` if it's yours.
4. The Auto calls `paths.startToShoot()` and never mentions a number.

Always lay paths out on the **red** side. Blue comes free — see below.

## Red and blue from one set of numbers

Every pose is typed once, for red. `common/Alliance` flips it for blue:

```
x        ->  144 - x      (mirrored across x = 72, the middle of the field)
y        ->  unchanged     (both alliances start at the same wall)
heading  ->  180 - heading
```

`Alliance.poses()` hands back a Pedro `PoseFactory` that has the flip baked in,
so a paths class just asks for red numbers and gets the right side out:

```java
PoseFactory poses = alliance.poses();
START = poses.of(56, 8.0197, 90);     // red: (56, 8.02, 90)
                                      // blue: (88, 8.02, 90)
```

**Never type a second set of blue numbers.** One set of numbers means re-measuring
the field fixes both sides at once; two sets means they quietly drift apart and
only one of them gets tested.

`PoseFactory` is immutable — `mirrorX()` returns a new factory — so blue can
never corrupt red.

### The mirror axis

`Alliance.MIRROR_AXIS_X` is **72**, the exact middle of a 144 × 144 in field.

The HIVE shapes in `ShootAndPark.pp.json` actually imply 71.25: the red HIVE is
drawn 24.7 in from centre and the blue one 23.2 in. Those shapes were dragged
into place by hand in the Visualizer and aren't measured field data, so 72 is
the number to trust. If blue is consistently ~1.5 in off while red is good,
`MIRROR_AXIS_X` is the one constant to change.

## What this path does

Three segments, exactly as exported:

| # | Name | From → to | Heading |
| --- | --- | --- | --- |
| 1 | `MoveToShoot` | (56, 8.02) → (56.27, 31.11) | holds 90° |
| 2 | `ShootToCorner` | → (18.92, 30.90) | reverse tangent (~0.3°) |
| 3 | `CornerToPark` | → (14.69, 94.49) | reverse tangent (~273.8°) |

The Auto drives segment 1, settles, fires the opening volley, then drives
segments 2 and 3 to park. Segments 2 and 3 are driven backwards.

Both alliances come out in bounds with the same wall clearances (checked against
a 16 × 16 in robot; the tightest is the start pose, whose bumper sits on the
y = 0 wall as you'd expect for a wall start).## The shooting heading almost certainly needs changing

**Confirmed 2026-11-25: the shooter is fixed to the chassis and fires along
robot-forward.** So the robot's heading at the shooting spot *is* the direction
the pollen go. There is no turret to make up the difference.

That makes `SHOOT_HEADING` the most important number in
`common/paths/ShootAndParkPaths`, and the exported 90° looks wrong:

- The shooting **position** (56.27, 31.11) is good — 40.6 in from the HIVE with
  a clear line. Laid out and simulated in the Visualizer. Not in question.
- The **heading** of 90° was never aimed at anything. The export's segment 1
  interpolates heading `linear 90 -> 90`, so it carries the wall start's 90°
  straight through. Nothing in the path asks the robot to turn.
- Shooting on heading 90° from that spot sends the pollen along the line
  x = 56.3, straight up the field. The red HIVE is drawn at x = 46–48.6. That
  line runs **parallel to the HIVE wall, about 8 in to its side, and never
  crosses it** — it isn't a near miss that lands short or long, it goes past.
- `AIM_AT_HIVE_HEADING` = **102.8°** is the heading that points at the middle of
  the drawn HIVE (a 12.8° turn). Blue mirrors to 10.7°.

**Still worth confirming before changing it**, because the one remaining unknown
is whether the HIVE is drawn where the real HIVE sits. Those shapes were dragged
in by hand and are not self-consistent — red and blue disagree about the field
centre by 1.5 in (see the mirror axis note above). If the drawn rectangle is only
the HIVE's back panel and the real scoring opening is wider or further out, 90°
may clip it after all.

So: put the robot on the shoot pose and fire a few pollen. Then set
`SHOOT_HEADING` to whatever actually scores. It's one constant and it fixes both
alliances at once — no re-export needed.

`START_HEADING` (90°) is kept separate from `SHOOT_HEADING` on purpose: the start
heading is a physical measurement of how the robot sits against the wall, while
the shoot heading is an aiming choice. Segment 1 turns from one to the other on
the way up the field, so re-aiming the shot never implies the robot started the
match rotated.

## Known and accepted: the robot turns at each seam

Segment 1 holds a fixed heading; segments 2 and 3 take theirs from the direction
of travel, so the robot turns ~89.7° at the seam after the shot and ~86.5° at
the next one. Nothing collides.

It costs a couple of seconds, and the budget absorbs it comfortably:

| | Worst case |
| --- | --- |
| `settle` before the shot | 1.0 s |
| spin up (`SPIN_UP_TIMEOUT_MS`) | 3.0 s |
| volley (`VOLLEY_TIMEOUT_MS`) | 4.0 s |
| driving, 124 in total | ~4 s |
| two seam turns | ~2 s |
| **total** | **~14 s of 30** |

The volley ends on shot count rather than the clock, so it normally finishes well
inside that. If Auto ever does run short, giving segments 2 and 3 a constant
heading is the first thing to try.
