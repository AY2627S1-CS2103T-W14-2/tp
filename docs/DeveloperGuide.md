---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# LinkUp Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

LinkUp builds on [se-edu AddressBook Level 3](https://github.com/se-edu/addressbook-level3).
The layered architecture, command infrastructure, and parts of this documentation originate from that project.
The application uses JavaFX for its interface and Jackson for JSON storage; dependencies and versions are
declared in `build.gradle`. Documentation is built with MarkBind.

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

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-W14-2/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-W14-2/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-W14-2/tp/blob/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `RecentFindPanel`,
`PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart`
class, which captures common behavior among classes that represent visible GUI parts.

`RecentFindPanel` displays names, Telegram usernames, and project tags in a compact,
independently scrolling list above `PersonListPanel`.

Below the command box, `MainWindow.fxml` places the result box, the two search sections and the contact list in
a vertical `SplitPane` (`mainSections`), so users can drag the dividers to give any section more space.
Each section has a minimum height small enough for all three to fit, together with the command box and status bar,
in the 600-pixel minimum window height. `MainWindow` sets the starting divider positions after the window is shown,
so they apply to the final window size. Divider positions are not saved between sessions.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-W14-2/tp/blob/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-W14-2/tp/blob/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-W14-2/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

`DeleteCommand` takes the contact `p` at index 1 of the displayed list and asks the model to delete that contact.

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, it passes the text to `AddressBookParser`.
   Commands with structured arguments use a matching parser (e.g., `DeleteCommandParser`); commands such as
   `list`, `help`, and `undo` are constructed directly.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   For simplicity, the diagram shows only a successful deletion; checking the index, recording the undo history and saving to storage are left out.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* For commands with structured arguments, `AddressBookParser` creates an `XYZCommandParser` (`XYZ` stands for
  the command name, e.g., `Add`). That parser validates arguments and returns an `XYZCommand` object as a `Command`.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-W14-2/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* exposes a `FilteredList<Person>` backed by the address book's observable list. Changing its predicate updates
  the displayed results without removing contacts from the underlying address book.
* maintains a separate `FilteredList<Person>` for the latest search, exposed by `Model.getRecentFindList()` and
  `Logic.getRecentFindPersonList()`. Both lists observe the same underlying contacts.
* stores a `UserPrefs` object containing GUI settings. This is exposed to the
  outside as a `ReadOnlyUserPrefs` object.
* keeps undo snapshots in an in-memory `Deque<AddressBook>`; they are not stored in JSON.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)




### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-W14-2/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

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

### Current command and contact model

