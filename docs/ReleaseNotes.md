---
  layout: default.md
  title: "LinkUp v1.5 Release Notes"
  pageNav: 3
---

# LinkUp v1.5

LinkUp v1.5 improves reliability, command feedback, search consistency and usability following alpha testing.

## Improvements and fixes

- **Flexible command casing.** Command words accept uppercase and mixed case, such as `FIND`, `FindProject` and `UNDO`. Prefixes remain case-sensitive.
- **Clearer feedback.** Single-contact success messages show contact details, and adding a contact reports its index. Project feedback uses saved spelling and correctly escapes quotation marks in rename messages.
- **More reliable project management.** Improved `renameproject` validation identifies the affected field. Projects containing literal `pr/` text can be removed correctly.
- **Search and sorting fixes.** Project searches handle Greek sigma case variants correctly. Search panels refresh after case-only project changes. Numbers in contact names sort by value, so `Alex 2` precedes `Alex 10`.
- **Improved input handling.** Repeated whitespace in contact names is normalised, email and Telegram validation is tightened, and commands exceeding 1,000 characters are rejected.
- **Safer saving and recovery.** Failed saves restore the previous application state. Read-only commands no longer require saving contact data, and broken-file recovery avoids repeated backups.
- **Better interface behaviour.** Long contact details wrap, interface sections can be resized, and typing returns focus to the command box.
- **Updated branding and documentation.** LinkUp wording, icons, command explanations and developer diagrams have been refreshed.

## Getting started

**Requirements:** Java 25 on Windows, macOS or Linux.

1. Download `LinkUp.jar` from [this release](https://github.com/AY2627S1-CS2103T-W14-2/tp/releases/tag/v1.5).
2. Place it in a writable folder.
3. Open a terminal in that folder and run:

   java -jar LinkUp.jar

4. Enter `help` to view the command reference.

Start LinkUp from the same folder each time to access the same saved contacts. Contact data is stored in `data/addressbook.json` relative to that folder; window preferences are stored in `preferences.json`.

## Release scope

This release supports `add`, `list`, `find`, `findproject`, `project`, `projects`, `edit`, `replaceproject`, `deleteproject`, `renameproject`, `sort`, `delete`, `clear`, `undo`, `help`, and `exit`.

Use the project commands to manage associations; `edit` changes contact details only.

Undo history and search summaries are available only during the current session. There is no redo. Renaming into an existing project, deletion and clearing take effect without a confirmation prompt.

Contact indices refer to the numbered main list. Check them again after sorting or changing the displayed contacts.

## Documentation

- [User Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/UserGuide.html)
- [Developer Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/DeveloperGuide.html)
- [Full changelog since v1.4](https://github.com/AY2627S1-CS2103T-W14-2/tp/compare/v1.4...1b8653406afc9b049dd9e589227b352b366fc3eb)

LinkUp is based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3) by the SE-EDU initiative.
