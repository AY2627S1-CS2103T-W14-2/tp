---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

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
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
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

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

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

### Finding contacts by project

The `findproject KEYWORD` command implements UC02 using the existing filtered contact list.

1. `AddressBookParser` routes `findproject` to `FindProjectCommandParser`.
2. The parser removes surrounding whitespace, rejects a blank phrase, and checks the 40-character limit before
   collapsing internal whitespace. Length is measured in Unicode code points, consistent with the project model.
   The surrounding-whitespace rule covers the same whitespace and Unicode separator characters as `Project.normalize()`.
3. `ProjectContainsKeywordPredicate` uses `Project.normalize()` and `Locale.ROOT` to normalise the search phrase
   once. It checks whether any project returned by `Person.getProjects()` contains that whole phrase, ignoring case.
4. `FindProjectCommand` passes the predicate to `Model.updateFilteredPersonList(...)` and returns the result count
   or a specific no-matches message. The UI observes this list and displays the existing contact cards and indices.

The predicate filters contacts rather than collecting a result for each matching project, so a contact appears at
most once. Replacing the predicate searches the full underlying address book, even after another search has
filtered the displayed list. `list` restores all contacts; `delete` continues to use the displayed indices.
Search does not change contacts or project associations. `LogicManager` retains its normal save-after-command
behaviour, saving the complete address book rather than just the search results. Invalid input fails during parsing,
before the filter or saved data is changed.

The search keyword limit is 40 characters, as specified in UC02; stored project names can contain up to 50 characters.
The parser therefore validates the search phrase separately from `ParserUtil.parseProject()`.

**Design choices:** A separate command preserves the existing name-search behaviour. Whole-phrase substring matching
supports partial project names without treating individual words as alternatives. Reusing the model's existing
filter avoids introducing a separate project index or changes to storage and the UI.

### Project association implementation

The `project INDEX pr/PROJECT` command implements UC03. `AddressBookParser` delegates to
`ProjectCommandParser`, which parses the displayed index and requires exactly one project prefix.
`ParserUtil.parseProject` reuses the shared `Project` validation and normalisation rules.

`ProjectCommand` resolves the index against `Model.getFilteredPersonList()`, checks whether the
normalised project already exists, and copies the contact's immutable project list before appending
the new project. It constructs a replacement `Person` with all other fields preserved and calls
`Model.setPerson`. Keeping the active predicate preserves the user's search context.

Projects are stored as an ordered list to retain display order. Duplicate detection uses `Project.equals`,
so casing and redundant whitespace do not create separate associations. Invalid indices and duplicate
associations throw `CommandException` before any model mutation; malformed arguments throw `ParseException`.

The existing `LogicManager` saves the updated address book through JSON storage after execution.
No new storage format or UI component is required: both already support a contact's project list.
Command tests cover filtered indices, preservation of fields and order, and duplicate rejection.
Parser tests cover malformed input and length boundaries. An integration test exercises command dispatch,
filtered selection, rejected operations, and a JSON round trip.

### Undo feature
The existing **Implementation** section uses a feature heading, explains the command flow, 
then describes key behavior and design choices. Here’s a paste-ready section for undo:

### Undo command implementation

The `undo` command restores the address book to the state before the most recent successful command that 
supports undo. It supports `add`, `edit`, `delete`, `clear`, and `project`. Only one previous state is kept,
so undo can be used once for each such command.

When `LogicManager.execute(...)` receives a command, it checks `Command.isUndoable()`. For an undoable 
command, it copies the current address book before execution. If execution succeeds, `LogicManager`
saves that copy through `Model.saveUndoState(...)`, replacing any previously saved state. Commands that
are not undoable do not replace the snapshot. If a command fails, the snapshot is not updated.

`UndoCommand` checks `Model.canUndo()` before restoring anything. If a snapshot exists, `ModelManager.undo()`
restores it with `AddressBook.resetData(...)`, clears the snapshot, and updates the filtered person list to
show all contacts. The regular storage save then persists the restored address book. If no snapshot exists,
the command reports `No command to undo.`

The snapshot is held only in memory and is not persisted, so it is unavailable after restarting the application.
A new undoable command replaces the previous snapshot, and an undo consumes the snapshot. Failed commands and 
non-undoable commands preserve it.

**Design choice:** The implementation copies the entire address book rather than storing command-specific 
inverse data. This makes restoration straightforward and preserves contact details and ordering, at the cost 
of keeping one full copy in memory.


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

