---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# LinkUp User Guide

LinkUp is a **desktop contact manager for students working across projects and teams**. Type commands to save
contact details, associate project mates with projects, and find the right person by name or project. The contact
list shows phone numbers, emails, Telegram usernames, and projects so you can distinguish people with the same name.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Install Java `25` and check that `java -version` reports version 25 in your terminal.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download `LinkUp.jar` from the assets of the [LinkUp release](https://github.com/AY2627S1-CS2103T-W14-2/tp/releases).
   Download the JAR, not the source-code ZIP or TAR archive.

1. Place the JAR in a folder where you have permission to create and update files.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar LinkUp.jar`.<br>
   Always start from this folder: LinkUp stores data relative to the terminal's current folder. On first launch,
   if no data file exists, LinkUp supplies sample contacts.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/+6598765432 e/johnd@example.com tele/@john_doe pr/CS2103T` : Adds John Doe with an initial project.

   * `find John` : Shows contacts whose names contain John.

   * `findproject cs2103` : Shows contacts associated with matching projects.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

Examples in the feature sections are independent illustrations. Use the current displayed index and contact details
that are not already saved. Repeating an `add` example produces a duplicate-contact error.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `find KEYWORD [MORE_KEYWORDS]` accepts both `find Alice Bob` and `find Alice`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Command words and prefixes are case-sensitive: use `findproject`, not `FindProject` or `findp`.
  Separate each prefix from the preceding value with a space. Supply each supported prefix only once.

* `INDEX` always refers to the current displayed list, including search results. Check the index again after
  adding, editing, deleting, searching, or undoing. Contacts with the same name still have separate indices.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Opens a window containing a command reference and the User Guide link. Use its copy button to copy the link
into your browser. You can also open help through the Help menu or `F1`.

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL tele/TELEGRAM_USERNAME pr/PROJECT`

<box type="tip" seamless>

**Tip:** Every new contact must have one initial project. Contacts saved before this requirement may still have no project;
use `project` to associate them with one later.

For a new phone number, enter `+` followed by a country calling code and a national number. The `+` must be
immediately followed by a digit, and the number must end in a digit after surrounding whitespace is trimmed.
You may include spaces or hyphens between digits for readability; LinkUp removes them and stores the number
as `+` followed by 3–15 digits. For example, `+--6591234567` and `+6591234567-` are rejected.
The first digit after `+` cannot be `0`. For example, `p/+65 9123-4567` is stored as `+6591234567`. Bare local
numbers are rejected. LinkUp checks the format only; it does not verify the country calling code or whether the
number is assigned or callable.

Names must contain 1–60 characters after surrounding spaces are removed. English letters, digits, spaces,
apostrophes, hyphens, and periods are accepted; at least one English letter is required. The name keeps its
capitalization for display.

Emails need exactly one `@` and at least two dot-separated domain labels. The local part accepts English letters,
digits, and the unquoted symbols ``!#$%&'*+/=?^_`{|}~-``; periods must separate non-empty parts.
Domain labels contain English letters and digits, with hyphens allowed only inside a label. Surrounding whitespace
is trimmed before validation. Internal whitespace, surrounding
punctuation such as a copied comma, and prefixes such as `mailto:` are rejected. Newly entered emails are stored
in lowercase.

The Telegram username is required. It must contain 5–32 Latin letters, digits, or underscores. A leading `@` is
optional, and uppercase letters are accepted. The username is stored in lowercase and displayed with a leading `@`.

The project is required. A project name must contain 1–40 printable characters. Surrounding whitespace is removed,
repeated internal whitespace is collapsed, and project names are compared case-insensitively. Supply exactly one
non-empty `pr/PROJECT`. If `pr/` is missing, the command reports `Project is required. Use pr/PROJECT.`

In `add`, a space followed by `a/` or `t/` is treated as a retired field prefix, even within a project name.
For example, `pr/Team a/b` is rejected. Other punctuation and symbols such as `*()!%$#&` are accepted.
</box>

Examples:
* `add n/John Doe p/+6598765432 e/johnd@example.com tele/@john_doe pr/CS2103T`
* `add n/Betsy Crowe e/betsycrowe@example.com tele/betsy_crowe p/+60123457878 pr/Orbital`

After a successful add, LinkUp confirms the contact and its initial project, for example:
`Added contact: John Doe [CS2103T]`

<box type="info" seamless>

**Duplicate contacts:** Each phone number, email and Telegram username can belong to only one contact.
Emails and Telegram usernames are compared ignoring case, so `Alex@Example.com` and `alex@example.com` count as
the same email. Contacts may share a name or a project. To tell contacts with the same name apart, compare their
phone numbers, emails, Telegram usernames and projects in the contact list.

If a detail is already used, LinkUp rejects the command, saves nothing, and names the clashing detail and contact:
* `Phone number +6587438807 is already used by Alex Yeoh. Please enter a different phone number.`
* `Email alexyeoh@example.com is already used by Alex Yeoh. Please enter a different email.`
* `Telegram username @alex_yeoh is already used by Alex Yeoh. Please enter a different Telegram username.`

LinkUp reports the first conflicting contact in saved order. If several details conflict with that contact,
it reports the phone number first, then email, then Telegram username. Correct the reported detail and try again;
another conflict may then be reported. Empty Telegram usernames on legacy contacts do not conflict.
</box>

Address and tags are no longer contact fields. The `add` and `edit` commands reject `a/` and `t/`; remove those arguments and try again.

### Associating an existing contact with a project: `project`

Adds one project to an existing contact, preserving their other projects and contact details.

Format: `project INDEX pr/PROJECT`

* `INDEX` is a positive integer referring to the currently displayed contact list, including search results.
* Supply exactly one `pr/PROJECT`. Project names contain 1–40 characters after surrounding whitespace
  is removed and repeated whitespace is collapsed. Control characters remaining after normalisation are rejected.
* Project comparison ignores capitalisation and normalises whitespace. For example, `Team Alpha` and
  `team   alpha` refer to the same project. Adding an existing association reports an error.
* The new project is appended after existing projects. The current search filter is preserved.
* Missing or invalid indices, missing or blank project names, and repeated `pr/` prefixes are rejected.
  These input errors leave contacts unchanged.
* Project associations are saved automatically. Unlike `add ... pr/PROJECT`, this command updates a saved contact.

Examples:
* With the original sample list displayed, `project 4 pr/Orbital` associates David Li with Orbital.
* `find David` followed by `project 1 pr/CS2103T` adds CS2103T to the first search result.

### Listing all persons: `list`

Shows a list of all persons in the address book.

Clears the current search filter. Shows `Listed all contacts.` when contacts exist, or `No contacts saved yet.`
when the list is empty.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* After a successful edit, LinkUp displays all contacts again. The previous search filter is cleared.
* New phone inputs follow the same international format as `add`. Existing saved local numbers remain unchanged
  until edited.
* Name and email inputs follow the same rules as `add`; edited emails are stored in lowercase.
* The new phone number and email must not belong to another contact, following the same
  [duplicate-contact rule](#adding-a-person-add) as `add`, with the same error messages. You can keep a contact's
  own phone number or email, and you can give a contact the same name as another contact.
* Address (`a/`) and tags (`t/`) are no longer supported. Telegram and projects are preserved when editing other fields; use `project` to add a project association.
* Only `n/`, `p/`, and `e/` are editable. Do not supply `tele/` or `pr/` to `edit`.
  Replacing or removing a project association is not supported in this release.

Examples:
*  `edit 1 p/+6591234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `+6591234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower` Edits the name of the 2nd person to be `Betsy Crower`.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Partial words match anywhere in a name; for example, `Han` matches `Hans`, and `LEX` matches `Alex`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.
* Keywords are separated by whitespace. The entire search input must contain 1–60 Unicode code points after
  trimming surrounding whitespace. Internal spaces count towards this limit, including repeated spaces.
* Every search checks all saved contacts and replaces the previous results. Each matching contact appears once,
  in the existing contact order, with its contact details and projects. Use `list` to show all contacts again.
* No matches produces an empty list and the message `0 person(s) listed!`.
* Successful searches report `N person(s) listed!`, where N is the number of matching contacts.
* Blank or overlong input produces an error without changing the current results or saved contacts.

Examples:
* `find John` returns `john` and `John Doe`
* `find ale` returns contacts such as `Alex Tan` and `Alexander Lee`.
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Finding contacts by project: `findproject`

Finds contacts associated with projects whose names contain the given phrase.

Format: `findproject KEYWORD`

* Matching is case-insensitive and partial: `cs2103` matches `CS2103T`.
* The entire keyword is one phrase. `team alpha` matches `CS2103T Team Alpha`, but does not match
  `Team Beta`, `Alpha Team`, or separate projects named `Team` and `Alpha`.
* Surrounding whitespace is ignored and repeated internal whitespace is treated as a single space when matching.
* The keyword must contain 1–40 characters after trimming surrounding whitespace. Internal spaces count towards
  this limit before they are collapsed. Each Unicode code point counts as one character.
* Any associated project can match. Each contact appears once, with its contact details and projects, in the
  existing contact order. Contacts with no projects are excluded.
* Every search checks all saved contacts and replaces the previous search results. Use `list` to show all contacts again.
* Matching contacts are displayed with the message `Found N contact(s) matching the project keyword.`
  If nothing matches, the displayed list is empty and the message is `No contacts found for the project keyword.`
* Blank or excessively long keywords produce an error and leave the current results and saved contacts unchanged.

Examples:

* `findproject cs2103` finds contacts in projects such as `CS2103T` and `CS2103T Team Alpha`.
* `findproject team alpha` finds contacts whose project names contain the phrase `team alpha`, ignoring case.
* `findproject orbital` followed by `delete 1` deletes the first contact in those search results.

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...
* Only the selected contact is removed, even if others have the same name. The current search filter is preserved.
* Deletion takes effect immediately without a confirmation prompt. Use `undo` in the same session to reverse it.

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

This removes **all contacts**, including those hidden by a search filter, without a confirmation prompt.
Use `undo` before closing the application if you need to restore them.

Format: `clear`

### Undo a command: `undo`

Undo the last supported command. The supported commands are:
* `add`
* `delete`
* `edit`
* `clear`
* `project`

Each `undo` reverses the most recent successful supported command that has not already been undone.
Enter `undo` repeatedly to reverse multiple commands in reverse execution order. Commands rejected for invalid
input or duplicate details do not affect the undo history. Commands such as `find`, `findproject`, `list`, and
`help` do not add undo entries. A successful undo displays all contacts again.

Undo history is available only during the current application session and is cleared on restart.
If there are no commands left to undo, the application reports `No command to undo.`
Redo is not supported.

Format: `undo`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

LinkUp automatically attempts to save after each successfully executed command, including searches and `list`.
Commands rejected during parsing or validation do not save. There is no separate save command.

The data file is `data/addressbook.json` relative to the folder from which you launch LinkUp. The status bar shows
the data location. Window settings are saved separately in `preferences.json` in the launch folder.

If a save error appears, the displayed contact list may already contain your change even though it was not saved.
Keep the application open, correct the storage or permission problem, then run `list` to retry saving the current
contacts. Do not repeat an `add` blindly, as the contact may already be in the list. If you want to reverse the change,
use `undo` after fixing storage. An undo that cannot save leaves the contacts and undo history unchanged and can be retried.

When loading an older data file, LinkUp ignores the retired address and tag fields. Contacts without a
Telegram username or project still load. Previously saved names and emails also retain their original spelling and
case when accepted by the legacy loading rules, even if they do not meet today's input rules.
Previously saved local phone numbers also load unchanged.
Before the first save that removes address or tags, LinkUp creates a
backup beside the original file, named `<original filename>.pre-v1.3-<unique suffix>.bak`. The backup preserves
the original file exactly. New saves omit address and tags; later saves of the updated file do not create more
migration backups. Importing another old file creates a separate backup. If the backup cannot be written,
LinkUp reports a save error and leaves the original file untouched.

To recover the old file, close LinkUp and copy the backup over the original data file. Keep the backup if you
want to retain address/tag values: the updated app no longer displays or edits them.

### Editing the data file

Advanced users can edit `data/addressbook.json` in the launch folder. Close LinkUp first and make a copy of the
file before editing. Changes must satisfy the saved-data validation and duplicate-contact rules.

In JSON, write international phone numbers in the saved format, such as `"+6591234567"`, without spaces or
hyphens. The command input `p/+65 9123-4567` is normalized before saving; that formatted string is not accepted
directly in JSON. Older digit-only numbers such as `"91234567"` remain loadable and are preserved exactly.
LinkUp does not infer a country code, so `91234567` and `+6591234567` are different stored phone values.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, LinkUp starts with an empty contact list at the next run. Before
starting, it copies the invalid file to a backup beside it, named `<original filename>.unreadable-<unique suffix>.bak`.
The next successfully executed command attempts to save the current contact list over the original file.
To recover, close LinkUp, fix the problem in the backup, and copy it over the original data file. If the
backup cannot be created (for example, because the disk is full or the folder is read-only), LinkUp shows an
error and does not start, so that your data file is never overwritten. Copy the data file somewhere safe, fix the
storage or permission problem, then start LinkUp again. Still, we recommend backing up the file yourself before
editing it.<br>
A file in which two contacts share a phone number, email or Telegram username breaks the
[duplicate-contact rule](#adding-a-person-add) and is also rejected on load.<br>
Use commands for ordinary changes; manual file editing is intended for users familiar with JSON and the validation rules.
</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Close LinkUp on both computers. Copy `data/addressbook.json` from the old launch folder into the `data`
folder in the new launch folder, backing up any existing destination file first. Launch LinkUp from that new folder.

**Q**: Why do I see sample contacts instead of my saved contacts?<br>
**A**: LinkUp supplies sample data when it cannot find a data file at the expected path. Check the status bar and
launch it from the same folder you used previously. A different terminal folder means a different data location.

**Q**: Why does a phone number from an old file fail when I type it into `edit`?<br>
**A**: Legacy digit-only phone numbers remain loadable, but newly entered numbers require `+` and a country code.
For example, enter `p/+6591234567` rather than `p/91234567`.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL tele/TELEGRAM_USERNAME pr/PROJECT ` <br> e.g., `add n/James Ho p/+6522224444 e/jamesho@example.com tele/james_ho pr/CS2103T`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**Find by project** | `findproject KEYWORD`<br> e.g., `findproject team alpha`
**List**   | `list`
**Project** | `project INDEX pr/PROJECT`<br> e.g., `project 1 pr/Orbital`
**Help**   | `help`
**Undo**   | `undo`
**Exit**   | `exit`
