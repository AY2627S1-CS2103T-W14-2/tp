---
  layout: default.md
  title: "LinkUp v1.4 Release Notes"
  pageNav: 3
---

# LinkUp v1.4

LinkUp v1.4 expands contact editing and project management, making it easier to organise teammates across projects. This release adds batch project updates, project replacement and renaming, alphabetical sorting, and search summaries that keep useful contacts visible while you browse.

## What's new

- **Contact editing with Telegram support.** `edit` now supports names, international phone numbers, emails and Telegram usernames. Unspecified fields and project associations are preserved. Duplicate contact details are rejected, and edits that leave all stored values unchanged report `No changes made.`.
- **Project associations for multiple contacts.** `project INDEX [MORE_INDICES] pr/PROJECT` adds one project to several contacts at once. All selections are validated before changes are applied. Errors identify repeated indices and every selected contact that already has the project. One `undo` reverses the entire batch.
- **Project removal.** `deleteproject INDEX [MORE_INDICES] pr/PROJECT` removes a project association from one or more contacts without deleting the contacts. Removing a contact's last project is allowed. Invalid selections or missing associations reject the whole batch.
- **Individual project replacement.** `replaceproject INDEX old/OLD_PROJECT new/NEW_PROJECT` replaces one contact's project association while preserving their other details and project order. Capitalisation corrections are supported.
- **Project renaming and merging.** `renameproject old/OLD_NAME new/NEW_NAME` updates a project across all saved contacts, including those hidden by a search. Renaming to an existing project merges the associations immediately and removes duplicates. One `undo` reverses the entire rename or merge.
- **Project overview.** `projects` lists every saved project with its contact count, sorted by name. Case variants count as one project, and contacts hidden by the current search are included.
- **Alphabetical contact sorting.** `sort` orders all saved contacts by name, ignoring case, and saves the new order. Active search results remain filtered and appear in the new order. Sorting can be undone.
- **Recent search results.** A scrollable **Recent search** section keeps the latest name or project search visible after `list`. Results show names, Telegram usernames and project tags, and stay in sync with contact changes.
- **Most searched contact.** A **Most searched** section displays the top contact for the current session. Each successful name or project search counts once for every matching contact. Ties favour the more recently matched contact, then contact-list order.
- **Updated help and documentation.** The help window and guides cover the new commands, batch operations, search summaries and supported editing fields. The edit usage example now uses an international phone number.

## Screenshots

The updated interface places **Most searched** and **Recent search** side by side above the numbered **Current contact list**.

<img width="745" height="790" alt="Screenshot 2026-10-22 105520" src="https://github.com/user-attachments/assets/25a6ecb8-378e-471a-b8dc-0ccfe94b0cd5" />


## Getting started

**Requirements:** Java 25 on Windows, macOS or Linux.

1. Download `LinkUp.jar` from the [release assets](https://github.com/AY2627S1-CS2103T-W14-2/tp/releases/tag/v1.4).
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

- A storage failure can leave an unsaved change visible and prevent the `help` and `exit` commands from completing. Fix the storage problem and run `list` to retry saving before closing. The Help menu and window close control remain available.

## Documentation

- [User Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/UserGuide.html)
- [Developer Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/DeveloperGuide.html)
- [Full changelog since v1.3](https://github.com/AY2627S1-CS2103T-W14-2/tp/compare/v1.3...db26c0368a8dd96e8835bd20fcc76bfe14369340)

LinkUp is based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3) by the SE-EDU initiative.