The MVP includes `add`, `list`, `find`, `findproject`, `project`, `delete`, `clear`, `undo`, `help`, and `exit`,
with same-name support, duplicate-detail validation and persistence. `edit` was outside the MVP release and now
goes beyond it, covering name, phone, email and Telegram username; see
[Edit command implementation](#edit-command-implementation).

The Java package remains `seedu.address`, and internal names such as `AddressBook` are retained from AB-3.
`AddressBookParser` dispatches `add`, `edit`, `delete`, `clear`, `find`, `findproject`, `list`, `project`,
`projects`, `replaceproject`, `deleteproject`, `renameproject`, `sort`, `undo`, `help`, and `exit`.
Command words are matched case-insensitively: `AddressBookParser` lowercases the command word with `Locale.ROOT`
before dispatching, so `FIND alex` works like `find alex` even on a computer set to Turkish, where a plain
`toLowerCase()` would turn `I` into a dotless `ı`. Prefixes and arguments are not changed, so prefixes stay
case-sensitive (`N/` is not `n/`).

Before trimming or regex matching, `AddressBookParser.parseCommand` checks the raw `String.length()`
against `MAX_COMMAND_LENGTH` (1,000 UTF-16 code units). This includes the command word, prefixes, values,
and all leading, internal and trailing whitespace; supplementary Unicode characters count as two units.
Oversized input throws `ParseException` with `MESSAGE_COMMAND_TOO_LONG`, before command dispatch or
execution, so contacts, filters, search history and storage remain unchanged. The constant-time length
check bounds the input passed to command parsers, preventing long whitespace runs from causing excessive
regex processing (for example, `findproject a` followed by 40,000 spaces and `b`). Existing field and
keyword limits are validated separately. `AddressBookParserTest` covers the boundary, whitespace,
Unicode counting and rejection of the reported input within two seconds.

`AddCommandParser` requires exactly one each of `n/`, `p/`, `e/`, `tele/`, and `pr/`.
It constructs a `Person` with one initial project. Subsequent `project` commands append associations.
`EditCommand` can replace the name, phone, email and Telegram username, and always keeps the contact's projects.
`EditCommandParser` accepts `n/`, `p/`, `e/`, and `tele/`, requires at least one of them, and rejects `pr/`.
Individual project replacement is supported by `replaceproject`, and removal by `deleteproject`.
`renameproject` renames or merges a project across all contacts.
Unsupported prefixes are not a general extension mechanism: unrecognised text can become part of another
field's value, so callers must use the documented grammar.

| Value class | Current new-input rule and normalisation |
| --- | --- |
| `Name` | 1–60 characters after whitespace normalisation, at least one English letter; English letters, digits, spaces, apostrophes, hyphens and periods. `Name.normalize` collapses each run of spaces, tabs, line breaks or Unicode space separators (e.g. non-breaking spaces) to one space and trims the ends; other characters such as U+0085 are not treated as spaces and are rejected. Saved names keep their stored spacing. |
| `Phone` | Leading `+`, 3–15 digits with a non-zero first digit; accepted internal spaces and hyphens are removed. No country code is inferred. |
| `Email` | One `@`, non-empty dot-separated local parts, and at least two valid domain labels; no whitespace. At most 254 characters, with at most 64 before the `@` (RFC 5321 limits). Stored in lowercase. `Email.isTooLong` runs before any pattern, on both the input and saved-file paths, because the local-part pattern recurses once per dot and very long values would overflow the stack (issue #196). |
| `TelegramUsername` | 5–32 Latin letters, digits or underscores, starting with a letter and not ending with an underscore (Telegram's own rules); optional leading `@` is removed and letters are lowercased. Saved values only need the pre-v1.5 rule (5–32 of those characters), so older data files still load. |
| `Project` | 1–40 Unicode code points after normalisation; no ISO control characters. Equality ignores case. `Project.normalize` first removes invisible format characters (Unicode category Cf, e.g. zero-width space, byte order mark, direction marks), except the zero-width non-joiner and joiner (U+200C/U+200D). It then removes those joiners too when they join nothing visible: next to a Latin letter, a digit, punctuation or whitespace, or at either end of the name; a run of joiners is removed as a whole. Joiners between emoji (e.g. 👨‍💻) or between letters of scripts such as Persian or Hindi are kept. Together these stop pasted names from creating look-alike duplicates (issue #177). It then collapses whitespace and removes only spaces at either end (not `String#strip`, which would also drop U+001C–U+001F); tabs and line breaks become spaces, so `normalize` alone does not validate a name. Before any cleaning, `isValidProjectName` rejects malformed text (an unpaired surrogate, since removing a character between two unpaired surrogates could join them into a new character) and any control character, including tabs and line breaks (issue #202). Known limits: a joiner between letters of other non-Latin scripts (e.g. Cyrillic) is kept, tag characters in subdivision flag emoji are removed, and invisible characters outside category Cf (e.g. U+3164 Hangul filler) are not handled. Saved names are normalised the same way when loaded. |

`JsonAdaptedPerson` restores names, phones, emails and Telegram usernames through their stored-value validation
paths, preserving legacy spelling and phone formats. Missing Telegram usernames become `TelegramUsername.EMPTY`; missing
projects become an empty list. A project that equals an earlier one on the same contact is dropped, keeping the first
spelling, rather than rejecting the file: a file saved before invisible characters were removed (issue #177) can
list two names that are now equal. These compatibility rules do not make those fields optional in new `add` input.

### Name search and displayed indices

`FindCommandParser` trims surrounding whitespace and rejects blank input or more than 60 Unicode code points
before splitting keywords on whitespace. `NameContainsKeywordsPredicate` compares lowercase text using
`Locale.ROOT` and matches any keyword as a substring (OR semantics), including partial words.
For example, `find lex vid` can match Alex and David. Each contact appears once in saved order.

Both search commands replace the predicate on the full address book rather than searching only the previous
results. `delete`, `edit`, and `project` resolve one-based indices against the displayed list.
`add`, a successful `edit`, `list`, and successful `undo` show all contacts; `delete`, `project`, and a failed or
unchanged `edit` retain the active filter.
After a mutation, indices can change, so callers must read the current list before using another index.

### Recent search results

`MainWindow.fillInnerParts()` constructs `RecentFindPanel` using `Logic.getRecentFindPersonList()` and inserts
it into `recentFindPanelPlaceholder`. `RecentFindPanel.fxml` defines its `ListView<Person>` with variable-height cells.
Each cell displays a compact `VBox` containing a `Name | Telegram` header above project tags, without indices.
`TelegramUsername.isEmpty()` selects the `No Telegram Username` fallback; otherwise `toString()` supplies
the username with its leading `@`. The header tooltip contains the full name and displayed handle or fallback.
Commands continue to resolve indices exclusively against `Model.getFilteredPersonList()`.

`SearchContactDetails` gives the projects `FlowPane` the same `projects` ID used by `PersonCard`, reusing
the existing tag colors, padding, corners, font, and spacing in `DarkTheme.css`. The card width follows the cell
width so tags wrap within the list. Empty project lists are hidden and unmanaged.

The panel binds its single latest-search label to `Logic.recentSearchProperty()`. `LogicManager` owns the
read-only string wrapper and updates it after either search command executes; searches do not save. The stored
value uses `Name: ` or `Project: `,
with query trimming delegated to `FindCommandParser.trimSearchInput`. Other commands and parse failures
leave it unchanged. The label adds `Latest search: ` and exposes the full text in a tooltip.

`ModelManager` creates the `recentFind` filtered list once, initially with a predicate that matches no contacts.
`updateFilteredPersonList(predicate)` updates the main list and copies the predicate to `recentFind` unless it
is the `PREDICATE_SHOW_ALL_PERSONS` constant. Both `find` and `findproject` therefore replace the latest search,
while `list`, `add`, successful `edit`, and `undo` can show all contacts without replacing that search predicate.
Keeping a separate list with a stable identity allows the panel to observe updates throughout the session.

Recent results are a live filtered view rather than a snapshot. Contact additions, edits, deletions, clearing,
and undo update membership and displayed details according to the retained predicate. Invalid search input
fails before either predicate changes. Before the first search the list prompts for a search; later an empty
list displays `No contacts match the latest search.`.
The predicate is held only in memory, so recent results start empty on each launch and require no storage changes.

`MainWindowTest.recentFindResults_updateAndRemainAboveLargerPersonList()` verifies the initial empty state,
relative size and placement, replacement by a later search, no-match results, and retention after `list`.
It also checks the initial label, name and project search text, preservation after invalid input and `list`,
and the tooltip text.

`RecentFindPanelTest` checks populated and missing Telegram usernames, header tooltips, project order,
and cleanup when cells are reused or results are cleared. Its rendered styling test compares project tags
with a `PersonCard` for background, text color, padding, font, border, and spacing, and verifies wrapping.

### Most searched contact and shared latest search

`LogicManager` owns one session-only `SearchHistory`, constructed with the unfiltered observable contact list.
After either `FindCommand` or `FindProjectCommand` executes, it records the filtered results without saving the
address book. Parse failures and command failures do not increment frequencies. The shared latest search property
updates after a successful search, independently of frequency counts.

`SearchHistory` maps each full `Person` value to a frequency and last-search sequence number. Every matching
contact earns one count per search; repeated matching keywords do not multiply counts. The sequence is shared
by all contacts in a single search. Ranking compares frequency first, then the last-search sequence. Iterating
the current unfiltered contact list resolves any remaining draw in contact order, yielding exactly one contact
or `null`. Same-name contacts have separate entries. A no-match search updates recent text without affecting
any contact's frequency or recency.

The contact-list listener transfers statistics across replacements without changing their last-search sequence.
It handles both single-element edits/project updates and equal-sized snapshot replacements from undo. Contacts
already present in the replacement retain their own statistics, so list reordering cannot exchange counts.
Removed contacts are pruned. Deletion and clear therefore discard the affected counts permanently; undoing
those operations restores contacts without restoring their discarded counts. Undoing an edit or project change
preserves counts. Search statistics are not part of the contact undo snapshots, JSON data or user preferences,
and reset on restart. The latest search query survives contact mutations until the next executed name or project search.

`Logic` exposes read-only `mostSearchedPersonProperty()` and `recentSearchProperty()`. `MainWindow` groups
the two search sections in an equal-width `GridPane`, above the numbered Current contact list.
Most searched is on the left, with the subtitle Top contact this session; Recent search is on the right.
Both content areas align vertically and scroll independently. The horizontal scrollbar of the recent
results list is hidden; contact headers truncate with tooltips, while project tags wrap.
The latest query appears only once, in `RecentFindPanel`. `SearchContactDetails` renders contact headers,
Telegram fallbacks, full-text tooltips and wrapping project tags for both features. The favourite shows
`No contacts searched yet.` when no current contact has a count, and its details scroll when needed.

`SearchHistoryTest` covers frequency, search recency, same-search ordering, namesakes, edits, snapshot restore,
reordering, deletion, empty results and session reset. `LogicManagerTest` covers command integration, Unicode
query trimming, repeated keywords, failed searches, searches with unwritable storage, edits/undo and clear/undo.
`SearchSummaryUiTest` loads the actual FXML and checks distinct latest results and the session favourite,
tooltips, equal column widths, aligned content and compact layout at the 450-pixel minimum width.

### Removing retired contact fields

`Person` contains name, phone, email, Telegram username, and an ordered list of projects. Address and tags
have been removed from the model, contact cards, and JSON output. `AddCommandParser` and
`EditCommandParser` recognize the old `a/` and `t/` delimiters only to reject them with
`ParserUtil.MESSAGE_REMOVED_CONTACT_FIELDS`; this prevents old arguments from becoming another field's value.
These delimiter sequences following a space remain reserved in add/edit input.

`JsonAdaptedPerson` ignores old address/tag properties through the existing Jackson unknown-property
configuration. Missing Telegram usernames and projects retain their legacy defaults. Before saving,
`JsonAddressBookStorage.backupLegacyData` inspects the existing file for retired fields and writes its original
bytes to a unique sibling `.pre-v1.3-<unique suffix>.bak` file. Malformed JSON is not legacy data: it is backed
up as `.unreadable-<unique suffix>.bak` instead, unless `backupAddressBook()` already backed up those exact bytes
at startup (see below), which the storage remembers for the current run. One broken file therefore gets exactly one
backup, while a file broken after startup, for example by editing it while LinkUp is open, is still preserved
before the next save replaces it.
A backup I/O failure propagates before any overwrite. After a successful migration, subsequent saves find no
retired properties and create no further backup. Backup files are not part of command undo history.

A file that is well-formed but cannot be loaded, for example because two contacts conflict, may have no retired
fields and therefore may not qualify for the migration backup. When `MainApp.initModelManager` catches the
`DataLoadingException` and starts with an empty address book, it calls `Storage#backupAddressBook()`, which copies
the file to a unique sibling `.unreadable-<unique suffix>.bak` file before the first save can replace it. If the
backup fails, `initModelManager` rethrows the `IOException` and `init()` stops before creating `Logic` or the UI,
so no save can replace the only copy; `start()` then shows an error dialog asking the user to copy the file
somewhere safe and fix the problem, and exits. LinkUp starts with an empty address book only after a successful
backup. In that case `initModelManager` also records a startup message naming the backup, which `MainApp` passes
to `UiManager`; once the main window opens, `MainWindow#showStartupMessage` shows it in the result box, so the
user knows the empty list is recoverable before running a command that saves over the original file. `MainAppTest` covers both outcomes. The backup is made only at startup, not on every read, so tests
and other callers of `readAddressBook` do not create backup files.

Regression tests cover legacy loading, the save/load round trip, exact backup contents, repeated saves,
backup failure, and former-prefix rejection. Existing edit, project, search, and undo tests exercise the updated
contact structure. New `add` commands require an initial project; legacy contacts can still have an empty project list.

### Duplicate contact detection

Two contacts **conflict** if they share a phone number, an email or a Telegram username. Names and projects are
never compared, so contacts may share a name. The rule lives in one place, `Person#findConflictWith(Person)`, which
checks phone, email and Telegram username in that order and returns the first clash as a `ContactConflict`
(the clashing field, the value and the existing contact). `ContactConflict#getMessage()` builds the only
user-facing duplicate message. `Person#conflictsWith(Person)` is the boolean form.

* Newly entered emails are stored in lower case, but emails saved by older versions keep their case. `Email#equals`
  and `Email#hashCode` therefore compare the lower-cased (`Locale.ROOT`) text, so a saved `Bob@Example.com` still
  clashes with a new `bob@example.com`. `TelegramUsername` is already normalised to lower case without `@` when it
  is constructed.
* Phones are compared in their saved form. New numbers are stored as `+` followed by digits, so spaces or hyphens
  never hide a clash. A legacy local number such as `91234567` does not match `+6591234567`, because LinkUp never
  infers a country code.
  An empty Telegram username (`TelegramUsername.EMPTY`, used by legacy contacts) never matches.
* `UniquePersonList` checks every `add`, `setPerson` and `setPersons` call and throws `DuplicatePersonException`
  carrying the conflict message. `setPerson(target, editedPerson)` ignores `target`, so a contact never conflicts
  with its own old values, while a clash with any other contact is still caught.
* `Model#findConflict(candidate)` and `Model#findConflict(candidate, excluded)` expose the check. `AddCommand` uses
  the first; `EditCommand` passes the contact being edited as `excluded`. Both reject a conflict through
  `Command#rejectIfConflicting`, before the model is changed. `JsonSerializableAddressBook` rethrows a conflict found
  while loading as an `IllegalValueException` with the same message.
* The relation is symmetric but not transitive: A may share a phone with B and B an email with C, while A and C do
  not conflict. It is therefore not an equality. `Person#equals` remains the all-fields check used to find and
  remove a contact, which keeps lookup working for contacts with the same name.

**Alternatives considered:**

* *Keep `isSamePerson`, redefined as "all fields equal".* This would still let two contacts share a phone or email
  under different names, and it would duplicate `equals`. It was removed instead.
* *Lower-case every saved email when loading.* This would make a case-sensitive comparison enough, but it would
  silently rewrite emails saved by older versions. Comparing case-insensitively handles both old and new emails.
* *Warn about, rather than reject, a contact with the same name.* Names do not identify people, and a warning on
  every same-name contact would add noise without preventing real duplicates.

Tests build clashes with `ConflictTestUtil#clashesWith`. `EditCommandTest#UNIQUE_FIELD_EDITS` lists the editable
unique fields (phone, email and Telegram username); when another unique field becomes editable, adding one entry
there tests that it is rejected on a clash with a visible or hidden contact, and that the contact's own value is
not a clash when another field changes.

### Finding contacts by project

The `findproject KEYWORD` command implements UC02 using the existing filtered contact list.

1. `AddressBookParser` routes `findproject` to `FindProjectCommandParser`.
2. The parser removes surrounding whitespace, rejects a phrase that is blank once `Project.normalize()` has removed
   invisible characters (e.g. one made only of zero-width spaces), and checks the 40-character limit before
   collapsing internal whitespace. Length is measured in Unicode code points, consistent with the project model.
   The surrounding-whitespace rule also trims U+001C–U+001F, which `Project.normalize()` keeps so that `Project`
   rejects them as control characters; a search phrase is never stored, so this only widens what a search ignores.
3. `ProjectContainsKeywordPredicate` uses `Project.normalize()` and `Locale.ROOT` to normalise the search phrase
   once. It checks whether any project returned by `Person.getProjects()` contains that whole phrase, ignoring case.
4. `FindProjectCommand` passes the predicate to `Model.updateFilteredPersonList(...)` and returns the result count
   or a specific no-matches message. The UI observes this list and displays the existing contact cards and indices.

The predicate filters contacts rather than collecting a result for each matching project, so a contact appears at
most once. Replacing the predicate searches the full underlying address book, even after another search has
filtered the displayed list. `list` restores all contacts; `delete` continues to use the displayed indices.
Search does not change contacts or project associations, so `LogicManager` does not save the address book for a
search. Invalid input fails during parsing, before the filter or saved data is changed.

Both the search keyword limit and the stored project-name limit are 40 Unicode code points. The search parser
checks length before collapsing internal whitespace, whereas `Project` validates the normalised name.
The parser therefore validates the search phrase separately from `ParserUtil.parseProject()`.

**Design choices:** A separate command preserves the existing name-search behaviour. Whole-phrase substring matching
supports partial project names without treating individual words as alternatives. Reusing the model's existing
filter avoids introducing a separate project index or changes to storage. The recent-results panel observes
the same project predicate through its separate filtered list.

### Project association implementation

The `project INDEX [MORE_INDICES] pr/PROJECT` command extends UC03 to one or more contacts.
`AddressBookParser` delegates to `ProjectCommandParser`, which splits the index preamble on whitespace,
validates every token with `ParserUtil.parseIndex`, rejects repeated indices, and requires exactly one
project prefix. `ParserUtil.parseProject` reuses shared project validation and normalization.

The sequence diagram below shows a successful execution of `project 1 2 pr/Orbital`, including how the
command validates the complete batch before changing either contact.

<puml src="diagrams/ProjectCommandSequenceDiagram.puml" width="760" alt="Interactions inside the Logic and Model components for a multi-contact project command" />

`ProjectCommand` copies the displayed contact list before resolving indices. It validates all selections
and prepares updated `Person` objects before applying any changes through `Model.setPerson`.
A duplicate association or invalid index rejects the whole command, so earlier valid selections are
not changed. Duplicate errors list all conflicting contacts by displayed index and name, in input order.
Out-of-range indices take precedence over association conflicts; no updates occur in either case.

Each replacement appends the project while preserving existing project order and all contact details.
Selected contacts are captured before mutation, so changes to filtered results cannot redirect later
updates. The active predicate is retained. Contacts outside the selection remain unchanged.

The existing `LogicManager` persists the resulting address book and then records one undo snapshot for the
entire command. One `undo` restores all affected contacts; rejected commands leave undo history intact.
No new storage format is needed. FINE logs record parsing failures, rejected selections and success counts.

Tests cover single-contact compatibility, multiple indices, malformed or repeated indices, normalization,
validation before mutation, filtered-list changes, persistence, one-step undo and diagnostic output.
Manual check: run `findproject Team`, then `project 1 2 pr/Orbital`; verify both selected contacts gain
Orbital. Run `undo` once and verify both associations are removed. Try a valid index followed by an
out-of-range index and verify neither contacts nor undo history change.


### Replacing a project association

`replaceproject INDEX old/OLD_PROJECT new/NEW_PROJECT` changes one association on one contact.
`AddressBookParser` dispatches to `ReplaceProjectCommandParser`, which requires one of each prefix
and reuses `ParserUtil.parseIndex` and `ParserUtil.parseProject` for validation.

`ReplaceProjectCommand` resolves the displayed index, locates the old project with `Project.equals`,
and rejects missing associations, unchanged stored spelling or duplicate replacements before mutation.
It copies the ordered
project list, replaces the matching entry in place, and constructs a new `Person` preserving all
other fields. `Model.setPerson` retains the active predicate; a changed contact may leave the results.
This is a local association change, not a global project rename.

The command returns `isUndoable() == true`, allowing `LogicManager` to save the prior snapshot
only after successful execution. Existing JSON storage persists the replacement without schema changes.
Parser and command tests cover normalization, boundaries, invalid inputs, duplicates and filtered indices.
The integration test verifies dispatch, filter re-evaluation, JSON persistence and undo after rejected commands.

### Removing a project association

`deleteproject INDEX [MORE_INDICES] pr/PROJECT` removes one association from each selected contact,
including their last project.
`AddressBookParser` dispatches to `DeleteProjectCommandParser`, which requires exactly one `pr/` prefix
and uses `ParserUtil.parseIndex` and `ParserUtil.parseProject` for existing validation rules.

`DeleteProjectCommand` snapshots the displayed list and resolves every index before mutation.
It locates the project using `Project.equals`,
which ignores case and normalizes whitespace. Invalid indices and missing associations are rejected
before mutation. Missing-association errors list all conflicting selections; invalid indices take precedence.
The parser rejects repeated indices with a specific message. The command copies each ordered project list,
removes the matched entry and constructs
an updated `Person` preserving all contact details. `Model.setPerson` preserves the active filter,
which may remove the contact from the visible results. Other contacts are unchanged.

Single-contact success retains the stored project spelling; batch success identifies the project
and all selected contacts. FINE logs record
successful removal and rejection reasons. `isUndoable()` enables the existing snapshot mechanism;
failed commands do not replace undo history. One undo restores the entire batch.
Existing JSON storage supports empty project lists.
Parser and command tests cover validation, normalization, order, last-project removal and diagnostics.
An integration test covers filtered selection, disappearance from results, persistence and undo after errors.

Manual check: run `findproject CS2103T`, then `deleteproject 1 pr/cs2103t`. Confirm the selected
contact leaves the results. Run `list` to verify the contact remains saved, then `undo` to restore its project.

### Edit command implementation

`edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [tele/TELEGRAM]` replaces one or more details of the contact at a
one-based index in the displayed list. `EditCommandParser` validates each supplied value with the same
`ParserUtil` methods as `add` and stores it in an `EditCommand.EditPersonDescriptor`. It reports one problem at
a time, in this order: retired `a/` or `t/`, any `pr/` (with `EditCommand.MESSAGE_PROJECT_NOT_EDITABLE`), the
index, repeated prefixes (even with identical values), the supplied values (name, phone, email, Telegram
username), and finally a missing field. The parser never reads the model. A field left unset in the
descriptor is kept exactly as saved, so untouched legacy values (for example a local phone `91234567` or a
mixed-case email) are not normalised, and a legacy contact is never forced to supply a Telegram username or project.

`EditPersonDescriptor#setTelegramUsername` rejects `TelegramUsername.EMPTY` with an `IllegalArgumentException`:
a Telegram username cannot be cleared, so a parser or helper bug fails loudly instead of wiping it. `null` means
"not supplied". `EditCommand` also defines `MESSAGE_PROJECT_NOT_EDITABLE`, for the parser to report `pr/`.

`EditCommand#execute` checks, in order:

1. The index is within the displayed list, or it reports `Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX`.
2. It builds the complete replacement `Person`: supplied fields replace, omitted fields and the ordered project
   list are copied.
3. If every saved string (`Name#fullName`, `Phone#value`, `Email#value`, `TelegramUsername#value`) is unchanged,
   it throws `No changes made.` Saved strings are compared instead of `Person#equals` because `Email#equals`
   ignores case: re-entering a legacy `Alex@Example.com` as `alex@example.com` is a real change. A Telegram
   username entered as `@Alice_Tan` normalises to `alice_tan`, so it is unchanged if that is the saved value.
4. `Model#findConflict(editedPerson, personToEdit)` checks the whole address book, including contacts hidden by
   the filter, and `Command#rejectIfConflicting` reports the first clash. Unchanged values cannot clash, so the
   no-change check runs first.
5. `Model#setPerson` replaces the contact once and the filter is reset to show all contacts.

The activity diagram below shows these checks. Each rejection ends the command before the model changes.

<puml src="diagrams/EditActivityDiagram.puml" alt="Activity diagram of the checks in EditCommand#execute" />

Every rejection is a `CommandException` thrown before the model changes. Because `LogicManager` records an undo
snapshot and saves only after a command returns normally, a failed or unchanged edit leaves contacts, the filter,
the saved file and the undo history as they were, without any change to `LogicManager`. One successful edit,
whatever the number of fields, is one undo step.

**Alternatives considered:**

* *Treat an unchanged edit as a successful command.* It would save the file and push an undo snapshot that undoes
  nothing, so the next `undo` would appear to do nothing. It was rejected.
* *Apply the valid fields and report the invalid ones.* Partial edits are hard to notice and undo, so the whole
  command is rejected instead.

`EditCommandTest` covers each field, legacy values, filtered indices, conflicts with visible and hidden contacts,
no-change detection and filter handling. `EditCommandParserTest` covers the parse order above.
`EditCommandIntegrationTest` types `edit` commands, including `tele/` and `pr/`, through the real parser,
`LogicManager` and JSON storage, checking saved data, reloads, save counts, the filter and undo.

### Project renaming and merging

`RenameProjectCommandParser` uses the shared `ArgumentTokenizer` to parse `old/` and `new/` in either order,
rejecting missing, blank or repeated arguments and unexpected preamble. Values go through
`ParserUtil.parseProject`. Quotes and backslashes remain literal, as in `replaceproject`; a space followed
by a recognised prefix is always a delimiter, even inside quotation marks.

`RenameProjectCommand.execute` reads the full address book and validates a complete replacement before
changing the model. Case-insensitive `Project.equals` identifies memberships, while exact display strings
identify actual changes. The replacement preserves contact details (including legacy values), contact order
and unrelated project order. For merges, an existing destination association retains its position; the source
is removed when both occur. Destination-only contacts also receive the requested spelling. Changed contacts
are counted once, and `Locale.ROOT` keeps the displayed count independent of the system locale.

Ordinary renames, case-only corrections and merges execute immediately through the same command path.
`Model.setAddressBook` installs the replacement without changing the current filter. The live filtered list
re-evaluates the renamed contacts, so a project search may gain or lose results. `LogicManager` saves and then
records one undo snapshot through the normal command flow. The success message mentions `undo`. There is no
pending confirmation state or rename-specific branch in `LogicManager`.

Case-only changes must never be detected with `AddressBook.equals` or `Person.equals`, because their project
comparisons ignore case. The shared persistence and save-failure behaviour described below applies to rename:
a failed save restores the previous contacts, filter, search summary, and undo history.

<puml src="diagrams/RenameProjectSequenceDiagram.puml" width="850" />

Tests cover literal quotes and backslashes, Unicode length boundaries, mixed display spelling, hidden contacts,
merge overlap/order, filter re-evaluation, locale-independent counts, failed saves, restart, and one undo
restoring exact strings. `RenameProjectUiTest` verifies visible project labels after case-only renaming and undo.

### Sort command implementation

`sort` puts every saved contact in name order and saves that order. `AddressBookParser` creates a `SortCommand`
directly, since the command takes no arguments; extra text is ignored, as for `list` and `help`.

<puml src="diagrams/SortSequenceDiagram.puml" alt="Interactions inside the Logic, Model and Storage components for the `sort` command" />

The order is defined once, in `PersonNameComparator`: names are compared in lower case (`Locale.ROOT`) with
natural ordering, so each run of digits is compared by numeric value (`Alex 2` before `Alex 10`). Digit runs are
compared as text after removing leading zeros, longer runs being larger, so names with more digits than a `long`
holds cannot overflow. Names that are otherwise equal (for example, differing only in case or in leading zeros)
are ordered by their exact text, and contacts with the same name are ordered by their
saved phone value. Phone values are unique (see [Duplicate contact detection](#duplicate-contact-detection)),
so the order of any contact list is fully determined.

When there are no contacts (`No contacts saved yet.`) or they are already in order (`Contacts are already sorted
by name.`), `SortCommand#execute` succeeds with `CommandResult#withoutDataChange`. The input is valid, so the
message is shown as normal feedback rather than an error. `LogicManager` records an undo state only for undoable
commands whose result reports `isDataChanged()`, so these cases add no undo entry. Otherwise it calls `Model#sortPersons`, which reaches
`UniquePersonList#sortPersons`. That uses `FXCollections.sort`, so the UI receives a single reordering change.
The filtered list keeps its predicate, so any search results stay displayed in the new order. `sort` is
undoable: after a successful save, `LogicManager` stores the previous address book, and `undo` restores the
previous order.

**Design choices:**

* *Reorder and save the list, rather than show a sorted view (e.g. a JavaFX `SortedList`).* Indices then match
  what is displayed and saved, the order survives a restart, and undo works through the existing snapshot
  mechanism. The cost is that contacts added later appear at the end until the next `sort`.
* *Break ties by exact name, then phone, rather than keeping the existing order.* The result depends only on
  the contacts, not on their history, which keeps the behaviour predictable and easy to test.

### Undo command implementation

The `undo` command restores the address book to the state before the most recent successful command that
supports undo and has not already been undone. It supports `add`, `edit`, `delete`, `clear`, `project`,
`deleteproject`, `replaceproject`, `renameproject`, and `sort`.
Previous states are kept in a stack, so repeated `undo` commands reverse changes in reverse execution order.

When `LogicManager.execute(...)` receives a command, it checks `Command.isUndoable()`. For an undoable
command, it copies the current address book before execution. If execution changes contact data and the save
succeeds, `LogicManager` saves that copy through `Model.saveUndoState(...)`, pushing it onto `ModelManager`'s `Deque<AddressBook>`
undo history. Commands that are not undoable and commands rejected during parsing or execution will not be added
to the undo history.

When `undo` is invoked and undo history is not empty, `LogicManager` first saves `Model.getUndoState()` to storage.
If that save fails, the contact list, filter, and history remain unchanged, allowing the user to retry.
`UndoCommand` checks `Model.canUndo()` before restoring anything. If a snapshot exists, `ModelManager.undo()`
restores the most recent snapshot with `AddressBook.resetData(...)`, removes only that snapshot from the undo
history, and updates the filtered person list to show all contacts. If no snapshot exists,
the command reports `No command to undo.`

The undo history is held only in memory and is not persisted, so it is unavailable after restarting the application.
A new undoable command after an undo adds a snapshot of the current state while preserving earlier history.
There is no redo command.

### Persistence and save failures

Apart from the special undo path above, `LogicManager` saves the complete address book after an undoable command
changes contact data. It uses `CommandResult.isDataChanged()` to skip saves for successful commands that change
nothing, such as sorting an already sorted list. Read-only commands, including `find`, `findproject`, and `list`,
do not save. Parsing and validation failures do not save.
The data path is `data/addressbook.json`, relative to the launch folder; `preferences.json` records GUI
settings. Both paths are constants in `MainApp`. Shutdown saves preferences, not a separate copy of contact data.

A failed save is reported as a `CommandException`, with a separate message for access-denied errors.
`LogicManager` restores the previous address book and filter, and restores `SearchHistory` counts and its
most-searched contact after the model has been restored. It records an undo snapshot only after a successful
save. Fix storage and retry the original modifying command; `list` cannot retry a save because it is read-only.
An undo save failure leaves contacts, the filter, and undo history unchanged, so `undo` can be retried after
storage is fixed. Validation failure leaves contacts unchanged.

### Deferred features

Notes, roles, sorting by project, archiving,
and redo are not implemented. The user stories below include backlog ideas; they are not claims about
commands available in this release. No archiving design has been implemented yet.


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Release boundary:** All ten commands listed in the current command model were in the MVP release. `edit` was
excluded from MVP acceptance and has since been extended as post-MVP work. Other future user stories below are
not commitments.

**Target user profile**:

* is a student who works on multiple courses or projects and needs to manage
  classmates, teammates, and other project contacts
* needs to keep contact details such as names, phone numbers, email addresses,
  Telegram usernames, and project associations together in one place
* prefers a lightweight desktop application and is reasonably comfortable with
  keyboard-driven CLI commands
* values quick contact lookup and updates over navigating a feature-heavy
  contacts application

**Value proposition**: LinkUp helps students quickly add, organize, and retrieve
  project-related contacts and their details from one keyboard-driven address
  book, reducing the need to search across separate course or project contact
  lists.


### User stories

This table records both current capabilities and future ideas. The current release supports adding, listing,
deleting, partial name search, project association and search, same-name contacts, duplicate detection, undo,
recent search results, and a session-only most searched contact.
Editing covers name, phone, email and Telegram username. Project associations can be replaced with
`replaceproject`, removed with `deleteproject`, or renamed and merged globally with `renameproject`.
New contacts require all five add fields; optional missing Telegram/project data is supported only when
loading legacy files. Contacts can be sorted by name. Notes, roles, sorting by project, archiving,
multi-criteria search, and highlighting which project matched remain backlog items.

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
| `* *`    | student | store contacts when some optional information is unavailable           | save a person without knowing every detail                        |
| `* *`    | student | find a contact without remembering the exact capitalisation of their name | search conveniently                                             |
| `* *`    | student | see which project caused a search result to match                      | understand why a contact was returned                            |
| `* *`    | student | keep the latest search results visible after listing all contacts      | refer back to the people I just found                            |
| `* *`    | student | see which contact I have searched for most in this session             | return quickly to a frequently needed contact                    |
| `* *`    | student | sort contacts alphabetically                                           | browse the contact list more easily                              |
| `* *`    | student | rename a project                                                       | keep project information accurate when project names change      |
| `* *`    | student | view all projects I am tracking                                        | see how my contacts are organised                                |
| `*`      | student | search contacts using multiple criteria                                | narrow down ambiguous results                                    |
| `*`      | student | sort contacts by project                                               | see related contacts together                                    |
| `*`      | student | view recently added contacts                                           | quickly find people I just met                                   |
| `*`      | student | archive contacts from completed projects                               | prevent old contacts from cluttering my active contact list      |
| `*`      | student | restore archived contacts                                              | reuse their information if I work with them again                |
| `*`      | student | remove a project                                                       | prevent completed or incorrectly created projects from cluttering my records |

### Use cases

The following use cases describe the currently implemented workflows of LinkUp.
For all use cases, the **System** is `LinkUp` and the **Actor** is a student managing project contacts.
The application is running. **MSS** stands for Main Success Scenario.

#### UC01: Find a project mate by name

**MSS**

1. Student requests to search for contacts using a name keyword.
2. LinkUp displays contacts whose names contain any whitespace-separated keyword, ignoring case and allowing
   partial-word matches, together with their contact details and projects. Matching uses OR semantics and preserves
   the existing contact order without duplicate results.
3. Student uses the displayed project information and contact details to identify the intended project mate.

Use case ends.

**Extensions**

* 1a. The search input is empty or exceeds 60 Unicode code points after trimming surrounding whitespace.
  Internal whitespace counts towards the limit before splitting the input into keywords.

  * 1a1. LinkUp displays an error message without changing saved contacts or the displayed results.

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

#### UC05: Add a contact

**MSS**

1. Student requests to add a contact, supplying a name, phone number, email, Telegram username and one initial project.
2. LinkUp saves the contact and displays a success message with its details.

Use case ends.

**Extensions**

* 1a. A required detail is missing or invalid.

  * 1a1. LinkUp displays the relevant input error without changing saved contacts.

  Use case resumes at step 1.

* 1b. The phone number, email or Telegram username is already used by another contact. Emails and Telegram
  usernames are compared ignoring case; names and projects are not compared.

  * 1b1. LinkUp names the first clashing detail, in the order phone number, email, Telegram username, and the
    contact that uses it, without changing saved contacts.

  Use case resumes at step 1.

### Non-Functional Requirements

These are design targets, not a claim that performance or usability benchmarks have been completed.
CI exercises Windows, macOS and Linux with Java 25; other OS/JDK combinations are not verified by that matrix.

1.  **Environment:** The supported release setup is Windows, macOS or Linux with Java `25` installed.
2.  **Capacity:** The system shall support at least 1000 saved contacts without noticeable sluggishness during typical usage.
3.  **Performance:** The system shall return results within 2 seconds when searching using name or project for a contact list of up to 1000 contacts.
4.  **Usability:** The system shall display a clear success message or a specific error message after every command.
5.  **Interaction efficiency:** A user with above-average typing speed for regular English text should be able to complete most tasks faster using commands than using mouse interactions.
6.  **Data integrity:** If a command fails because of invalid input, an invalid index, or a duplicate, the system shall leave the contact list unchanged.
7.  **Data consistency:** The system shall validate and normalise contact and project data consistently before storing or searching it.
8.  **Persistence and reliability:** Contacts should survive restart after a successful save. An unreadable data
    file is backed up before starting empty; if backup fails, startup stops with an error dialog. Ordinary save
    failures report an error and restore the previous in-memory state, as described above.

### Glossary

* **Contact**: A saved record for a project mate, containing their name, contact details, and associated projects.
* **Project**: A module project or team that a contact belongs to, such as CS2103T or Orbital.
* **Project association**: A link between a contact and a project. A contact can be associated with more than one project.
* **Displayed contact list**: The numbered main person list, including results of a search; command indices refer to this list.
* **Recent search results**: A smaller, unnumbered view of contacts matching the latest name or project search in the session.
* **Index**: A one-based number identifying a contact in the currently displayed contact list.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Use a separate writable test folder with Java 25 and a freshly built `LinkUp.jar`. These steps modify test
contacts and files. Keep real contact data outside this folder. Record the commit, OS, Java version, commands,
expected and actual results when reporting a failure. The cases below are a repeatable starting point for
exploratory testing, not a claim that every platform has been manually tested.

### Launch, shutdown and persistence

1. From the empty test folder, run `java -jar LinkUp.jar` in a terminal. Expect sample contacts.
2. Run `list`. Expect all sample contacts and `Listed all contacts.`; no data file is written. Add a unique test
   contact using `add n/Launch Test p/+6599990000 e/launch@example.com tele/launch_test pr/Test`; this saves the
   contact list to `data/addressbook.json`.
3. Resize and move the window, then run `exit`. Relaunch from the same folder. Expect the saved contacts and
   window settings to return. Check `data/addressbook.json` and `preferences.json` in this folder.
4. Run `undo` immediately after restart. Expect `No command to undo.` because history is session-only.
5. Run `help`. Expect the help window; close it and continue entering commands in the main window.
6. Shrink the window to its minimum size. Expect the status bar to stay fully visible. Run `projects`, then drag
   the bar below the result box downwards. Expect the result box to grow and show more of the project list.

### Prepare deterministic contacts

Run these commands in order in the test instance:

```text
clear
add n/Alex Tan p/+65 9123-4567 e/ALEX@Example.COM tele/@Alex_Tan pr/Team Alpha
add n/Alex Tan p/+65 8765-4321 e/OTHER@Example.COM tele/@Other_Alex pr/Orbital
add n/David Lim p/+65 8111-2222 e/david@example.com tele/david_lim pr/Team Beta
```

Expect three contacts in that order, including two separate Alex Tan cards. Phones display without spaces or
hyphens, emails are lowercase, and Telegram usernames display a leading `@`. Projects distinguish the namesakes.

### Search summary and draws

Restart LinkUp after preparing the deterministic contacts above so the session search history is empty.
Both search sections should initially show their empty states. Run these commands in order:

| Input | Expected summary |
| --- | --- |
| `find Alex` | One most searched contact on the left, latest search results on the right; the first Alex wins the same-search draw. |
| `find David` | David wins: all three contacts have one match, but David's search is most recent. |
| `list` then `edit 1 n/Alex Lee` | David stays the winner; editing Alex must not change search recency. |
| `find Alex` | Alex Lee wins: both Alex contacts have two matches and Alex Lee is first in contact order. |
| `undo` | First Alex's name returns to Alex Tan; their count remains two, so they stay the winner. |
| `find missing` | Winner unchanged; Latest search becomes `Name: missing`. |
| `find` | Error; both summary values remain unchanged. |
| `clear` | Most searched shows `No contacts searched yet.`; Latest search remains `Name: missing`. |
| `undo` | Contacts return, but the most searched empty state remains until a new search matches a contact. |

At the minimum window width, verify that long names and queries stay on one line with ellipses and full-text
hover tooltips. Restart again before continuing the contact-editing scenarios below.

### Search, index selection and editing

Run the following in order after preparing the contacts:

| Input | Expected result |
| --- | --- |
| `find LEX vid` | Three contacts: case-insensitive partial keywords use OR matching. |
| `findproject team alpha` | Only the first Alex Tan. |
| `find vid` | David Lim, proving search starts from the full address book. |
| `edit 1 n/David Lee` | David is renamed; all three contacts are displayed again. |
| `findproject Orbital` | Only the second Alex Tan, shown at index 1. |
| `project 1 pr/Team Alpha` | Adds a second project to this Alex and retains the Orbital filter. |
| `project 1 pr/ team   ALPHA` | Duplicate project error; associations unchanged. |
| `findproject Team Alpha` | Both Alex contacts, once each. |
| `findproject Alpha Team` | No matches: project search uses a whole phrase, not OR keywords. |
| `find` | Format error; previous results remain unchanged. |
| `list` | All three contacts return in saved order. |
| `delete 0` then `delete 4` | Invalid-index errors; all contacts remain. |
| `findproject Orbital` then `delete 1` | Only the second Alex is deleted. |
| `undo` | The deleted Alex returns, in original order; all contacts are shown. |
| `FIND lee` then `List` | Only David Lee, then all three contacts: command words ignore case. |
| `findp Orbital` then `edit 1 N/Bob` | `Unknown command.`, then `Invalid command format!` with the `edit` usage: command words cannot be shortened, and prefixes stay case-sensitive. |

### Recent search results panel

After the preceding sequence, the contacts are Alex Tan, Alex Tan and David Lee. Check the following in order:

| Input or action | Expected result |
| --- | --- |
| `find Alex` | Both Alex contacts appear in both lists. Recent cards show names, Telegram handles, and project tags without indices; the search label shows `Latest search: Name: Alex`. |
| `list` | All three contacts appear in the main list; recent results still show only the two Alex contacts and retain the search label. |
| `findproject Orbital` | Recent results are replaced with the second Alex only; the label shows `Latest search: Project: Orbital`. |
| `find` | Parse error; both lists and the search label retain their previous values. |
| `delete 1` | The Orbital contact is removed from both lists; recent results show `No contacts match the latest search.`. |
| `undo` | All three contacts return to the main list; the Orbital contact returns to recent results. |
| `find Nobody` then `list` | Recent results remain empty while the main list shows all three contacts. |
| `find LEX vid` | Three recent results can be scrolled independently of the main list. |
| Resize the window | Most searched stays left of Recent search in equal-width columns above the main person list; project tags wrap as needed. |
| Hover over a recent-result header | The tooltip shows the full name and Telegram handle. |
| `exit`, then relaunch | Contacts persist; recent results start empty. |

### Editing contacts

Continue after the preceding sequence, with Alex Tan, Alex Tan and David Lee displayed. Run in order:

| Input | Expected result |
| --- | --- |
| `find David` then `edit 1 n/David Lee` | `No changes made.`; only David is still shown and nothing is saved. |
| `edit 1 n/David Lim p/+65 8111-3333 e/David.Lim@Example.com` | `Edited contact: David Lim; Phone: +6581113333; Email: david.lim@example.com; ...`; all three contacts are shown and the project is kept. |
| `edit 3 n/David Lim` | `No changes made.`; no undo step is added. |
| `undo` | Name, phone and email all return to David Lee's previous values in one step. |
| `find Lee` then `edit 1 p/+6591234567` | Phone conflict with the first Alex, although that contact is hidden; only David is still shown. |
| `edit 1 n/Dave e/` | Invalid email; the name is not changed either. |
| `list` | All three contacts, with David's details unchanged. |

Continue with Telegram usernames and projects:

| Input | Expected result |
| --- | --- |
| `edit 3 tele/@David_L n/David Lim` | Both fields change; the Telegram username is saved as `david_l`. |
| `edit 3 tele/@DAVID_L` | `No changes made.`: it normalises to the saved `david_l`. |
| `edit 3 tele/` | Telegram username format error; nothing changes, because a Telegram username cannot be cleared. |
| `edit 3 tele/@alex_tan` | Telegram conflict with the first Alex. |
| `edit 3 n/David pr/Team Beta` | `Projects cannot be edited with edit. Use project INDEX pr/PROJECT to add an association.`; nothing changes. |
| `undo` | David Lee's name and Telegram username return, restoring the fixture for the next section. |

Use `project` to append a project.

### Validation and duplicate rejection

Start with `list` after the preceding sequence. Each rejected command below must leave the contacts unchanged:

| Input | Expected result |
| --- | --- |
| `add n/New Person p/+6591234567 e/new@example.com tele/new_person pr/Test` | Phone conflict with the first Alex. |
| `add n/New Person p/+6599998888 e/ALEX@EXAMPLE.COM tele/new_person pr/Test` | Email conflict, ignoring case. |
| `add n/New Person p/+6599998888 e/new@example.com tele/@ALEX_TAN pr/Test` | Telegram conflict, ignoring case and `@`. |
| `add n/New Person p/+6599998888 e/new@example.com tele/new_person` | Required-project error. |
| `add n/New Person p/99998888 e/new@example.com tele/new_person pr/Test` | Invalid new phone: a leading `+` is required. |
| `edit 3 e/other@example.com` | Email conflict with the second Alex. |
| `edit 1 a/Old Address` | Address/tags are no longer supported. |
| `edit 1 n/Alex Tan n/Another Name` | Duplicate-prefix error. |
| `edit 1 n/Alex   Tan` | `No changes made.`: repeated spaces collapse to the saved name `Alex Tan`. |

For boundaries, try `find` with 60 and then 61 consecutive `x` characters, and `findproject` with 40 and
then 41. The shorter query is accepted (with no matches); the longer one reports a length error without
changing the current results. Run `list` to restore the displayed contacts. Then `project 1 pr/` with 40
consecutive `x` characters succeeds, while 41 fails.
Run `undo` after the accepted project change to restore the fixture.

To check the overall command limit, paste `help` followed by 996 spaces: it should open help.
With 997 spaces, it should instead report
`Command is too long (maximum 1000 characters, including spaces). Shorten it and try again.`
On Windows PowerShell, use `Set-Clipboard ('findproject a' + (' ' * 40000) + 'b')`, paste into
the command box and execute. Verify that the same command-length error appears within two seconds
and that the displayed contacts, recent search and saved data remain unchanged.

### Renaming projects

Start from the three-contact fixture in "Prepare deterministic contacts" with no subsequent changes. After
each successful rename below, use `undo` to restore that fixture before trying the next example.

| Input | Expected result |
| --- | --- |
| `find David`, then `renameproject old/Team Alpha new/Team Gamma` | The hidden Alex's project is renamed; only David remains displayed. |
| `findproject Team Alpha`, then `renameproject old/Team Alpha new/Team Gamma` | No contacts remain in the current results; the renamed Alex is still saved. |
| `findproject Orbital`, then `renameproject old/Team Alpha new/Orbital` | Both Alex contacts appear immediately; one undo restores all associations. |
| `renameproject old/orbital new/ORBITAL` | Display spelling changes immediately; undo restores `Orbital`. |
| `renameproject old/Team Alpha new/Orbital` | Memberships merge immediately; the message mentions undo. |
| `renameproject old/"Team Alpha" new/Orbital` | Source not found: quotation marks are part of the name. |
| `renameproject old/Team Alpha new/"Team Blue"` | The saved project name includes both quotation marks. |
| `renameproject old/Missing new/Orbital` | Error; nothing changes. |

After a successful rename, restart from the same test folder and verify the saved names. If a save fails,
the previous contacts, filter, search summary, and undo history remain. Fix storage and retry the rename;
`undo` would reverse an earlier successful command. Automated tests inject failures without OS-specific permissions.

### Undo and restart

1. Run `list`, then `clear`. Expect an empty list; `list` now reports `No contacts saved yet.`
2. Run `undo`. Expect the three contacts to return. `list` has not consumed or added an undo entry.
3. Run `edit 1 n/Alex Updated`, then `undo`. Expect the original name and all contacts.
4. Run `exit` and relaunch from the same folder. Expect the restored contacts and `undo` to report no history.

### Sorting contacts

Start from the prepared contacts.

1. Add a contact whose name comes first alphabetically, e.g.
   `add n/Aaron Lim p/+6581110000 e/aaron@example.com tele/aaron_lim pr/CS2103T`. Expect it at the end of the list.
2. Run `sort`. Expect `Sorted 4 contact(s) by name.` and the order Aaron Lim, Alex Tan (`+6587654321`),
   Alex Tan (`+6591234567`), David Lim: the two namesakes are ordered by phone.
3. Run `sort` again. Expect `Contacts are already sorted by name.` as a normal message (the command box clears)
   and no change.
4. Run `undo`. Expect Aaron back at the end; the repeated `sort` did not add an undo entry.
5. Run `find alex`, then `sort`. Expect the Alex results to stay displayed, now in name order.
6. Run `exit` and relaunch from the same folder. Expect the sorted order from step 5 to be kept.

### Missing, unreadable and legacy files

Close the application before each file change, and keep a valid copy outside the test folder. Restore that
valid copy before each scenario so the legacy tests start with contacts rather than the preceding empty file.

| Scenario | Procedure and expected result |
| --- | --- |
| Missing data | Move `data/addressbook.json` out of the test folder. Relaunch: sample contacts appear. Run `list` and verify that no data file was written. Add a unique contact to save the list. |
| Malformed JSON | Replace the test data file with `{`. Relaunch: an empty list appears after a sibling `.unreadable-<unique suffix>.bak` backup is created. Verify that backup contains exactly `{`. Run `list` and verify that the invalid file is untouched; add a unique contact to save a new address book without creating a second backup. |
| File broken while open | With LinkUp open, replace the data file with `{x` and save it. Run `list` and verify that it leaves the file untouched. Add a unique contact: a new `.unreadable-<unique suffix>.bak` containing exactly `{x` appears before the file is replaced. |
| Legacy fields | In a valid saved contact, add `"address": "Old Address"` and `"tags": ["old"]`. Relaunch and run `list`: the saved file remains unchanged. Add a unique contact: the saved file now omits these fields, and a sibling `.pre-v1.3-<unique suffix>.bak` preserves the original file. A later contact change creates no further migration backup. |
| Legacy missing fields | Remove `telegramUsername` and `projects` from one valid contact. Relaunch and search for that contact: the recent-result header shows `Name \| No Telegram Username` and no project tags. The main person card hides the missing Telegram row. New `add` commands still require both fields. |

Backup and permission failures depend on the OS and account. Their automated coverage is in `MainAppTest`,
`JsonAddressBookStorageTest`, and `LogicManagerTest`. If reproducing them manually, use only the test folder:
when startup cannot back up unreadable data, expect an error dialog and no main window; when an ordinary save
fails, expect an error and the contacts, filter, search summary, and undo history to return to their previous
state. Fix storage and retry the original modifying command. A failed undo save must preserve contacts, the
filter, and undo history so undo can be retried.
