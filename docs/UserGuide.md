---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# AB-3 User Guide

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/+6598765432 e/johnd@example.com tele/@john_doe pr/CS2103T` : Adds a contact named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

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

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

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
* `Phone number 91234567 is already used by Alex Yeoh. Please enter a different phone number.`
* `Email alexyeoh@example.com is already used by Alex Yeoh. Please enter a different email.`
* `Telegram username @alex_yeoh is already used by Alex Yeoh. Please enter a different Telegram username.`

If several details clash, LinkUp reports the first one in the order phone number, email, Telegram username.
Correct that detail and enter the command again.
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
* `project 2 pr/Orbital` adds Orbital to the second displayed contact.
* `find Alice` followed by `project 1 pr/CS2103T` adds CS2103T to the first search result.

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* New phone inputs follow the same international format as `add`. Existing saved local numbers remain unchanged
  until edited.
* Name and email inputs follow the same rules as `add`; edited emails are stored in lowercase.
* The new phone number and email must not belong to another contact, following the same
  [duplicate-contact rule](#adding-a-person-add) as `add`, with the same error messages. You can keep a contact's
  own phone number or email, and you can give a contact the same name as another contact.
* Address (`a/`) and tags (`t/`) are no longer supported. Telegram and projects are preserved when editing other fields; use `project` to add a project association.

Examples:
*  `edit 1 p/+6591234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `+6591234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower` Edits the name of the 2nd person to be `Betsy Crower`.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
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

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Undo a command: `undo`

Undo the last supported command. The supported commands are:
* `add`
* `delete`
* `edit`
* `clear`
* `project`

Only the most recent command can be undone.

Format: `undo`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

When loading an older data file, LinkUp ignores the retired address and tag fields. Contacts without a
Telegram username or project still load. Previously saved names and emails also retain their original spelling and
case, even if they do not meet today's input rules. Previously saved local phone numbers also load unchanged.
Before the first save that removes address or tags, LinkUp creates a
backup beside the original file, named `<original filename>.pre-v1.3-<unique suffix>.bak`. The backup preserves
the original file exactly. New saves omit address and tags; later saves of the updated file do not create more
migration backups. Importing another old file creates a separate backup. If the backup cannot be written,
LinkUp reports a save error and leaves the original file untouched.

To recover the old file, close LinkUp and copy the backup over the original data file. Keep the backup if you
want to retain address/tag values: the updated app no longer displays or edits them.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

In JSON, write international phone numbers in the saved format, such as `"+6591234567"`, without spaces or
hyphens. The command input `p/+65 9123-4567` is normalized before saving; that formatted string is not accepted
directly in JSON. Older digit-only numbers such as `"91234567"` remain loadable and are preserved exactly.
LinkUp does not infer a country code, so `91234567` and `+6591234567` are different stored phone values.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. Before
starting, it copies the invalid file to a backup beside it, named `<original filename>.unreadable-<unique suffix>.bak`.
The next command you run saves the empty address book over the original file (AddressBook saves after every command).
To recover, close AddressBook, fix the problem in the backup, and copy it over the original data file. If the
backup cannot be created (for example, because the disk is full or the folder is read-only), AddressBook shows an
error and does not start, so that your data file is never overwritten. Copy the data file somewhere safe, fix the
storage or permission problem, then start AddressBook again. Still, we recommend backing up the file yourself before
editing it.<br>
A file in which two contacts share a phone number, email or Telegram username breaks the
[duplicate-contact rule](#adding-a-person-add) and is also rejected on load.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

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
