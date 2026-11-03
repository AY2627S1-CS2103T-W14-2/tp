---
  layout: default.md
  title: "LinkUp v1.6 Release Notes"
  pageNav: 3
---

# LinkUp v1.6

LinkUp v1.6 is the final release. It improves data safety, validation and display, and finalises the documentation.

## Improvements and fixes

- **Safer saving.** If a save fails partway, the data file keeps its previous contents instead of being left empty.
- **Stricter data file checks.** A data file with extra content after the JSON data, or with a repeated key, is treated as invalid and backed up instead of being partly loaded.
- **More robust start-up.** Missing window settings fall back to the defaults, and LinkUp warns if it cannot create its log file.
- **Tighter email validation.** Each dot-separated part of an email domain can have at most 63 characters.
- **More consistent project matching.** Project names that differ only in letter case are recognised as the same project more reliably, including names with Greek letters.
- **Clearer project errors.** Quotation marks and backslashes in project names are shown correctly in project-not-found messages.
- **Better display.** Project tags no longer cover contact details or spill outside search results, and long project names in the search sections show in full in a tooltip.
- **Updated documentation.** The User Guide now lists known limitations, and the guides and diagrams have been clarified.

## Getting started

**Requirements:** Java 25 on Windows, macOS or Linux.

1. Download `LinkUp.jar` from [this release](https://github.com/AY2627S1-CS2103T-W14-2/tp/releases/tag/v1.6).
2. Place it in a writable folder.
3. Open a terminal in that folder and run:

   ```text
   java -jar LinkUp.jar
   ```

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
- [Full changelog since v1.5](https://github.com/AY2627S1-CS2103T-W14-2/tp/compare/v1.5...v1.6)

LinkUp is based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3) by the SE-EDU initiative.