* is a student who works on multiple courses or projects and needs to manage
  classmates, teammates, and other project contacts
* needs to keep contact details such as names, phone numbers, email addresses,
  Telegram usernames, and project tags together in one place
* prefers a lightweight desktop application and is reasonably comfortable with
  keyboard-driven CLI commands
* values quick contact lookup and updates over navigating a feature-heavy
  contacts application

**Value proposition**: LinkUp helps students quickly add, organize, and retrieve
  project-related contacts and their details from one keyboard-driven address
  book, reducing the need to search across separate course or project contact
  lists.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …  | I want to …                                                            | So that I can…                                                    |
|----------|---------|------------------------------------------------------------------------|-------------------------------------------------------------------|
| `* * *`  | student | add a contact                                                          | save the details of a project mate                                |
| `* * *`  | student | view all my contacts                                                   | see the people whose information I have saved                     |
| `* * *`  | student | delete a contact                                                       | remove contacts I no longer need                                  |
| `* * *`  | student | search for a contact by name                                           | quickly find someone                                              |
| `* * *`  | student | associate a contact with a project                                     | remember where I know the person from                             |
| `* * *`  | student | search for contacts by project                                         | find members of a particular project                              |
| `* * *`  | student | distinguish contacts with the same name using their project information | identify the correct person                                      |
| `* *`    | student | edit a contact's details                                               | keep my contacts' information up to date                          |
| `* *`    | student | store a contact's phone number                                         | contact them by phone or messaging apps                           |
| `* *`    | student | store a contact's email address                                        | contact them by email                                             |
| `* *`    | student | store a contact's Telegram username                                    | contact them on Telegram                                          |
| `* *`    | student | associate one contact with multiple projects                           | avoid duplicating entries for the same person                     |
| `* *`    | student | view the projects associated with a contact                            | remember how I know them                                          |
| `* *`    | student | remove a contact from a project without deleting the contact           | maintain accurate project information                            |
| `* *`    | student | search using part of a person's name                                   | find someone without remembering their full name                  |
| `* *`    | student | view all contacts belonging to the same project                        | quickly see my teammates                                          |
| `* *`    | student | add notes about a contact                                              | remember useful information about them                            |
| `* *`    | student | record a contact's role in a project                                   | remember their responsibilities                                  |
| `* *`    | student | search for contacts by their role                                      | find the person responsible for a particular task                 |
| `* *`    | student | see a contact's complete information                                   | verify that I have found the correct person                       |
| `* *`    | student | detect duplicate contact information                                   | avoid accidentally saving the same person multiple times         |
| `* *`    | student | be warned when two contacts have the same name                         | know that additional information may be needed to distinguish them |
| `* *`    | student | store contacts when some optional information is unavailable           | save a person without knowing every detail                        |
| `* *`    | student | find a contact without remembering the exact capitalisation of their name | search conveniently                                             |
| `* *`    | student | see which project caused a search result to match                      | understand why a contact was returned                            |
| `*`      | student | search contacts using multiple criteria                                | narrow down ambiguous results                                    |
| `*`      | student | tag contacts                                                           | organise them using categories meaningful to me                  |
| `*`      | student | search contacts by tag                                                 | quickly retrieve a group of related contacts                     |
| `*`      | student | sort contacts alphabetically                                           | browse the contact list more easily                              |
| `*`      | student | sort contacts by project                                               | see related contacts together                                    |
| `*`      | student | view recently added contacts                                           | quickly find people I just met                                   |
| `*`      | student | archive contacts from completed projects                               | prevent old contacts from cluttering my active contact list      |
| `*`      | student | restore archived contacts                                              | reuse their information if I work with them again                |
| `*`      | student | rename a project                                                       | keep project information accurate when project names change      |
| `*`      | student | remove a project                                                       | prevent completed or incorrectly created projects from cluttering my records |
| `*`      | student | view all projects I am tracking                                        | see how my contacts are organised                                |

### Use cases

The following use cases describe the intended behaviour of LinkUp.
For all use cases, the **System** is `LinkUp` and the **Actor** is a student managing project contacts.
The application is running. **MSS** stands for Main Success Scenario.

#### UC01: Find a project mate by name

**MSS**

1. Student requests to search for contacts using a name keyword.
2. LinkUp displays contacts whose names contain the keyword, ignoring case, together with their contact details and projects.
3. Student uses the displayed project information and contact details to identify the intended project mate.

