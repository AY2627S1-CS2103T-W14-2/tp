---
  layout: default.md
  title: "LinkUp v1.5 Release Notes"
  pageNav: 3
---

# LinkUp v1.5

LinkUp v1.5 improves command feedback, input validation, search consistency and usability following alpha testing.

## Improvements and fixes

- **Flexible command casing.** Command words accept uppercase and mixed case, such as `FIND`, `FindProject` and `UNDO`. Prefixes remain case-sensitive.
- **Clearer feedback.** Adding a contact reports its index. Single-contact success messages show contact details, while project feedback uses saved spelling. Rename messages escape literal quotation marks and backslashes.
- **Project management fixes.** Invalid `renameproject` input identifies the affected field. `deleteproject` can remove names containing literal `pr/` text.
- **Search and sorting fixes.** Project search matches Greek sigma case variants, and search panels refresh after case-only project changes. Numbers in contact names sort by value, so `Alex 2` precedes `Alex 10`.
- **Input validation.** Names have repeated whitespace reduced to one space. New email and Telegram input follows stricter validation rules, and commands longer than 1,000 characters are rejected.
- **Saving and recovery.** Read-only commands no longer save contact data. If saving a change fails, LinkUp restores the previous in-memory contacts, search filter and undo history. Unreadable-file recovery avoids repeating a backup of the same file contents.
- **Interface improvements.** Long contact details wrap, and the result box, search sections and contact list can be resized using their dividers. Typing a character outside the command box moves focus back to it.

## Screenshots

The updated interface places **Most searched** and **Recent search** side by side above the numbered **Current contact list**.

<img width="745" height="790" alt="Screenshot 2026-10-22 105520" src="https://github.com/user-attachments/assets/25a6ecb8-378e-471a-b8dc-0ccfe94b0cd5" />


## Getting started

**Requirements:** Java 25 on Windows, macOS or Linux.

1. Download `LinkUp.jar` from the [release assets](https://github.com/AY2627S1-CS2103T-W14-2/tp/releases/tag/v1.5).
2. Place it in a writable folder and open a terminal in that folder.
3. Run `java -jar LinkUp.jar`.
4. Enter `help` to view the command reference.

Start LinkUp from the same folder each time to use the same saved contacts. Contact data is stored in `data/addressbook.json` relative to that folder; window preferences are stored in `preferences.json`.

## Release scope

This release supports `add`, `list`, `find`, `findproject`, `project`, `projects`, `edit`, `replaceproject`, `deleteproject`, `renameproject`, `sort`, `delete`, `clear`, `undo`, `help`, and `exit`.

Use the project commands to manage associations; `edit` changes contact details only. Telegram usernames can be replaced but cannot be cleared through editing.

Undo history and search summaries are available only during the current session. There is no redo. Renaming into an existing project, deletion and clearing take effect without a confirmation prompt.

Contact indices always refer to the numbered main list. Check them again after sorting or changing the displayed contacts.

## Known issues

- After moving the application to a secondary monitor and disconnecting that monitor, the window can reopen off-screen. Close LinkUp and remove `preferences.json` to reset its position.
- Running `help` does not restore a minimized Help window. Restore that window manually.

## Documentation

- [User Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/UserGuide.html)
- [Developer Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/DeveloperGuide.html)
- [Full changelog since v1.4](https://github.com/AY2627S1-CS2103T-W14-2/tp/compare/v1.4...v1.5)

LinkUp is based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3) by the SE-EDU initiative.
