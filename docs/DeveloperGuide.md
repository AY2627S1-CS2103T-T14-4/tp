---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# PawPals Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

This project is based on the
[AddressBook-Level3](https://github.com/se-edu/addressbook-level3) project
created by the [SE-EDU initiative](https://se-education.org).

Libraries used include
[JavaFX](https://openjfx.io/),
[Jackson](https://github.com/FasterXML/jackson), and
[JUnit 5](https://github.com/junit-team/junit5).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T14-4/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T14-4/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delc 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T14-4/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `ClientListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T14-4/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T14-4/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Client` objects from the model.

#### Command feedback states

`CommandResult` carries a `FeedbackType` (`NEUTRAL`, `SUCCESS`, or `ERROR`) alongside its unchanged
feedback string and help/exit flags. Existing constructors default to `SUCCESS`; overloads accept an
explicit type. Equality, hashing, and the string representation include the type. `find`, `findp`,
`list`, `help`, and `exit` return `NEUTRAL`; commands that change records use `SUCCESS`.

`MainWindow#executeCommand(...)` passes the result's message and type to
`ResultDisplay#setFeedbackToUser(String, FeedbackType)`. It catches `ParseException` and
`CommandException`, displays `ERROR`, and rethrows the exception so `CommandBox` retains failed input.
Storage failures follow the same error path: `LogicManager` saves before returning the command result,
so a failed save never displays that result as a success. The existing storage behavior still applies:
a save failure may occur after the in-memory model has changed; feedback does not roll it back.

`ResultDisplay` owns presentation. It maps the type to an `Info`, `Success`, or `Error` label and one
of the mutually exclusive `feedback-neutral`, `feedback-success`, and `feedback-error` CSS classes.
Each update removes previous state classes, preserves the original message, and resets the caret and
scroll offsets to the beginning. A read-only, wrapping `TextArea` provides selection and vertical
scrolling for long feedback. The one-argument setter defaults to `SUCCESS` for compatibility.
Startup guidance is `NEUTRAL`. Feedback arguments are checked for null before changing any visible state.

`CommandBox` keeps input correction separate from the last outcome: editing clears its own error style
without clearing the feedback, and successful execution clears the input. Repeated failures do not
add duplicate input error classes. Its executor must be non-null at construction.

#### Empty list and card states

`Logic#getClientList()` exposes the model's unmodifiable observable list of all stored clients.
`MainWindow` passes this list and `Logic#getFilteredClientList()` to `ClientListPanel`. The `ListView`
displays the filtered list and uses its placeholder only when that list is empty. The placeholder's
text is bound to whether the stored list is empty: no stored clients takes precedence over no matches.
Its width is bounded by the list width, clamped at zero during initial layout, so its text can wrap.
Both list arguments must be non-null; the UI never changes their contents directly.

`ClientCard` rejects a null client and renders missing pets and active appointments as subdued,
wrapping labels. A pet gets one empty-note label only when its entire notes map is empty; partial
notes keep their existing labels and safety markers. `emptyHistory` is visible and managed only while
history is empty, so it reserves no space after the first completed session. Populated history displays up to three recent
session snapshots, while all records remain in the model and storage. Model updates replace immutable clients,
causing list cells to rebuild their cards and recompute empty sections. No command syntax or storage
schema changes are required.

Tests use the shared `JavaFxTestUtil` toolkit and run new UI scenarios on the JavaFX application thread.
`ResultDisplayTest` checks all state transitions, rejected null feedback, and long-message scrolling;
`FeedbackUiIntegrationTest` covers parsing, execution and saving failures plus correction behavior.
`EmptyStateFixtures` supplies empty and populated sections, including partial/full notes and history.
`ClientCardEmptyStateTest`, `ClientListPanelTest`, and `EmptyStateUiIntegrationTest` verify placeholders,
reused cells, filtering, and transitions through real commands. `LogicManagerTest` verifies that the
stored list stays observable across changes and cannot be modified through the Logic API.

#### Appointment presentation and refresh

`ClientListPanel` displays each client through a `ClientCard` in the existing `ListView`.
`ClientCard` renders `Client#getAppointments()` in insertion order. Its private appointment formatter
reads the date, start time, optional end time, recorded linked names and booking details directly from
`Appointment`. A row such as `Appointment: 12-12-2026 14:00–15:30 · Milo, Luna (Full groom)` omits the
end-time range when no end time exists and omits parentheses for blank details.

The formatter uses `Appointment#getPetNames()` in recorded order, rather than looking up the client's
current pets. An empty collection displays `No pets linked`; it never implies all the client's pets.
Unresolved names in existing records remain visible, so displaying a card does not lose booking information.

Pet summaries, appointment rows and pet notes share wrapping-label configuration. The FXML containers
allow content to shrink horizontally, while labels keep their computed preferred height. Each list
cell binds its card's preferred and maximum widths to its available width after insets, allowing
wrapping and variable row heights. Replacing or clearing a cell unbinds its previous card.

Model updates replace immutable clients through `Model#setClient(...)`, which replaces an element in
the observable client list. The displayed list then updates its cells and recreates affected cards.
`Client#withPetReplaced(...)` updates active appointment names after a rename, and `Client#withoutPet(...)`
removes deleted pets from active links. Removing the final link displays `No pets linked` on the next
card update. Presentation adds no per-pet listeners or separate refresh mechanism.

Rendering tests cover linked/unlinked rows with optional end times, stored details, ordered names and
unresolved names. Layout tests exercise wrapping at narrow widths; list tests verify model-driven
refresh and binding cleanup during cell reuse. UI assertions run on the JavaFX application thread.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T14-4/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delc 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delc 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteClientCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `PawPalsParser` object, which in turn creates a parser that matches the command (e.g., `DeleteClientCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteClientCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a client).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `PawPalsParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `PawPalsParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteClientCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T14-4/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the PawPals data i.e., all `Client` objects (which are contained in a `UniqueClientList` object). Each `Client` also holds its own pets, active appointments and completed-session history, described in [Pets and appointments](#pets-and-appointments) and [Completed-session history](#completed-session-history).
* stores the `Client` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Client>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `PawPals`, and each `Client` references tags from that list. This lets `PawPals` maintain one `Tag` object per unique tag instead of each `Client` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>

#### Pets and appointments

The class diagram below shows how a `Client`'s pets and appointments are modelled.

<puml src="diagrams/PetAppointmentClassDiagram.puml" width="700" />

* A `Pet` has a `PetName`, `Species` and `Breed`, and up to three `PetNote`s: at most one for each `NoteType` (`PREF` preference, `SENS` sensitivity, `TEMP` temperament). Sensitivity and temperament notes count as safety information.
* An `Appointment` has an `AppointmentDate`, a start `AppointmentTime`, an optional end `AppointmentTime` and free-text details. Within a client, an appointment is identified by its date and start time.
* An `Appointment` links to the pets it covers by `PetName`, not by `Pet` object. Pet names are unique within a client (ignoring case), so a name is enough to find the pet, and `Client#getPetsFor(Appointment)` returns the linked `Pet` objects.
* Like `Client`, these classes are immutable. Changes such as `Pet#withNote(...)` or `Client#withPetReplaced(...)` return a new object.

`AppointmentCommandParser` accepts repeated optional `pn/` fields using the shared `PREFIX_PET_NAME` and
`ParserUtil#parsePetName(...)`. Required client/date/start parsing still uses
`ParserUtil#parseAppointmentTarget(...)`. At execution, `AppointmentCommand` resolves every requested name
through the displayed client's `Client#getPet(...)`, then stores the recorded names once in first-reference
order. This reuses existing case-insensitive pet identity rules and preserves the appointment's other fields.
Missing pets reject the command before any model update. The resolved appointment uses the same global
Model conflict check as client-only bookings; omitting `pn/` does not infer participating pets.

`appt` accepts past, current and future starts. Its injected clock is used only to add an informational
note to success feedback for past starts. Past appointments use the same pet validation and active-booking
conflict checks as future appointments, and remain active until cancelled or recorded with `done`.

**Design consideration: how an appointment refers to its pets**

* **Alternative 1 (current choice):** store the pet's name.
  * Pros: `Pet` objects are immutable, so editing a pet replaces the object; a name stays valid across edits. The data file stays simple, and older files without linked pets still load.
  * Cons: the links must be updated when a pet is renamed or deleted. `Client#withPetReplaced(...)` and `Client#withoutPet(...)` do this, so commands never have to.
* **Alternative 2:** store a reference to the `Pet` object.
  * Pros: no lookup by name.
  * Cons: after a pet is edited, appointments would still point to the old `Pet` object, and saving the reference to the data file would need an ID scheme.


#### Global appointment conflicts

Commands call `Model#findAppointmentConflict(candidate)` through the `Model` facade.
`ModelManager` delegates the check to `AppointmentConflictChecker`, which returns either
the first conflicting active appointment and its owning client, or an empty optional.

The activity diagram below shows the global conflict-checking workflow. The check runs before
`Model#setClient(...)`, so rejected bookings leave the data and current filter unchanged.

<puml src="diagrams/AppointmentConflictActivityDiagram.puml" alt="Activity diagram of the global appointment-conflict check" />

Commands call `Model#findAppointmentConflict(candidate)` to check active bookings through the Model facade.
`ModelManager` delegates to `AppointmentConflictChecker`, which scans the full client list's active
appointments using `Appointment#overlaps(...)`. It returns the first conflict with its owning client,
or an empty optional. It ignores the current UI filter and completed-session history.
`appt` checks this before replacing a client, so rejected bookings leave data and the filter unchanged.
`Messages#formatAppointmentConflict(...)` supplies feedback including the owner and conflicting slot.

The overload `Model#findAppointmentConflict(candidate, originalClient, originalAppointment)` supports `editappt`.
It excludes only the original owner's booking identified by date and start time, while checking all
other bookings. The editing command must validate its target, run this check before mutation, then use
`Client#withAppointmentReplaced(...)` and one model update. A client-only appointment and appointments
for different pets follow the same overlap rules.

#### Appointment rescheduling

`PawPalsParser` dispatches `editappt` to `EditAppointmentCommandParser`. The parser reuses
`ParserUtil#parseAppointmentTarget(...)` for required `c/`, `d/` and `t/`, and the existing validated
date/time types for optional `nd/`, `ns/` and `ne/`. It rejects duplicate prefixes, non-empty preambles,
missing target fields and invalid values. The immutable `EditAppointmentDescriptor` requires at least
one replacement value, including when a command is constructed directly. Ordering and future-start
checks belong to execution because omitted values depend on the original appointment.

`EditAppointmentCommand` performs these steps:

1. Resolve the client from the displayed list and look up its active appointment by original date/start.
2. Construct a replacement using the supplied values and the original details and ordered pet links.
   Without a supplied end, retain a missing end or preserve the existing duration. Compute the proposed
   end in minutes and reject an end at or after midnight before converting it to `LocalTime`, avoiding
   wraparound. Exactly 23:59 is valid. A supplied `ne/` overrides this calculation.
3. Validate the end through `Appointment`'s constructor, then require the resulting start to be strictly
   after `LocalDateTime.now(clock)`. The default clock uses the system's local zone; tests inject a fixed
   clock. Past originals may be moved, but an end-only edit still requires a future resulting start.
4. Check all active bookings with `Model#findAppointmentConflict(candidate, client, original)`.
   Only the original owner's original date/start is excluded; the filter and history do not limit the scan.
5. Use `Client#withAppointmentReplaced(...)`, then call `Model#setClient(...)` exactly once. This preserves
   appointment position, other client data, history and the current predicate. Validation failures occur
   before mutation. `LogicManager` saves the updated model through the existing storage path.

Success feedback identifies the owner, original and resulting starts, and the resulting end when present.
The observable list replacement updates the card immediately. The current card format displays the date,
start and any booking details; end-only edits are confirmed through feedback and saved data. No storage
schema change is needed. Save errors retain the application's existing behavior: memory may already
contain the replacement when writing the file fails.

Tests are separated by responsibility: `EditAppointmentCommandTest` uses a model stub for validation,
duration boundaries and update/delegation checks; `EditAppointmentCommandIntegrationTest` uses the real
model for filtering, conflict exclusion and preservation. `EditAppointmentCommandParserTest` covers
syntax independently. `EditAppointmentPersistenceIntegrationTest` exercises dispatch, saving and reloading;
`EditAppointmentUiIntegrationTest` drives the actual command box, feedback display and list controls.
UI tests share a JavaFX toolkit via `JavaFxTestUtil` because it cannot restart after `Platform.exit()`
in the same test worker. Full checks run with `gradlew check coverage`.

#### Completed-session history

History is stored under its owning `Client` as an immutable `List<CompletedSession>`, separate from
the active appointments returned by `Client#getAppointments()`. The `done` command uses this model/storage
foundation; history presentation is handled by the UI.

`DoneCommandParser` reuses `ParserUtil#parseAppointmentTarget(...)` for client/date/start parsing and
the existing `PREFIX_NOTE` for required `nt/` session notes. It uses
`ArgumentTokenizer#tokenizeWithTrailingText(...)` to preserve prefix-like note text when a single `nt/`
follows all three target fields. Other field orders use normal tokenization, and duplicate prefixes
before the notes or a repeated `nt/` are still rejected. `SessionNotes` handles trimming and length
validation. `PawPalsParser` dispatches `done` to this parser.

`DoneCommand` resolves the target against the displayed client list, checks active appointments first,
and uses history only to distinguish an already completed slot from a missing booking. It delegates
completion eligibility and snapshot creation to `Client#completeAppointment(...)`, using an injectable
clock, then applies the replacement with exactly one `Model#setClient(...)` call. This keeps completion
and active removal together without changing the filter. Future starts become a command error before
the model is updated. Success feedback identifies the client, slot, recorded pets and session notes.

Each `CompletedSession` retains:

* An immutable `Appointment` snapshot: date, start time, optional end time, booking details and linked pet names.
* Required `SessionNotes`: 1–200 characters after trimming leading and trailing spaces. These describe
  the visit and do not replace booking details or a pet's standing preference/safety notes.
* `loggedAt`, an `Instant` recording when the session was logged. It does not claim to be the actual finish time.

**Completion and identity rules**

* `Client#completeAppointment(date, time, notes, clock)` looks up the **active** appointment by date and
  start time. It returns a replacement client with that appointment removed and one history record added.
  The caller applies the result with one `Model#setClient(...)` call, preserving the current filter.
* Completion is allowed at or after the scheduled start, interpreted in the supplied clock's local zone.
  It may occur before the scheduled end, or much later than the visit. The logging instant is read once
  from that same clock. A future start or missing active appointment leaves the original client unchanged.
* Repeating completion without another active appointment fails. `hasCompletedAppointment(date, time)`
  lets `DoneCommand` distinguish an already logged slot from one that never existed.
  Always look for an active appointment first; an old history record must not block a new booking at that slot.
* History has no date/start-time uniqueness constraint and never reserves time. Booking and conflict
  checks use active appointments only; `appt` also accepts past and current starts.
* An appointment without linked pets remains a client-only session. Current pets are not inferred as participants.
  A visit covering several pets creates one record with all its recorded pet names.

**Preservation, display and storage rules**

* Pet rename/deletion updates active appointment links but preserves all historical pet names. Render
  history from its snapshot rather than resolving those names through `Client#getPetsFor(...)`.
  Names are historical labels, not stable identifiers for automatic per-pet matching across renames.
* All client-copy operations and contact edits preserve history. This includes adding pets/appointments,
  changing pet notes, rescheduling and removing appointments. Deleting a client removes its history;
  `clear` removes every client's history. There is no individual history edit/delete operation in this scope.
* `Client#getHistory()` returns **all** records, sorted by appointment date and start time descending,
  then logging instant descending. Equal keys retain their insertion order. A UI preview of three
  records must not discard older stored sessions; the UI applies its limit only when rendering.
* `JsonAdaptedCompletedSession` stores the appointment snapshot, notes and an ISO-8601 logging instant.
  `JsonAdaptedClient` stores these records in an optional `history` array. Missing or null history in
  older files loads as empty history; empty history is omitted when saving.
* Stored records validate required fields and appointment/note/timestamp formats. Invalid entries reject
  the file rather than silently dropping history. Loading does not reapply completion eligibility against
  the current clock, or require historical pets to remain in the client's current pet list.

Example entry in a client's `history` array:

```json
{
  "appointment": {
    "date": "20-10-2026",
    "time": "14:00",
    "endTime": "15:00",
    "details": "Call on arrival",
    "pets": ["Milo"]
  },
  "notes": "Teddy cut, short ears",
  "loggedAt": "2026-10-21T06:30:00Z"
}
```

The single-client update guarantees that active removal and history creation happen together in memory.
Persistence still uses the existing `LogicManager` save pipeline; this model operation does not introduce
disk transactions or rollback on save errors.

#### Completed-session rendering

`ClientCard` adds a **History** section beneath active appointments. Its private
`HISTORY_DISPLAY_LIMIT` is three: it consumes `Client#getHistory()` in the supplied order
using `stream().limit(...)`, leaving the complete immutable list and storage unchanged.
Each session is a pair of wrapping labels: snapshot date/start/optional end and recorded
pet names, followed by `Notes: ...`. An empty pet set renders as `No pets linked`.
Booking details and `loggedAt` remain stored but are not displayed.

The shared appointment-summary formatter reads `CompletedSession#getAppointment()` directly.
It never resolves historical names against current pets, so later rename/deletion cannot
rewrite what the completed visit displays. All labels wrap and grow vertically at narrow widths.
An empty history shows `No completed sessions` beneath the History heading and has no session rows.
The placeholder is hidden and unmanaged when completed sessions are present.

The existing JavaFX observable-list mechanism refreshes the card after `DoneCommand` applies
its single `Model#setClient(...)` replacement. The UI displays active removal and history creation
from that same replacement without resetting the filter or adding a command-to-UI dependency.

Rendering tests cover zero through four sessions, recorded names, absent end times/pet links,
ordering ties, client isolation and long wrapping notes. The command-box integration test checks
immediate refresh under a filter, retention of four records after JSON reload, and subsequent
pet rename/deletion without changes to displayed historical names.

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T14-4/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both PawPals data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonPawPalsStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)
* stores each client's pets, active appointments and completed sessions inside that client's JSON object. A pet's notes are saved in its `notes` object, keyed by note type, e.g. `"notes": {"pref": "Teddy cut", "temp": "Nervous around dryers"}`.
* treats every field added after v1.3 (pet notes, an appointment's end time and linked pets, and session history) as optional, so data files saved by older versions still load.

#### Corrupted-data recovery

When `JsonPawPalsStorage` cannot convert `data/pawpals.json` into a valid `PawPals` model,
`MainApp` must not immediately treat the empty recovery model as ordinary data. The recovery flow is:

1. `JsonPawPalsStorage.readPawPals()` throws `DataLoadingException` after JSON or model validation fails.
2. `MainApp.initModelManager(...)` asks the `Storage` component to create a backup before constructing an
   empty `PawPals` model. The backup is created by `JsonPawPalsStorage.backupCorruptedData()` using a
   unique temporary name based on the original file, such as `pawpals.json.corrupt-12345.bak`. The
   original rejected file is never used as the backup destination and is not overwritten during this step.
3. `DataRecoveryInfo` carries the original path, the optional backup path, and the loading error from
   `MainApp` to `UiManager`.
4. After the main window is created, `UiManager` shows a modal warning before the command box can be used.
   It explains that the application is using an empty client list and identifies the backup file when one
   was created.
5. The user must choose **Continue** or **Exit**. Continuing permits normal commands and therefore permits
   the existing auto-save pipeline to replace the rejected file with the user's new empty-data state.
   Exiting closes the application before any command can trigger an auto-save, preserving the rejected file
   and its backup.

If backup creation fails, the warning explicitly says that no backup was created. The application still
requires the same confirmation before continuing, so the user is aware that the rejected file cannot be
recovered automatically. Missing data files are not considered corruption: they continue to load sample
data without a recovery warning. Valid files, including valid files containing zero clients, also follow the
normal startup path.

The storage tests verify that backups preserve the exact rejected bytes, that repeated recovery attempts
receive different backup paths, and that the original file remains unchanged. Manual testing should verify
both confirmation choices: **Exit** must leave the rejected file unchanged, while **Continue** followed by
a successful command may replace it with the current model.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Deleting a pet note: `delnote`

`delnote c/CLIENT_INDEX pn/PET_NAME ty/TYPE` removes one note from one of a client's pets, keeping the pet's other notes.
`note` adds or replaces a note, and its `nt/` is required, so `delnote` is the only way to remove one.

The sequence diagram below shows how `delnote c/1 pn/Milo ty/temp` is executed.
Parsing works as in the `delc` example in the [Logic component](#logic-component), so it is shown as a `ref` frame.

<puml src="diagrams/DeleteNoteSequenceDiagram.puml" alt="Interactions when executing delnote" width="700" />

1. `DeleteNoteCommandParser` parses the client index, pet name and note type into `d:DeleteNoteCommand`.
   The note type is parsed by `ParserUtil#parseNoteType(...)`, which `note` also uses.
1. `d` gets the displayed client list from the `Model`, picks client `c` at the given index, and looks up the pet by name, ignoring case.
1. `d` checks that the pet has a note of that type, then builds the updated objects. `Pet` and `Client` are immutable,
   so `withoutNote(...)` and `withPetReplaced(...)` return new objects instead of changing `p` and `c`.
1. `d` calls `Model#setClient(...)` once to swap in the updated client, then returns a `CommandResult`.

All checks happen before the `Model` is changed, so a failed command never leaves partly updated data:

<puml src="diagrams/DeleteNoteActivityDiagram.puml" alt="Checks performed by delnote" width="650" />

**Design consideration: how a user removes a note**

* **Alternative 1 (current choice):** a separate `delnote` command, with `nt/` required in `note`.
  * Pros: each command does one thing, so a note can never be removed by accident, and the error messages are specific.
  * Cons: one more command for users to learn.
* **Alternative 2:** `note` with an empty or missing `nt/` removes the note.
  * Pros: one command covers adding, replacing and removing.
  * Cons: a typo such as a forgotten `nt/` silently deletes a note, and "a note with no text" is confusing.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is an independent mobile pet groomer
* has a need to manage a significant number of client contacts, their pets and appointments
* prefers desktop apps over other types of applications
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: Manage client contacts, appointments and related pet information faster than with a typical mouse-driven GUI application.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                               | I want to …                                                  | So that I can…                                                                          |
|----------|------------------------------------------------------|--------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `* *`    | pet groomer using PawPals for the first time         | see the app populated with sample data                       | understand how client, pet, and appointment records are structured                      |
| `*`      | pet groomer ready to start using PawPals for my work | purge the sample data                                        | quickly start to populate PawPals with real business information                        |
| `* * *`  | pet groomer onboarding a new client                  | add a new client                                             | keep their information for future visits                                                |
| `* * *`  | pet groomer                                          | record a client’s contact details and home address           | refer to their contact and location information when needed                             |
| `* * *`  | pet groomer                                          | add a pet belonging to a client                              | track distinct grooming needs and histories for different pets owned by the same client |
| `* * *`  | pet groomer                                          | record a pet’s basic details                                 | identify and understand the pet I am grooming                                           |
| `* *`    | pet groomer                                          | record a pet’s temperament and behavioural concerns          | handle it safely and appropriately                                                      |
| `* *`    | pet groomer                                          | record grooming-related sensitivities                        | avoid unsuitable products or procedures                                                 |
| `* *`    | pet groomer                                          | record a pet’s grooming preferences and special instructions | provide consistent service without relying on memory                                    |
| `* * *`  | pet groomer                                          | view a summary of available commands                         | quickly learn or remember how to use the application                                    |
| `* * *`  | pet groomer planning my schedule                     | schedule client appointments                                 | prepare for  subsequent pet grooming jobs                                               |
| `* * *`  | pet groomer                                          | view my upcoming appointments                                | plan my day efficiently                                                                 |
| `* * *`  | pet groomer preparing for a visit                    | view an appointment’s date, time, client, pet, and address   | arrive at the correct location prepared for the visit                                   |
| `* *`    | pet groomer preparing to groom on-site               | view important pet notes before grooming                     | prepare to handle the pet appropriately                                                 |
| `* *`    | pet groomer handling schedule adjustments            | update an appointment                                        | keep the appointment details accurate                                                   |
| `* * *`  | pet groomer                                          | cancel an appointment                                        | prevent outdated information from accumulating in my schedule                           |
| `* *`    | pet groomer scheduling an appointment                | identify conflicting appointment times                       | avoid double-booking myself                                                             |
| `* *`    | pet groomer planning my workday                      | view appointments for a selected date                        | focus on that day’s visits                                                              |
| `* *`    | pet groomer reviewing service history                | view a pet’s previous grooming sessions                      | provide consistent service during future visits                                         |
| `* *`    | pet groomer completing a service                     | record details and notes about a completed grooming session  | remember what was done during the visit                                                 |
| `* *`    | pet groomer receiving client feedback                | update grooming preferences and instructions                 | reflect the client’s latest requests in future visits                                   |
| `* *`    | pet groomer                                          | view a client’s details                                      | quickly recall their information                                                        |
| `* *`    | pet groomer                                          | view all pets belonging to a client                          | efficiently manage multi-pet households                                                 |
| `* *`    | pet groomer                                          | see the client associated with a pet                         | know whom to contact about the pet                                                      |
| `* *`    | pet groomer updating outdated contact information    | update a client’s details                                    | keep the client’s information accurate                                                  |
| `* *`    | pet groomer updating a pet’s information             | update a pet’s details                                       | keep the pet’s information accurate over time                                           |
| `* * *`  | pet groomer looking up a returning client            | search for a client using identifying information            | find the correct client quickly without scrolling                                       |
| `*`      | pet groomer looking up a returning pet               | search for a pet by name                                     | retrieve its information quickly without scrolling                                      |
| `*`      | fast-typing mobile pet groomer                       | search across notes and records using keywords               | find relevant information even when I do not remember which client or pet it belongs to |
| `*`      | pet groomer managing a large client base             | categorise clients or pets                                   | easily filter them for specialized care                                                 |
| `*`      | pet groomer preparing for upcoming visits            | filter pets by important safety information                  | identify pets requiring special handling                                                |
| `*`      | pet groomer managing many appointments               | filter appointments by relevant criteria                     | focus on the appointments that matter at the moment                                     |
| `* * *`  | pet groomer maintaining long-term records            | remove inactive client records                               | keep my client list relevant and manageable                                             |
| `* * *`  | pet groomer maintaining long-term records            | remove pet records I no longer need                          | keep my pet records relevant and manageable                                             |
| `* * *`  | pet groomer reviewing my client                      | list all client records with their contact details           | browse the people I serve                                                               |
| `* * *`  | pet groomer finished with my records                 | exit PawPals                                                 | end my work session                                                                     |
| `* *`    | pet groomer updating records between visits          | automatically save my changes                                | prevent accidental data loss                                                            |

### Use cases

The following use cases describe the functional requirements of PawPals. They focus on the observable interaction between the user and the application.

#### UC01 - Add client

System: PawPals<br>
Use case: UC01 - Add client<br>
Actor: User<br>
Preconditions: PawPals is running.

MSS:

1. User requests to add a client.
2. User provides the client's name, phone number, email address, and home address.
3. PawPals validates the details and adds the new client.
4. PawPals displays a success message and the new client in the client list.

Use case ends.

Extensions:

* 2a. User provides an invalid or missing detail.
  * 2a1. PawPals displays the corresponding validation message.
  * 2a2. User provides corrected details.
  * Use case resumes from step 3.
* 4a. PawPals detects an existing client with the same name and phone number.
  * 4a1. PawPals informs the user that the client already exists.
  * Use case ends.

#### UC02 - Add pet

System: PawPals<br>
Use case: UC02 - Add pet<br>
Actor: User<br>
Preconditions: PawPals is running and the pet's client exists in the displayed client list.

MSS:

1. User requests to add a pet.
2. User identifies the pet's client and provides the pet's name, species, and breed.
3. PawPals validates the details and adds the pet under the selected client.
4. PawPals displays a success message and the new pet.

Use case ends.

Extensions:

* 2a. User provides an invalid or missing detail.
  * 2a1. PawPals displays the corresponding validation message.
  * 2a2. User provides corrected details.
  * Use case resumes from step 3.
* 3a. The client already has a pet with the same name.
  * 3a1. PawPals informs the user that the pet already exists for that client.
  * Use case ends.

#### UC03 - Delete client

System: PawPals<br>
Use case: UC03 - Delete client<br>
Actor: User<br>
Preconditions: PawPals is running and the client exists in the displayed client list.

MSS:

1. User requests to delete a client.
2. User provides the client's index.
3. PawPals removes the client and all appointments and pets belonging to the client.
4. PawPals displays a success message and the updated client list.

Use case ends.

Extensions:

* 2a. User provides a missing, extra, or invalid index.
  * 2a1. PawPals displays the corresponding validation message.
  * Use case ends.

#### UC04 - Add appointment

System: PawPals<br>
Use case: UC04 - Add appointment<br>
Actor: User<br>
Preconditions: PawPals is running and the appointment's client exists in the displayed client list.

MSS:

1. User requests to add an appointment.
2. User identifies the client and provides a date, start time, and optional end time and pet names.
3. PawPals validates the appointment's date and times, the linked pets' ownership, and global booking conflicts.
4. PawPals adds the appointment to the client.
5. PawPals displays a success message and the appointment under the client.

Use case ends.

Extensions:

* 2b. User provides an invalid date or time.
  * 2b1. PawPals displays the corresponding date or time validation message.
  * 2b2. User provides corrected details.
  * Use case resumes from step 3.
* 3a. A requested pet does not belong to the selected client.
  * 3a1. PawPals identifies the unknown pet and leaves all data unchanged.
  * Use case ends.
* 4a. The appointment overlaps an active booking for any client.
  * 4a1. PawPals identifies the conflicting client and booking slot, leaving all data unchanged.
  * Use case ends.
* 5a. The appointment starts in the past.
  * 5a1. PawPals adds an informational note to the success feedback suggesting `done` to record the completed visit.
  * Use case ends successfully.

#### UC05 - List clients, pets, and appointments

System: PawPals<br>
Use case: UC05 - List clients, pets, and appointments<br>
Actor: User<br>
Preconditions: PawPals is running.

MSS:

1. User requests to view the records.
2. PawPals displays all clients with their phone number, address, email, pets, and appointments.

Use case ends.

Extensions:

* 1a. User provides an invalid request.
  * 1a1. PawPals displays an error message explaining the expected input.
  * Use case ends.

#### UC06 - Find clients

System: PawPals<br>
Use case: UC06 - Find clients<br>
Actor: User<br>
Preconditions: PawPals is running.

MSS:

1. User requests to find clients using one or more keywords.
2. PawPals searches client names using full-word matching without regard to letter case.
3. PawPals searches phone numbers for exact matches.
4. PawPals searches email addresses for exact matches without regard to letter case.
5. PawPals uses OR matching when multiple keywords are provided.
6. PawPals displays all matching clients and their information.

Use case ends.

Extensions:

* 1a. User provides no keywords.
  * 1a1. PawPals displays an error message explaining that at least one keyword is required.
  * Use case ends.

* 1b. User provides a partial phone number, email address, or name word.
  * 1b1. PawPals displays no client for that partial match.
  * Use case ends.

#### UC07 - View help

System: PawPals<br>
Use case: UC07 - View help<br>
Actor: User<br>
Preconditions: PawPals is running.

MSS:

1. User requests help.
2. PawPals displays instructions for using the application and accessing the user guide.

Use case ends.

Extensions:

* 1a. User provides an invalid request.
  * 1a1. PawPals displays an error message explaining the expected input.
  * Use case ends.

#### UC08 - Clear all records

System: PawPals<br>
Use case: UC08 - Clear all records<br>
Actor: User<br>
Preconditions: PawPals is running.

MSS:

1. User requests to clear the application data.
2. PawPals removes all clients, pets, and appointments.
3. PawPals displays a confirmation message and an empty list.

Use case ends.

Extensions:

* 1a. User provides an invalid request.
  * 1a1. PawPals displays an error message explaining the expected input and retains all records.
  * Use case ends.

#### UC09 - Exit PawPals

System: PawPals<br>
Use case: UC09 - Exit PawPals<br>
Actor: User<br>
Preconditions: PawPals is running.

MSS:

1. User requests to exit PawPals.
2. PawPals closes the application.

Use case ends.

Extensions:

* 1a. User provides an invalid request.
  * 1a1. PawPals displays an error message explaining the expected input.
  * Use case ends.

#### UC10 - Auto-save data

System: PawPals<br>
Use case: UC10 - Auto-save data<br>
Actor: User<br>
Preconditions: PawPals is running and the data file is writable.

MSS:

1. User completes an action that changes clients, pets, or appointments.
2. PawPals saves the updated data automatically.
3. PawPals keeps the saved data available for the next application launch.

Use case ends.

Extensions:

* 2a. PawPals cannot write to the data file because of insufficient permissions, concurrent access, or insufficient disk space.
  * 2a1. PawPals displays the corresponding save failure message.
  * 2a2. PawPals leaves the data file unchanged.
  * Use case ends.

#### UC11 - Delete pet note

System: PawPals<br>
Use case: UC11 - Delete pet note<br>
Actor: User<br>
Preconditions: PawPals is running and the client exists in the displayed client list.

MSS:

1. User requests to delete a note of one of a client's pets.
2. User provides the client's index, the pet's name and the note type.
3. PawPals removes that note from the pet, keeping the pet's other notes.
4. PawPals displays a success message and the updated client card.

Use case ends.

Extensions:

* 2a. User provides a missing, repeated or invalid index, pet name or note type.
  * 2a1. PawPals displays the corresponding validation message.
  * 2a2. User provides corrected details.
  * Use case resumes from step 3.
* 2b. The client has no pet with that name.
  * 2b1. PawPals informs the user that the client has no pet with that name.
  * Use case ends.
* 2c. The pet has no note of that type.
  * 2c1. PawPals informs the user that the pet has no such note, leaving all data unchanged.
  * Use case ends.

#### UC12 - Delete pet

System: PawPals<br>
Use case: UC12 - Delete pet<br>
Actor: User<br>
Preconditions: PawPals is running and the client exists in the displayed client list.

MSS:

1. User requests to delete one of a client's pets.
2. User provides the client's index and the pet's name.
3. PawPals removes the pet and its notes from the client, and unlinks the pet from the client's appointments.
4. PawPals displays a success message and the updated client card.

Use case ends.

Extensions:

* 2a. User provides a missing, repeated or invalid index or pet name.
  * 2a1. PawPals displays the corresponding validation message.
  * 2a2. User provides corrected details.
  * Use case resumes from step 3.
* 2b. The client has no pet with that name.
  * 2b1. PawPals informs the user that the client has no pet with that name, leaving all data unchanged.
  * Use case ends.

#### UC13 - Edit pet

System: PawPals<br>
Use case: UC13 - Edit pet<br>
Actor: User<br>
Preconditions: PawPals is running and the client exists in the displayed client list.

MSS:

1. User requests to edit one of a client's pets.
2. User provides the client's index, the pet's current name, and a new name, species or breed.
3. PawPals updates the pet, keeping its notes. If the pet was renamed, PawPals also renames it in the client's appointments.
4. PawPals displays a success message and the updated client card.

Use case ends.

Extensions:

* 2a. User provides a missing, repeated or invalid index, pet name, species or breed, or no field to edit.
  * 2a1. PawPals displays the corresponding validation message.
  * 2a2. User provides corrected details.
  * Use case resumes from step 3.
* 2b. The client has no pet with that name.
  * 2b1. PawPals informs the user that the client has no pet with that name, leaving all data unchanged.
  * Use case ends.
* 3a. The new name is already used by another of the client's pets.
  * 3a1. PawPals informs the user that the client already has a pet with that name, leaving all data unchanged.
  * Use case ends.

#### UC14 - Add or update pet note

System: PawPals<br>
Use case: UC14 - Add or update pet note<br>
Actor: User<br>
Preconditions: PawPals is running and the client exists in the displayed client list.

MSS:

1. User requests to add or update a note for one of a client's pets.
2. User provides the client's index, the pet's name, the note type and the note text.
3. PawPals adds the note, or replaces the existing note of the same type for that pet.
4. PawPals displays a success message and the updated client card. Sensitivity and temperament notes are marked with `⚠` as safety information.

Use case ends.

Extensions:

* 2a. User provides a missing, repeated or invalid client index, pet name, note type or note text.
  * 2a1. PawPals displays the corresponding validation message and leaves all data unchanged.
  * 2a2. User provides corrected details.
  * Use case resumes from step 3.
* 2b. The client has no pet with the provided name.
  * 2b1. PawPals informs the user that the client has no pet with that name, leaving all data unchanged.
  * Use case ends.

#### UC15 - Find pet owners

System: PawPals<br>
Use case: UC15 - Find pet owners<br>
Actor: User<br>
Preconditions: PawPals is running.

MSS:

1. User requests to find pet owners using one or more pet-name keywords.
2. PawPals searches all pets using full-name matching without regard to letter case.
3. PawPals uses OR matching when multiple keywords are provided.
4. PawPals displays each client who owns at least one matching pet.
5. PawPals displays each matching owner only once, even if multiple pets match.
6. PawPals keeps the displayed client indexes available for subsequent client commands.

#### UC16 - Cancel appointment

System: PawPals<br>
Use case: UC16 - Cancel appointment<br>
Actor: User<br>
Preconditions: PawPals is running and the client exists in the displayed client list.

MSS:

1. User requests to cancel an appointment.
2. User provides the client's displayed index and the appointment's date and start time.
3. PawPals removes the selected active appointment, preserving the client's other appointments, pets,
   notes, contact details and completed history. The current filter is kept.
4. PawPals displays feedback identifying the cancelled appointment and removes its row from the client card.

Use case ends.

Extensions:

* 1a. User provides no keywords.
  * 1a1. PawPals displays an error message explaining that at least one keyword is required.
  * Use case ends.

* 1b. No pets match the given keywords.
  * 1b1. PawPals displays an empty client list and reports that no pets were found.
  * Use case ends.

* 2a. User provides a missing, repeated or invalid index, date or start time, or an index outside the displayed list.
  * 2a1. PawPals displays the corresponding validation message and leaves the data unchanged.
  * 2a2. User provides corrected details.
  * Use case resumes from step 3.
* 2b. The selected client has no active appointment at that date and start time.
  * 2b1. PawPals identifies the missing appointment and leaves the data unchanged.
  * Use case ends.

Past and future active appointments can be cancelled. Cancellation does not create a completed session.
The removed booking no longer reserves its slot; a replacement may start in the past, present or future,
provided it does not overlap any remaining active booking.

#### UC17 - Complete appointment

System: PawPals<br>
Use case: UC17 - Complete appointment<br>
Actor: User<br>
Preconditions: PawPals is running and the client exists in the displayed client list.

MSS:

1. User requests to complete an appointment.
2. User provides the client's displayed index, the appointment's date and start time, and session notes.
3. PawPals checks that the active appointment exists and has started, using the computer's local time.
4. PawPals removes the active appointment and adds one completed-session record containing its date,
   start, optional end, booking details, linked pet names, session notes and logging time. Other client data,
   earlier history and the current filter are preserved.
5. PawPals displays feedback identifying the completed appointment and notes; its active row disappears.

Use case ends.

Extensions:

* 2a. User provides a missing, repeated or invalid index, date, start time or notes, or an index outside the displayed list.
  * 2a1. PawPals displays the corresponding validation message and leaves the data unchanged.
  * 2a2. User provides corrected details. Notes must contain 1 to 200 characters after trimming.
  * Use case resumes from step 3.
* 3a. The selected client has no active appointment at the supplied date and start time.
  * 3a1. If a completed session exists at that slot, PawPals reports that it has already been completed;
    otherwise, PawPals reports that no active appointment exists there.
  * 3a2. PawPals leaves active appointments and history unchanged.
  * Use case ends.
* 3b. The appointment has not started.
  * 3b1. PawPals rejects completion and leaves active appointments and history unchanged.
  * Use case ends.

Completion is allowed at or after the start, even before the planned end. Completed history does not
reserve booking time, so a future interval released by completion can be booked again. A new active
appointment takes precedence over a historical record at the same slot when identifying a completion target.

### Non-Functional Requirements

1. PawPals should work on any mainstream OS as long as it has Java 25 or above installed.
2. For a dataset of up to 100 clients and their associated pet and appointment records, common commands such as list and find should complete and update the displayed results within 1 second.
3. All core PawPals workflows should be completable using keyboard input without requiring mouse interaction.
4. Client, pet, and appointment data that has been successfully saved should remain intact and available after PawPals is closed and subsequently relaunched.
5. PawPals should support at least 100 clients and their associated pet and appointment records.

### Glossary

* **Client**: A customer of the pet grooming business whose contact and home address information is stored in PawPals. A client may have one or more pets.
* **Pet**: An animal belonging to a client whose grooming-related information is managed in PawPals.
* **Appointment**: A grooming visit associated with a client that remains active until completed or cancelled. Its start may be in the past, present or future.
* **Grooming session**: A completed grooming service whose details and notes are recorded for future reference.
* **Safety information**: Information about a pet that may affect how it should be handled or groomed, such as temperament, behavioural concerns, or grooming-related sensitivities.
* **Client index**: A positive integer identifying a client in the currently displayed client list. It is used by commands that require the user to specify a particular client.
* **Mainstream OS**: Windows, macOS, or Linux.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with six sample clients. Most have pets and appointments, and some pets have notes,
      with ⚠ on sensitivity and temperament notes. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.


### Empty states and command feedback

Use a disposable copy of PawPals. The following checks include `clear`, which removes all records.
Resize to the minimum window width and check that text remains readable.

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Initial feedback | Launch PawPals. | `Info` shows the initial command guidance. |
| Empty database | Run `clear`, then `find Nobody`. | The list shows `No clients yet. Use addc to create a client.`; `clear` gives `Success`, while `find` gives `Info`. |
| Empty card | Run `addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road`. | `Success` identifies Alice. Her card shows no pets, appointments, or completed sessions. |
| Search without matches | Run `find Nobody`, then `findp Nobody`, then `list`. | Each search shows the no-matches placeholder with `Info`; `list` restores Alice's card. |
| Empty notes | Run `addp c/1 n/Milo s/Dog b/Poodle`, then `note c/1 pn/Milo ty/sens nt/Sensitive paws`. | The pet replaces the empty-pets message. Its empty-note message is replaced by `⚠ Sens: Sensitive paws`. |
| Remove last note and pet | Run `delnote c/1 pn/Milo ty/sens`, then `delp c/1 pn/Milo`. | The empty-note message returns after `delnote`; after `delp`, the empty-pets message replaces the pet and its notes. |
| Last appointment cancelled | Add `appt c/1 d/FUTURE_DATE t/14:00`, using a future date in `DD-MM-YYYY`, then `cancel c/1 d/FUTURE_DATE t/14:00`. | The appointment replaces its empty message; cancellation restores it. |
| First completed session | Add a future booking, wait until its start, then run `done c/1 d/DATE t/TIME nt/Trim completed` using its recorded date/start. | The active booking disappears and its empty message returns. `No completed sessions` disappears and the completed session appears with its recorded times, pet names and notes. |
| Error and correction | Submit `unknown` twice. Edit it to `list`, then press Enter. | `Error` and retained input on each failure; editing resets input colour while feedback remains `Error`; submission changes feedback to `Info` and clears input. |
| Error to success | Submit `unknown`, then a valid `addc` for another client. | `Success` replaces `Error` without stale error styling. |
| Long feedback | Submit `appt` without arguments and scroll within its feedback. Then submit `list`. | Usage text wraps, can be selected, and scrolls to its last line. `list` replaces it with `Info` and resets scrolling. |
| Last client removed | Run `clear`, add Alice again, then run `delc 1`. | The empty-database message returns after the last client is deleted. |

### Adding and persisting pets

Use a disposable copy of the JAR in an empty folder so these tests do not affect real client data.
Launch it with `java -jar pawpals.jar`.

Set up two clients using `addc`:

```text
clear
addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road
addc n/Bob Test p/92345678 e/bob@example.com a/20 Dover Road
```

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Add a pet | `addp c/1 n/Milo s/Dog b/Poodle` | Success message appears and Alice's card shows `Pet: Milo (Dog, Poodle)`. Bob's card has no pet entry. |
| Add a pet to another client | `addp c/2 n/Luna s/Cat b/Persian` | Luna appears under Bob. Alice still shows only Milo. |
| Restart and reload | Run `exit`, relaunch from the same folder, and run `list`. | Milo remains under Alice and Luna remains under Bob. Client details are unchanged. |
| Legacy client without pets | In a disposable `data/pawpals.json`, remove a client's `pets` property and restart. | The client still loads correctly and displays without a pet entry. |

### Adding or updating a pet note

Set up one client with a pet:

```text
clear
addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road
addp c/1 n/Milo s/Dog b/Poodle
```

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Add a preference note | `note c/1 pn/Milo ty/pref nt/Likes quiet grooming` | `Updated preference note for Milo: Likes quiet grooming`. The note appears without `⚠`. |
| Add a sensitivity note | `note c/1 pn/Milo ty/sens nt/Sensitive paws` | `Updated sensitivity note for Milo: Sensitive paws`. The note appears with `⚠ Sens`. |
| Add a temperament note | `note c/1 pn/Milo ty/temp nt/Anxious around dryers` | `Updated temperament note for Milo: Anxious around dryers`. The note appears with `⚠ Temp`. |
| Replace an existing note | `note c/1 pn/Milo ty/pref nt/Prefers a teddy cut` | The preference note is replaced; the old preference text is no longer shown. Existing sensitivity and temperament notes remain. |
| Unknown pet | `note c/1 pn/Rex ty/pref nt/Likes treats` | `This client has no pet named Rex.` No data changes. |
| Invalid note type | `note c/1 pn/Milo ty/mood nt/Likes treats` | `Note type must be pref, sens or temp.` |
| Empty note | `note c/1 pn/Milo ty/pref nt/` | The note-length validation message is shown. No data changes. |
| Note over 200 characters | `note c/1 pn/Milo ty/pref nt/<201-character note>` | The note-length validation message is shown. No data changes. |
| Repeated prefix | `note c/1 pn/Milo ty/pref ty/temp nt/Note` | `Multiple values specified for the following single-valued field(s): ty/` |
| Restart | Add or replace a note, then run `exit`, relaunch and run `list`. | The latest note remains visible after restart. |

### Deleting a pet note

Continue from [Adding and persisting pets](#adding-and-persisting-pets), so that Alice has Milo and Bob has Luna.
Add two notes to Milo first:

```text
note c/1 pn/Milo ty/temp nt/Nervous around dryers
note c/1 pn/Milo ty/pref nt/Teddy cut
```

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Delete a note | `delnote c/1 pn/Milo ty/temp` | `Deleted temperament note for Milo`. The ⚠ Temp line disappears from Milo; the Pref line remains. |
| Pet name and type in any case | `delnote c/1 pn/MILO ty/PREF` | `Deleted preference note for Milo`. Milo now has no notes. |
| No such note | `delnote c/1 pn/Milo ty/sens` | `Milo has no sensitivity note to delete.` No data changes. |
| Unknown pet | `delnote c/1 pn/Rex ty/pref` | `This client has no pet named Rex.` No data changes. |
| Invalid type | `delnote c/1 pn/Milo ty/mood` | `Note type must be pref, sens or temp.` |
| Missing prefix | `delnote c/1 pn/Milo` | `Invalid command format!` followed by the `delnote` usage. |
| Repeated prefix | `delnote c/1 pn/Milo ty/pref ty/temp` | `Multiple values specified for the following single-valued field(s): ty/` |
| Filtered index | `note c/2 pn/Luna ty/sens nt/No perfume`, then `find Bob`, then `delnote c/1 pn/Luna ty/sens`. | Index 1 refers to Bob in the filtered list, so Luna's note is deleted. Run `list`: Alice is unchanged. |
| Restart | Add a note with `note`, delete a different one with `delnote`, then run `exit`, relaunch and run `list`. | The added note is still shown and the deleted note is still gone. |

### Finding clients

Use a disposable copy of the JAR in an empty folder so these tests do not affect real client data.
Launch it with `java -jar pawpals.jar` and use `FUTURE_DATE` as a valid future appointment date where needed.

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Find by name | `find Alice` | Clients whose names contain the full word `Alice` are listed. |
| Find by phone | `find 91234567` | Clients with that exact phone number are listed. |
| Find by email | `find alice@example.com` | Clients with that exact email are listed. |
| Partial phone | `find 9123` | No client is returned for the partial phone number. |
| Partial email | `find alice@example` | No client is returned for the partial email address. |
| Partial name | `find Ali` | No client is returned unless `Ali` is a complete name word. |
| Filtered index | `find Alice`, then `addp c/1 n/Coco s/Dog b/Poodle` | Client index 1 refers to the first client in the filtered list. |

### Finding pets

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Find one pet | `findp Milo` | Owners of pets named `Milo` are listed. |
| Find several pets | `findp Milo Luna` | Owners of pets named `Milo` or `Luna` are listed. |
| No match | `findp Rex` | No clients are listed and the result reports zero pets found. |
| No keyword | `findp` | An invalid command format message is shown. |
| Filtered index | `findp Milo`, then `addp c/1 n/Coco s/Dog b/Poodle` | Client index 1 refers to the first owner in the filtered list. |
| Future appointment | `findp Milo`, then `appt c/1 d/FUTURE_DATE t/12:00` | The appointment is added to the filtered owner. |

### Deleting a client

1. Deleting a client while all clients are being shown

   1. Prerequisites: List all clients using the `list` command, with multiple clients in the list.

   1. Test case: `delc 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delc 0`<br>
      Expected: No client is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delc`, `delc x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. Deleting a client from a filtered list

   1. Prerequisites: Create multiple clients, then use `find` so that the displayed list contains only the client to delete.

   1. Test case: `delc 1`<br>
      Expected: The filtered client is deleted. The status message shows the deleted contact's details, and no undisplayed client is affected.

   1. Test case: `list`<br>
      Expected: The deleted client no longer appears, while the other clients remain.

### Editing a pet

Set up one client with two pets, a note and a linked appointment (replace `FUTURE_DATE` with a future date):

```text
clear
addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road
addp c/1 n/Milo s/Dog b/Poodle
addp c/1 n/Luna s/Cat b/Persian
note c/1 pn/Milo ty/temp nt/Nervous around dryers
appt c/1 d/FUTURE_DATE t/14:00 pn/Milo
```

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Edit the breed | `editp c/1 pn/milo b/Toy Poodle` | `Edited pet: Milo (Dog, Toy Poodle)`. Milo keeps his ⚠ Temp note. |
| Rename a linked pet | `editp c/1 pn/Milo n/Max`, then open `data/pawpals.json`. | `Edited pet: Max (Dog, Toy Poodle)`. Max keeps the note. In the data file, the appointment's `pets` array is now `["Max"]`. |
| Rename to another pet's name | `editp c/1 pn/Max n/LUNA` | `This client already has a pet with the same name.` No data changes. |
| No field to edit | `editp c/1 pn/Max` | `At least one field to edit must be provided.` |
| Unknown pet | `editp c/1 pn/Rex b/Beagle` | `This client has no pet named Rex.` |
| Repeated prefix | `editp c/1 pn/Max b/Pug b/Pug` | `Multiple values specified for the following single-valued field(s): b/` |
| Invalid value | `editp c/1 pn/Max s/Dog!` | The species validation message from `addp`. |
| Restart | Run `exit`, relaunch and run `list`. | The pet is still named Max with breed Toy Poodle. In `data/pawpals.json`, the appointment's `pets` array is still `["Max"]`. |

### Deleting a pet

Set up one client with two pets, a note and a linked appointment (replace `FUTURE_DATE` with a future date):

```text
clear
addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road
addp c/1 n/Milo s/Dog b/Poodle
addp c/1 n/Luna s/Cat b/Persian
note c/1 pn/Milo ty/temp nt/Nervous around dryers
appt c/1 d/FUTURE_DATE t/14:00 pn/Milo pn/Luna
```

| Scenario                       | Commands or action                                   | Expected result                                                                                                      |
|--------------------------------|------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| Delete a pet                   | `delp c/1 pn/milo`                                   | `Deleted pet: Milo (owner: Alice Test)`. Milo and his note disappear from the card; Luna remains.                    |
| Appointment kept, pet unlinked | Look at Alice's card, then open `data/pawpals.json`. | The appointment is still shown on the card. In the data file, that appointment's `pets` array is now `["Luna"]`.     |
| Unknown pet                    | `delp c/1 pn/Rex`                                    | `This client has no pet named Rex.` No data changes.                                                                 |
| Missing prefix                 | `delp c/1`                                           | `Invalid command format!` followed by the `delp` usage.                                                              |
| Repeated prefix                | `delp c/1 pn/Luna pn/Luna`                           | `Multiple values specified for the following single-valued field(s): pn/`                                            |
| Invalid index                  | `delp c/0 pn/Luna`, then `delp c/2 pn/Luna`          | The first gives `Index must be a positive integer.`; the second gives `The client index provided is invalid.`        |
| Restart                        | Run `exit`, relaunch and run `list`.                 | Milo is still gone and the appointment is still shown. In `data/pawpals.json`, its `pets` array is still `["Luna"]`. |

### Adding appointments

Use a disposable copy of the JAR in an empty folder so these tests do not affect real client data.
Launch it with `java -jar pawpals.jar`. Replace `FUTURE_DATE` below with a valid future date in
`DD-MM-YYYY` format; do not type the placeholder literally.

Set up two clients using the implemented `addc` command:

```text
clear
addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road
addc n/Bob Test p/92345678 e/bob@example.com a/20 Dover Road
```

The separate client-add section of the User Guide also uses `addc`.

| Scenario                                | Commands or action                                                                                   | Expected result                                                                                                     |
|-----------------------------------------|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Valid appointment without recorded pets | `appt c/1 d/FUTURE_DATE t/12:34`                                                                     | Success names Alice Test; her card shows the date/time. Bob's card is unchanged.                                    |
| Duplicate for the same client           | Repeat the previous command.                                                                         | Overlap rejection identifies Alice and her existing slot; no extra appointment appears.                             |
| Same slot for another client            | `appt c/2 d/FUTURE_DATE t/12:34`                                                                     | Overlap rejection identifies Alice and her existing slot; no data changes.                                          |
| Invalid date                            | `appt c/1 d/31-04-2027 t/12:34`                                                                      | Date-validation message; no data changes.                                                                           |
| Invalid time                            | `appt c/1 d/FUTURE_DATE t/24:00`                                                                     | Time-validation message; no data changes.                                                                           |
| Missing field                           | `appt c/1 d/FUTURE_DATE`                                                                             | Invalid-format message and usage instructions; no data changes.                                                     |
| Repeated prefix                         | `appt c/1 d/FUTURE_DATE t/12:34 t/12:34`                                                             | Duplicate-prefix message for `t/`, even though the values match.                                                    |
| Invalid index                           | Try `c/0`, `c/abc`, and `c/3` in an otherwise valid appointment.                                     | Non-positive/non-integer values give the positive-integer message; `c/3` gives the invalid displayed-index message. |
| Past appointment                        | `appt c/1 d/01-01-2000 t/12:34`                                                                      | Appointment is added with the past-appointment note. Use `done c/1 d/01-01-2000 t/12:34 nt/Recorded visit` to move it into completed history. |
| Same-day boundary                       | Use today's date with a time at least five minutes ahead, then with the current minute.              | Both succeed when the slots do not overlap. A start earlier than the computer's local time includes the informational note. |
| Filtered indices                        | `find Bob`, then `appt c/1 d/FUTURE_DATE t/13:45`. Try the same command with `c/2`, then run `list`. | The first command updates Bob and keeps the filter. `c/2` is rejected. Alice remains unchanged after `list`.        |
| Restart                                 | Run `exit`, relaunch from the same folder, and run `list`.                                           | Each appointment is still displayed under its original client.                                                      |
| Edit preservation                       | `edit 2 p/93456789`, then restart.                                                                   | Bob's phone changes, and all his appointments remain.                                                               |
| Delete and clear                        | `delc 1`, restart; then `clear` and restart.                                                         | Alice and her appointments disappear on deletion; Bob retains his. Clearing leaves no clients or appointments.      |

For linked-pet checks, reset the disposable data with the two-client setup above, then run:

```text
addp c/1 n/Milo s/Dog b/Poodle
addp c/1 n/Sir Fluffy s/Cat b/Persian
addp c/2 n/Luna s/Cat b/Persian
appt c/1 d/FUTURE_DATE t/14:00 et/15:00 pn/sir   FLUFFY pn/MILO pn/Milo
find Bob
appt c/1 d/FUTURE_DATE t/15:00 et/16:00 pn/Luna
```

Alice's success feedback must contain `pn/Sir Fluffy pn/Milo` once each; Bob's adjacent booking succeeds
and keeps the filter. Booking for Bob at `14:30–15:30` with `pn/Luna` must reject the overlap with hidden
Alice. Booking for Bob with `pn/Milo` must reject the pet belonging only to Alice. An empty `pn/` must
reject the input. These failures must leave the client data, filter and saved file unchanged.
Restart and inspect the saved appointments' `pets` arrays to verify the recorded names and input order
are retained, together with their end times. Appointment-card presentation is a separate UI concern.

For stored details, close the app, add `"details": "Call on arrival"` to an appointment object in the
disposable `data/pawpals.json`, and restart. Expect `(Call on arrival)` after its date/time.
Empty or whitespace-only details must not add empty parentheses. The current `appt` command does not accept details.

Additional disposable-file checks:

* Remove a client's `appointments` property and restart: the client still loads, with no appointments.
* Change a stored appointment to `29-02-2024` and restart: it still loads even though it is in the past.
* Try an invalid stored date, invalid stored time, a null appointment entry, or two entries with the same date/time
  and different details: storage rejects the invalid file. The current startup behavior logs a load warning and
  starts with an empty PawPals data. Keep the corrupted file disposable; executing a later command can overwrite it.

Record completed checks and any remaining manual verification in the feature pull request.

### Rescheduling appointments

Use the disposable test folder described under [Adding appointments](#adding-appointments).
Replace `FUTURE_DATE` with a future date and `OTHER_FUTURE_DATE` with a different future date in
`DD-MM-YYYY` format. Reset to this setup before each independent scenario:

```text
clear
addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road
addc n/Bob Test p/92345678 e/bob@example.com a/20 Dover Road
addp c/1 n/Milo s/Dog b/Poodle
appt c/1 d/FUTURE_DATE t/14:00 et/15:30 pn/Milo
appt c/2 d/FUTURE_DATE t/16:00 et/17:00
find Alice
```

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Preserve duration and exclude original | `editappt c/1 d/FUTURE_DATE t/14:00 ns/14:30` | Succeeds with `et/16:00`, adjacent to Bob's booking. Alice's row updates immediately; the filter stays active. |
| Date only | `editappt c/1 d/FUTURE_DATE t/14:00 nd/OTHER_FUTURE_DATE` | Date changes; start remains 14:00 and feedback ends with `et/15:30`. |
| End only | `editappt c/1 d/FUTURE_DATE t/14:00 ne/15:45` | Feedback reports `et/15:45`; start and date remain unchanged. |
| Same-day end boundary | Try `ns/22:30`, then `ns/22:29`, in an otherwise valid edit of the 14:00 booking. | First edit rejects the midnight end and suggests `ne/`; second succeeds with `et/23:59`. |
| Explicit end overrides duration | `editappt c/1 d/FUTURE_DATE t/14:00 ns/23:00 ne/23:30` | Succeeds with the supplied end instead of preserving the 90-minute duration. |
| Start-only booking | Add `appt c/1 d/FUTURE_DATE t/18:00`, then edit that slot with `ns/19:00`. Edit the new 19:00 slot with `ne/20:00`. | First move retains no end; the second edit adds one. |
| Hidden conflict | `editappt c/1 d/FUTURE_DATE t/14:00 ns/16:00` | Rejects Bob's hidden booking. Alice's booking, filter and saved file remain unchanged. |
| Missing edits or malformed time | Use the original target with no edits, then with `ns/24:00`. | Corresponding error feedback appears; failed input remains for correction and the row is unchanged. |
| Invalid end or past start | Use the original target with `ne/14:00`, then with `nd/01-01-2000`. | Rejects the invalid end or non-future start without changing data. |
| Displayed index | Use `c/2` while only Alice is displayed. | Invalid displayed-index message; Bob is not edited. |
| Correct failed input | After a rejection, change the input to a valid edit and press Enter. | Error styling clears on editing; success feedback appears and the command box clears. |
| Save/reload and preservation | Make a valid move, exit, relaunch from the same folder and inspect the booking and saved file. | Only the edited slot changes. Linked pets, their order, details, other bookings, pet notes and history remain intact. Use the new date/start for subsequent commands. |

End times are confirmed in feedback and saved data; the current card shows its date, start and any
nonblank details. To test stored details, use the disposable-file procedure under Adding appointments.
Automated UI tests verify row replacement, feedback, filtering and retained failed input; these manual
checks also allow visual inspection in the packaged application.

### Cancelling and completing appointments

Use a disposable copy of the JAR in an empty folder, as in [Adding appointments](#adding-appointments).
Replace all date and time placeholders below; do not type them literally. Inspect `data/pawpals.json`
without editing it while PawPals is running. History checks use the saved file because completed sessions
are not currently displayed on the client card.

**Cancellation setup:** replace `FUTURE_DATE` with a valid future date in `DD-MM-YYYY` format.
Reset to this setup before each independent table row; perform commands within a row in order.

```text
clear
addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road
addc n/Bob Test p/92345678 e/bob@example.com a/20 Dover Road
addp c/1 n/Milo s/Dog b/Poodle
addp c/2 n/Luna s/Cat b/Persian
note c/1 pn/Milo ty/temp nt/Nervous around dryers
appt c/1 d/FUTURE_DATE t/14:00 et/15:00 pn/Milo
appt c/1 d/FUTURE_DATE t/16:00 et/17:00 pn/Milo
appt c/2 d/FUTURE_DATE t/18:00 et/19:00 pn/Luna
```

For every rejected command below, verify that the client cards, current filter and contents of
`data/pawpals.json` remain unchanged. Cancelling a booking must not add a history record.

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Cancel successfully | `cancel c/1 d/FUTURE_DATE t/14:00` | `Cancelled appointment: Alice Test d/FUTURE_DATE t/14:00`. Only Alice's 14:00 booking disappears. Her 16:00 booking, Milo and his note, both clients' contact details and Bob's booking remain. |
| Unknown slot | `cancel c/1 d/FUTURE_DATE t/13:00` | `Alice Test has no appointment at FUTURE_DATE 13:00.` No data changes. |
| Wrong index | `cancel c/3 d/FUTURE_DATE t/14:00`, then repeat with `c/0`. | The first gives `The client index provided is invalid.`; the second gives `Index must be a positive integer.` No data changes. |
| Repeated prefix | `cancel c/1 d/FUTURE_DATE t/14:00 t/14:00` | `Multiple values specified for the following single-valued field(s): t/`. The booking remains. |
| Unsupported end time | `cancel c/1 d/FUTURE_DATE t/14:00 et/15:00`, then repeat with an empty `et/` or `et/25:00`. | `cancel does not accept et/. Remove et/ and use the appointment's date and start time.`, followed by usage instructions. The booking remains. Remove `et/` and its value to cancel successfully. |
| Filtered index | `find Bob`, then `cancel c/1 d/FUTURE_DATE t/18:00`, then `list`. | Feedback identifies Bob Test's 18:00 booking. The filter remains active until `list`; Alice's bookings are unchanged. |
| Rebook the cancelled slot | `cancel c/1 d/FUTURE_DATE t/14:00`, then `appt c/2 d/FUTURE_DATE t/14:00 et/15:00 pn/Luna`. | Both succeed. The released 14:00–15:00 interval now belongs to Bob; Alice's other booking remains. |
| Cancellation survives restart | `cancel c/1 d/FUTURE_DATE t/14:00`, then `exit`, relaunch from the same folder and run `list`. | Alice's 14:00 booking is still absent from her card and saved `appointments` array. Her other booking, pets and notes and Bob's data remain; cancellation has not added history. |

**Completion setup:** create a future appointment to exercise both the pre-start rejection and completion boundary.
Past appointments can also be added with `appt` and completed immediately using their recorded date and start time.
Use today's local date for `FUTURE_DATE`, a `FUTURE_START_TIME` about two minutes ahead in `HH:mm` format,
and a `FUTURE_END_TIME` twenty minutes after that start. Both times must fit on today's date without
crossing midnight. Here `FUTURE_DATE` is the date of the booking whose start is initially in the future;
the same date and start are used after waiting. Reset to this setup with fresh times before each independent row.

```text
clear
addc n/Alice Test p/91234567 e/alice@example.com a/10 Dover Road
addc n/Bob Test p/92345678 e/bob@example.com a/20 Dover Road
addp c/1 n/Milo s/Dog b/Poodle
addp c/2 n/Luna s/Cat b/Persian
note c/1 pn/Milo ty/temp nt/Nervous around dryers
appt c/1 d/FUTURE_DATE t/FUTURE_START_TIME et/FUTURE_END_TIME pn/Milo
```

Wait until the computer's local clock reaches `FUTURE_START_TIME` before completing the appointment,
except in the future-completion rejection row. The planned end does not need to have passed.
Use the recorded **start**, rather than its end, to identify the booking. After a successful `done`,
inspect Alice's saved `history` array: exactly one new record must contain the original appointment
snapshot (including end time and `pets: ["Milo"]`), `notes: "Teddy cut"` and an ISO-8601 `loggedAt` instant.
The matching entry must be absent from her active `appointments` array. Milo and his care note remain.
For rejection rows, compare the saved file before and after and verify that no history is added or changed.

| Scenario | Commands or action | Expected result |
| --- | --- | --- |
| Reject completion before the start | Immediately after setup, run `done c/1 d/FUTURE_DATE t/FUTURE_START_TIME nt/Teddy cut` before the start time. | `An appointment cannot be completed before its scheduled start time.` The booking remains active and no history is added. If the start has already passed, reset with fresh times and retry. |
| Complete successfully | Wait until the start, then run `done c/1 d/FUTURE_DATE t/FUTURE_START_TIME nt/Teddy cut`. | `Completed session: Alice Test d/FUTURE_DATE t/FUTURE_START_TIME et/FUTURE_END_TIME pn/Milo nt/Teddy cut`. Alice's active row disappears and the saved history contains the snapshot and notes described above. Bob's data is unchanged. |
| Unknown slot | `done c/2 d/FUTURE_DATE t/FUTURE_START_TIME nt/Teddy cut` | `Bob Test has no active appointment at FUTURE_DATE FUTURE_START_TIME.` Alice's booking remains and no history is added. |
| Wrong index | `done c/3 d/FUTURE_DATE t/FUTURE_START_TIME nt/Teddy cut`, then repeat with `c/0`. | The first gives `The client index provided is invalid.`; the second gives `Index must be a positive integer.` No data changes. |
| Repeated prefix | `done c/1 d/FUTURE_DATE t/FUTURE_START_TIME nt/Teddy cut nt/Short ears` | `Multiple values specified for the following single-valued field(s): nt/`. No booking or history changes. |
| Complete twice | Wait until the start, run `done c/1 d/FUTURE_DATE t/FUTURE_START_TIME nt/Teddy cut`, then repeat it. | The first succeeds; the second gives `Alice Test already has a completed session at FUTURE_DATE FUTURE_START_TIME.` Only one history record exists; its notes and logging time are unchanged by the second attempt. |
| Rebook time released by early completion | Before completing, choose `REBOOK_TIME` about ten minutes after `FUTURE_START_TIME`, strictly before `FUTURE_END_TIME`. Run `appt c/2 d/FUTURE_DATE t/REBOOK_TIME et/FUTURE_END_TIME pn/Luna`. Wait until Alice's start, complete her booking with `done c/1 d/FUTURE_DATE t/FUTURE_START_TIME nt/Teddy cut`, then retry the same `appt`. | The first booking attempt identifies an overlap with Alice. After completion the booking succeeds for Bob, despite overlapping Alice's historical interval. Alice's completed record remains. If `REBOOK_TIME` has passed, the booking is still accepted with an informational note. |
| Completion survives restart | Wait until the start, run `done c/1 d/FUTURE_DATE t/FUTURE_START_TIME nt/Teddy cut`, inspect the saved record, then `exit`, relaunch from the same folder and run `list`. | The active booking stays absent. Alice's saved history retains exactly the same snapshot, notes and logging time; Milo, his care note and Bob's data remain. Repeating `done` at that slot still reports an already-completed session. |

### Saving data

Use a disposable copy of the JAR in an empty folder, and keep a backup of `data/pawpals.json` before editing it.

1. Missing data file

   1. Close PawPals, delete `data/pawpals.json`, and relaunch.<br>
      Expected: The sample clients are shown. Running any command, such as `list`, creates a new `data/pawpals.json`.

1. Corrupted data file

   1. Close PawPals. In `data/pawpals.json`, change a client's `phone` to `abc`, or delete a closing `}`. Relaunch.<br>
      Expected: PawPals creates a uniquely named `.corrupt-*.bak` backup and shows a recovery warning before
      accepting commands. The warning identifies the backup and offers **Continue** or **Exit**.
   1. Repeat the corrupted-data test and choose **Exit**. Relaunch or inspect the files.<br>
      Expected: The rejected data file and its backup remain unchanged, and no empty replacement is written.
   1. Repeat the corrupted-data test and choose **Continue**, then run `list`.<br>
      Expected: The command is accepted, the empty client list is shown, and the data file is replaced only
      after the explicit confirmation.

1. Data file from an older version

   1. Close PawPals. In `data/pawpals.json`, remove the `notes` field from a pet, and the `endTime` and `pets` fields
      from an appointment. These fields did not exist in v1.3. Relaunch.<br>
      Expected: The data loads. That pet shows no notes, and that appointment shows only its date and start time.

1. New data is kept after restarting

   1. Add a pet note with `note`, an appointment with an end time and a linked pet (e.g. `appt c/1 d/FUTURE_DATE
      t/14:00 et/15:30 pn/Milo`), then run `exit` and relaunch.<br>
      Expected: The note and appointment are still shown. In `data/pawpals.json`, the appointment has `endTime` and `pets`.