Use case ends.

**Extensions**

* 1a. The keyword is empty or exceeds 60 characters after trimming surrounding spaces.

  * 1a1. LinkUp displays an error message without changing saved contacts.

  Use case resumes at step 1.

* 2a. No contacts match the keyword.

  * 2a1. LinkUp displays a message indicating that no contacts were found.

  Use case ends.

#### UC02: Find contacts belonging to a project

**MSS**

1. Student requests to search for contacts using a project keyword.
2. LinkUp displays contacts associated with projects whose names contain the keyword, ignoring case.
3. Student reads the displayed contact details and project information to find the relevant project mates.

Use case ends.

**Extensions**

* 1a. The keyword is empty or exceeds 40 characters after trimming surrounding spaces.

  * 1a1. LinkUp displays an error message without changing saved contacts.

  Use case resumes at step 1.

* 2a. No contacts belong to a matching project.

  * 2a1. LinkUp displays a message indicating that no contacts were found for the project keyword.

  Use case ends.

#### UC03: Associate an existing contact with a project

**MSS**

1. Student requests to list contacts.
2. LinkUp displays the saved contacts with their indices, contact details, and projects.
3. Student requests to associate a contact at a displayed index with a project, supplying the project name.
4. LinkUp adds the project association to that contact, preserves its existing project associations, and displays a success message.

Use case ends.

**Extensions**

* 2a. There are no saved contacts.

  * 2a1. LinkUp displays a message indicating that no contacts have been saved.

  Use case ends.

* 3a. The index is missing, is not a positive integer, or does not refer to a contact in the displayed list.

  * 3a1. LinkUp displays an index error without changing saved contacts.

  Use case resumes at step 3.

* 3b. The project name is missing or invalid, or the project parameter is repeated.

  * 3b1. LinkUp displays the relevant input error without changing saved contacts.

  Use case resumes at step 3.

* 3c. The contact is already associated with the same normalised project.

  * 3c1. LinkUp displays a message indicating that the association already exists, without changing saved contacts.

  Use case resumes at step 3.

#### UC04: Delete a contact

**MSS**

1.  **Environment:** The system shall run on _mainstream OS_ with Java `25` or above installed.
1. Student requests to list contacts.
2. LinkUp displays the saved contacts with their indices, contact details, and projects.
3. Student identifies the contact to remove and requests its deletion using its displayed index.
4. LinkUp deletes only the selected contact and displays a success message.

Use case ends.

**Extensions**

* 2a. There are no saved contacts.

  * 2a1. LinkUp displays a message indicating that no contacts have been saved.

  Use case ends.

* 3a. The index is missing, is not a positive integer, or does not refer to a contact in the displayed list.

  * 3a1. LinkUp displays an index error without deleting any contact.

  Use case resumes at step 3.

### Non-Functional Requirements

1.  **Environment:** The system shall run on all _mainstream OS_ with Java `25` or above installed.
2.  **Capacity:** The system shall support at least 1000 saved contacts without noticeable sluggishness during typical usage.
3.  **Performance:** The system shall return results within 2 seconds when searching using name or project for a contact list of up to 1000 contacts.
4.  **Usability:** The system shall display a clear success message or a specific error message after every command.
5.  **Interaction efficiency:** A user with above-average typing speed for regular English text should be able to complete most tasks faster using commands than using mouse interactions.
6.  **Data integrity:** If a command fails because of invalid input, an invalid index, or a duplicate, the system shall leave the contact list unchanged.
7.  **Data consistency:** The system shall validate and normalise contact and project data consistently before storing or searching it.
8.  **Persistence and reliability:** The system shall preserve saved contacts between application sessions and shall display a clear error message instead of crashing when the data file cannot be read.

### Glossary

* **Contact**: A saved record for a project mate, containing their name, contact details, and associated projects.
* **Project**: A module project or team that a contact belongs to, such as CS2103T or Orbital.
* **Project association**: A link between a contact and a project. A contact can be associated with more than one project.
* **Displayed contact list**: The contacts currently shown in LinkUp, including results of a search.
* **Index**: A one-based number identifying a contact in the currently displayed contact list.
* **Same-name disambiguation**: Using project information to distinguish contacts with identical or similar names.
* **Mainstream OS**: Windows, Linux, Unix, or macOS.

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

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
