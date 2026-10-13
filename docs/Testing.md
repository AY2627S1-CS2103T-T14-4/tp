---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Appointment release verification

For appointment release checks, run the focused tests from the repository root:

```text
./gradlew test --tests 'seedu.address.logic.AppointmentIntegrationTest' --tests 'seedu.address.logic.commands.AppointmentCommandTest' --tests 'seedu.address.logic.parser.AppointmentCommandParserTest' --tests 'seedu.address.model.appointment.*' --tests 'seedu.address.storage.JsonAdaptedAppointmentTest' --tests 'seedu.address.storage.JsonAdaptedClientTest' --tests 'seedu.address.storage.JsonAddressBookStorageTest' --tests 'seedu.address.model.client.ClientTest' --tests 'seedu.address.logic.commands.EditCommandTest' --tests 'seedu.address.logic.parser.AddressBookParserTest'
```

On Windows, use `./gradlew.bat` instead of `./gradlew`. Then run `./gradlew check coverage javadoc shadowJar`
and the repository checks (`bash .github/run-checks.sh`, using Git Bash on Windows).
See the [manual appointment scenarios](DeveloperGuide.md#adding-appointments) and
[verification record](AppointmentVerification.md) for the expected results and remaining checks.

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
