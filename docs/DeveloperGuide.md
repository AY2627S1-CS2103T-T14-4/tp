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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `ClientListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Client` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a client).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Client` objects (which are contained in a `UniqueClientList` object).
* stores the `Client` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Client>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Client` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Client` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th client in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new client. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the client was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the client being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
| `*`      | pet groomer ready to start using PawPals for my work | purge the sample data                                        | start with a clean database for my actual business                                      |
| `* * *`  | pet groomer onboarding a new client                  | add a new client                                             | keep their information for future visits                                                |
| `* * *`  | pet groomer                                          | record a client’s contact details and home address           | refer to their contact and location information when needed                             |
| `* * *`  | pet groomer                                          | add a pet belonging to a client                              | track distinct grooming needs and histories for different pets owned by the same client |
| `* * *`  | pet groomer                                          | record a pet’s basic details                                 | identify and understand the pet I am grooming                                           |
| `* *`    | pet groomer                                          | record a pet’s temperament and behavioural concerns          | handle it safely and appropriately                                                      |
| `* *`    | pet groomer                                          | record grooming-related sensitivities                        | avoid unsuitable products or procedures                                                 |
| `* *`    | pet groomer                                          | record a pet’s grooming preferences and special instructions | provide consistent service without relying on memory                                    |
| `* * *`  | pet groomer                                          | view a summary of available commands                         | quickly learn or remember how to use the application                                    |
| `* * *`  | pet groomer planning my schedule                     | schedule an appointment for a client                 | keep track of who I am grooming and when                                                |
| `* * *`  | pet groomer                                          | view my upcoming appointments                                | plan my day efficiently                                                                 |
| `* * *`  | pet groomer preparing for a visit                    | view an appointment’s date, time, client, pet and address   | arrive at the correct location prepared for the visit                                   |
| `* *`    | pet groomer preparing to groom on-site               | view important pet notes before grooming                     | prepare to handle the pet appropriately                                                 |
| `* *`    | pet groomer handling schedule adjustments            | update an appointment                                        | keep the appointment details accurate                                                   |
| `* * *`  | pet groomer                                          | cancel an appointment                                        | keep my schedule up to date                                                             |
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
2. User identifies the client and provides a date and time.
3. PawPals validates that the date and time form a valid future appointment.
4. PawPals adds the appointment to the client.
5. PawPals displays a success message and the appointment under the client.

Use case ends.

Extensions:

* 2a. User provides an invalid date or time.
  * 2a1. PawPals displays the corresponding date or time validation message.
  * 2a2. User provides corrected details.
  * Use case resumes from step 3.
* 3a. The appointment is not strictly in the future.
  * 3a1. PawPals informs the user that the appointment must be scheduled in the future.
  * Use case ends.
* 4a. The client already has an appointment at the same date and time.
  * 4a1. PawPals informs the user that the appointment already exists.
  * Use case ends.

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
2. PawPals searches client names without regard to letter case.
3. PawPals displays the matching clients and their information.

Use case ends.

Extensions:

* 1a. User provides no keywords.
  * 1a1. PawPals displays an error message explaining that at least one keyword is required.
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

### Non-Functional Requirements

1. PawPals should work on any mainstream OS as long as it has Java 25 or above installed.
2. For a dataset of up to 100 clients and their associated pet and appointment records, common commands such as list and find should complete and update the displayed results within 1 second.
3. All core PawPals workflows should be completable using keyboard input without requiring mouse interaction.
4. Client, pet, and appointment data that has been successfully saved should remain intact and available after PawPals is closed and subsequently relaunched.
5. PawPals should support at least 100 clients and their associated pet and appointment records.

### Glossary

* **Client**: A customer of the pet grooming business whose contact and home address information is stored in PawPals. A client may have one or more pets.
* **Pet**: An animal belonging to a client whose grooming-related information is managed in PawPals.
* **Appointment**: A scheduled future grooming visit associated with a client, not a specific pet.
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
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a client

1. Deleting a client while all clients are being shown

   1. Prerequisites: List all clients using the `list` command, with multiple clients in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No client is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
