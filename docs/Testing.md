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

## Types of tests

Run `./gradlew check coverage` (Windows: `.\gradlew.bat check coverage`) to run the full test suite,
Checkstyle, and the coverage check. At least 95% of production source lines must be covered; no UI or startup
classes are excluded. Branch coverage is reported separately and is not subject to this line-coverage threshold.
The HTML report is at `build/reports/jacoco/coverage/html/index.html`; CI uploads the corresponding
`build/reports/jacoco/coverage/coverage.xml` report to Codecov.

The JavaFX tests need a graphical session and briefly open application windows. On Linux without a display,
use `xvfb-run -a ./gradlew check coverage` after installing Xvfb. CI uses this command on Linux.
Tests use temporary directories for contact data and preferences. Run the complete suite when checking coverage;
a filtered test run alone is not expected to meet the project-wide threshold.

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
