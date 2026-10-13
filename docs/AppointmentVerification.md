---
  layout: default.md
  title: "Appointment verification"
---

# Appointment verification

Verification for issue #83 was performed on 13 October 2026, on branch `83-appointment-docs-verification`,
starting from upstream commit `3839df6121a010b52abefaad17fdce1cbe858f9d` (merged PRs #85 and #86).
The environment was Windows, Temurin JDK `25.0.4.1`, Gradle `9.6.1`, and the `Asia/Singapore` system time zone.
The checks below include the working-tree documentation and integration tests for #83.

Automated and scripted UI checks passed. A final human keyboard/window check is still pending;
the results below must not be treated as a completed manual GUI run.

## Automated checks

* The focused appointment command/parser, date/time value, client, edit-preservation, command-dispatch,
  and JSON storage tests passed.
* `./gradlew.bat check coverage javadoc shadowJar` passed: 368 tests, no failures, errors, or skipped tests;
  both main and test Checkstyle checks passed, and the runnable JAR was built.
* `bash .github/run-checks.sh` and `git diff --check` passed.
* Javadoc generation passed after fixing the unresolved `Command` link in `CommandException`.
  The repository still emits missing-comment/tag warnings in existing APIs; it is not warning-free.

Eight new `AppointmentIntegrationTest` cases exercise real command dispatch, model updates, automatic saving,
and loading through a fresh JSON reader. They cover client association, shared slots across clients, rejected
input, duplicates, filtered indices, contact edits, deletion/clear, and an I/O save failure.
The rejected-input tests check both model equality and unchanged JSON file contents.

Existing tests cover leap years (including century rules), year limits `0001` and `9999`, strict time formats,
null arguments and list entries, immutable appointment collections, equality/hash consistency, and duplicate
slots with different details. Fixed-clock command tests cover past, exact-current, and future same-day times.
Storage tests cover legacy files without appointments, past appointments, invalid stored date/time values,
and duplicate stored slots.

JaCoCo reports full line and branch coverage for the appointment parser, date/time values, appointment value,
and JSON appointment adapter. `AppointmentCommand` has full branch coverage, with its diagnostic `toString()`
line unexecuted by the unit suite. `ClientCard` is checked separately below; that scripted check is not included
in the unit-suite coverage figures.

## Scripted JavaFX checks

A temporary harness used the packaged JAR to load the real `MainWindow`, `CommandBox`, `ResultDisplay`,
and `ClientCard` FXML components. It submitted commands through the command text field's action event,
checked feedback and error styling, and checked model/file contents after rejected commands.
The save and reload phases ran in separate JVM processes with disposable data under `build/verification`.
No real client data was used.

| Scenario | Observed result |
| --- | --- |
| `appt c/1 d/13-10-2027 t/12:34`, with no recorded pets | Added to Alice Test; her card showed the date/time. |
| Repeat Alice's slot | Duplicate message; model and saved JSON remained unchanged. |
| Same slot for Bob Test | Accepted under Bob, without changing Alice's appointment. |
| `appt c/1 d/13-10-2026 t/17:23`, executed at about 17:18 | Accepted as a future same-day appointment. |
| Today's `t/17:18`, executed during that minute | Rejected with the strictly-future message; no data changes. |
| Past date, `31-04-2027`, or `t/24:00` | Rejected with the corresponding future/date/time feedback. |
| Missing time or repeated identical `t/` | Usage or duplicate-prefix feedback, respectively. |
| `c/0`, `c/abc`, or out-of-range `c/3` | Correct index feedback; no data changes. |
| `find Bob`, followed by `appt c/1 d/13-10-2027 t/13:45` | Updated Bob and retained the one-client filter. `c/2` was rejected. |
| `edit 2 p/93456789` after `list` | Changed Bob's phone while preserving both appointments. |
| Reload saved JSON in a fresh JVM | Both clients retained their correct appointments; Bob retained the phone edit. |
| Card with details `Call on arrival` | Displayed details in parentheses after date/time. |
| Card with whitespace-only details or no appointments | Omitted parentheses or appointment labels, respectively. |
| Delete Alice, then clear remaining records | Deletion preserved Bob's appointments; clearing persisted an empty client list. |

Rendered scene images for the successful state, duplicate error, and fresh-JVM reload were inspected.
The appointment text appeared under the correct clients, the duplicate feedback remained readable,
and the command box showed error styling. These were offscreen JavaFX renders, not native screenshots.
Local evidence is retained in `build/verification/smoke-create.log`, `smoke-reload.log`, and
`build/verification/smoke-final/{created,duplicate,reloaded}.png`; these build artifacts are not committed.

## Remaining manual check

Use the [appointment manual scenarios](DeveloperGuide.md#adding-appointments) with a disposable JAR folder.
Record the tester and date here after checking the actual application:

* [ ] Enter a valid and an invalid appointment using the physical keyboard; inspect feedback and card updates.
* [ ] Check a filtered client index and duplicate slot in the visible window.
* [ ] Run `exit`, launch the JAR again from the same folder, and confirm the correct appointments appear.
* [ ] Inspect appointment text at a narrow window size and with long stored details.

Tester/date: pending.

## Relevant limitations

* The separate client-add section of the User Guide says `addc`, while this revision implements `add`.
  The verification setup uses `add`. Renaming that command is outside #83.
* The `appt` command accepts only client index, date, and time. Details can be stored and displayed but cannot
  currently be entered through this command. Appointments belong to clients; individual pets are not selected.
* The injected save-failure test confirms that an appointment can remain in memory after saving fails.
  The User Guide explains that it may then be lost on restart. This is the current shared `LogicManager`
  behavior; #83 does not change command transaction semantics.
* These checks were run on Windows in one time zone. Other operating systems rely on the repository's CI matrix.

## Release-note bullet

See [Unreleased release notes](ReleaseNotes.md#unreleased) for the appointment feature summary.
