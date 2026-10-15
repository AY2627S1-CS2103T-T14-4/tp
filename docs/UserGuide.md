---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# PawPals User Guide

PawPals is a **desktop application for managing clients, pets, and appointments, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). PawPals helps pet groomers manage their client records and schedules quickly.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `addressbook.jar` file from the [PawPals releases page](https://github.com/AY2627S1-CS2103T-T14-4/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for PawPals.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample client data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all clients, their pets, and their appointments.

   * `addc n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a client named `John Doe`.

   * `addp c/1 n/Milo s/Dog b/Poodle` : Adds a pet named `Milo` to the 1st client shown.

   * `appt c/1 d/12-12-2026 t/14:00` : Adds an appointment for the 1st client shown.

   * `delc 3` : Deletes the 3rd client shown in the current list.

   * `clear` : Deletes all clients, pets, and appointments.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `addc n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the PawPals help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a client: `addc`

Adds a new client and their contact details.

Format: `addc n/NAME p/PHONE e/EMAIL a/ADDRESS`

Example: `addc n/Alice Tan p/91234567 e/alicetan@example.com a/10 Dover Road`

* `NAME` must contain 1 to 60 letters, spaces, apostrophes, or hyphens. Leading and trailing spaces are removed, and repeated spaces are treated as one space.
* `PHONE` must contain exactly eight digits.
* `EMAIL` must contain a non-empty local part, an `@` symbol, and a valid domain with at least one period. Leading and trailing spaces are removed.
* `ADDRESS` must contain between 1 and 200 characters. Leading and trailing spaces are removed.

Successful output: `New client added: Alice Tan`

If the command fails, the corresponding validation message is displayed and no client is added.

Clients with the same name and phone number, ignoring name capitalisation and repeated spaces, are treated as duplicates.

<box type="tip" seamless>

**Tip:** A client can have any number of tags, including zero.
</box>

Examples:
* `addc n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `addc n/Betsy Crowe e/betsycrowe@example.com a/Newgate Prison p/1234567`

### Adding a pet: `addp`

Adds a pet belonging to an existing client.

Format: `addp c/CLIENT_INDEX n/NAME s/SPECIES b/BREED`

Example: `addp c/1 n/Milo s/Dog b/Poodle`

* `CLIENT_INDEX` must be a positive integer corresponding to an existing client in the displayed client list.
**Note:** `CLIENT_INDEX` refers to the index shown in the currently displayed client list. After using `find`, the index refers to the filtered search results, not the original full list.
For example, after running `find Alice`, if Alice appears at index 1, `addp c/1 n/Milo s/Dog b/Poodle` adds Milo to Alice.
* `NAME` must contain 1 to 40 letters, numbers, spaces, apostrophes, or hyphens. Leading and trailing spaces are removed, and repeated spaces are treated as one space.
* `SPECIES` must contain 1 to 40 letters, numbers, spaces, apostrophes, or hyphens. Leading and trailing spaces are removed, and repeated spaces are treated as one space.
* `BREED` must contain 1 to 60 letters, numbers, spaces, apostrophes, or hyphens. Leading and trailing spaces are removed.

Successful output: `New pet added: Milo`

After a successful command, the pet appears under the selected client's card in the format
`Pet: Milo (Dog, Poodle)`. The pet is saved as part of that client and remains available after restarting PawPals.

If the command fails, the corresponding validation message is displayed and no pet is added. A pet is a duplicate only when it has the same name as another pet belonging to the same client, ignoring name capitalisation and repeated spaces.

### Deleting a client: `delc`

Removes an unwanted client record, all their pets, and all their appointments.

Format: `delc CLIENT_INDEX`

Example: `delc 2`

* `CLIENT_INDEX` must be a positive integer corresponding to an existing client in the displayed client list.

Successful output, if the selected client is Ben Lim:<br>
`Deleted client: Ben Lim; Phone: 91234567; Email: benlim@example.com; Address: 10 Dover Road; Tags: `

The message lists the deleted client's details so you can confirm the right client was removed.

The deleted client disappears from the client list, and the remaining clients are renumbered. If the command fails, no client, pet, or appointment is deleted.

### Adding an appointment: `appt`

Adds an appointment to an existing client. An appointment belongs to the client rather than to a specific pet, so it may cover one or more of the client's pets. You can also schedule an appointment for a client with no recorded pets; this command does not select individual pets.

Format: `appt c/CLIENT_INDEX d/DD-MM-YYYY t/HH:mm`

Example: `appt c/1 d/12-12-2026 t/12:34`

* All three fields are required and may appear in any order. Each prefix (`c/`, `d/`, and `t/`) must appear exactly once, even if repeated values are identical. Do not put extra text before the first prefix.
* `CLIENT_INDEX` must be a positive integer corresponding to an existing client in the **currently displayed** list. After `find`, index `1` refers to the first matching client, rather than the first client in the full list. Adding an appointment keeps the current filter.
* `DD-MM-YYYY` must be a valid calendar date with a year from `0001` to `9999`. Use two digits for the day and month, for example `09-01-2027`. Impossible dates such as `31-04-2027` and non-leap-year dates such as `29-02-2027` are rejected.
* `HH:mm` must be a valid time in 24-hour format, from `00:00` to `23:59`. Use two digits for both parts; seconds and AM/PM are not accepted.
* The **combined date and time** must be strictly later than the computer's current date and time when the command executes, using its local time zone. Later today is valid. For example, at 10:30 today, today's `t/10:31` is valid but `t/10:30` is not.
* The same date and time cannot be added twice for the **same client**, even if stored appointment details differ. Different clients may have appointments at the same date and time; PawPals does not check overlaps or available staff.
* `t/` means time in `appt`. It means a tag in commands that accept tags. The `appt` command does not accept tags or appointment details.

Successful output, if the selected client's name is Ben Lim: `Added appointment: Ben Lim d/12-12-2026 t/12:34`

The appointment's date and time appear under the selected client's contact information and are saved automatically. Stored nonblank details are also displayed when present. Appointments remain available after restarting PawPals, including after their scheduled time has passed. Editing the client's contact information preserves their appointments; deleting the client or clearing all records removes them.

If the command is rejected because of invalid input or a duplicate appointment, no appointment is added. Validation feedback includes:

| Problem | Feedback |
| --- | --- |
| Missing required prefix or extra text before the first prefix | `Invalid command format!`, followed by the `appt` usage instructions. |
| Repeated prefix, for example two `t/` values | `Multiple values specified for the following single-valued field(s): t/` |
| Empty, zero, negative, non-integer, or too-large index | `Index must be a positive integer.` |
| Positive index outside the displayed list | `The client index provided is invalid.` |
| Invalid or empty date | `Appointment dates must be valid calendar dates in DD-MM-YYYY format, with a year from 0001 to 9999.` |
| Invalid or empty time | `Appointment times must be valid times in 24-hour HH:mm format, from 00:00 to 23:59.` |
| Past or current date/time | `Appointment date and time must be strictly in the future.` |
| Existing date/time for the selected client | `This client already has an appointment at that date and time.` |

The example date must still be in the future when you try it. Replace it with a future date if needed.

If PawPals reports a save error, the appointment may still appear during the current session but may not survive a restart. Resolve the file or folder access problem before relying on the appointment being saved.

### Listing all clients: `list`

Shows an overview of all clients, their appointments, and their pets.

Format: `list`

Example: `list`

Each client entry displays the client's phone number, address, email, pets, and appointments. Each pet is shown
under its client with its name, species, and breed. Clients without pets continue to display without a pet entry.

### Editing a client: `edit`

Edits an existing client in PawPals.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... `

* Edits the client at the specified `INDEX`. The index refers to the index number shown in the displayed client list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the client's existing tags are removed; adding tags is not cumulative.
* To remove all of a client's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st client to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd client to be `Betsy Crower` and clears all existing tags.

### Locating clients by name: `find`

Finds clients whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Clients matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Clearing all entries: `clear`

Clears all clients, pets, and appointments from PawPals.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

PawPals automatically saves client, pet, and appointment data after every command. Pets are saved under their
owning client and remain available after restarting PawPals. You do not need to save manually.

### Editing the data file

PawPals data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, PawPals starts with an empty client list at the next run. The invalid file remains on disk until you run a command (PawPals saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause PawPals to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install PawPals on the other computer and overwrite the data file it creates with the data file from your previous PawPals home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add client** | `addc n/NAME p/PHONE e/EMAIL a/ADDRESS`<br> e.g., `addc n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665`
**Add pet** | `addp c/CLIENT_INDEX n/NAME s/SPECIES b/BREED`<br> e.g., `addp c/1 n/Milo s/Dog b/Poodle`
**Clear**  | `clear`
**Delete client** | `delc CLIENT_INDEX`<br> e.g., `delc 3`
**Add appointment** | `appt c/CLIENT_INDEX d/DD-MM-YYYY t/HH:mm`<br> e.g., `appt c/1 d/12-12-2026 t/12:34`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Exit**   | `exit`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**Help**   | `help`
