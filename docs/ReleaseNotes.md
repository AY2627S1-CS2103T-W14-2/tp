---
  layout: default.md
  title: "LinkUp v1.4 Release Notes"
  pageNav: 3
---

# LinkUp v1.4 — Alpha

LinkUp v1.4 expands contact editing and project management, making it easier to organise teammates across
projects. It also keeps useful search results visible while users browse the full contact list.

## What's new

- **Complete contact editing.** `edit` updates names, international phone numbers, emails, and Telegram usernames
  while preserving project associations. Duplicate contact details and unchanged edits are rejected.
- **Batch project association.** `project INDEX [MORE_INDICES] pr/PROJECT` associates several displayed contacts
  with one project. The whole selection is validated before any contact changes, and one `undo` reverses the batch.
- **Project replacement and removal.** `replaceproject` changes one association while preserving its position.
  `deleteproject` removes an association from one or more contacts, including a contact's last project.
- **Project renaming and merging.** `renameproject` updates a project across all saved contacts, including contacts
  hidden by a search. Renaming to an existing project merges the associations and removes duplicates.
- **Project overview.** `projects` lists every distinct project with its contact count, including contacts hidden
  by the current search.
- **Alphabetical sorting.** `sort` orders all saved contacts by name and saves the new order. The operation can be
  undone, and active search results remain filtered.
- **Search summaries.** **Recent search** retains the latest name or project results after `list`, while
  **Most searched** shows the contact matched most often in the current session.
- **Updated help and documentation.** The in-app command reference, User Guide, Developer Guide, and manual test
  instructions cover the v1.4 commands and interface.

## Interface

The v1.4 interface places **Most searched** and **Recent search** above the numbered
**Current contact list**. Search-summary cards show names, Telegram usernames, and project associations.

![LinkUp v1.4 interface](images/Ui.png)

## Getting started

**Requirements:** Java 25 on Windows, macOS, or Linux.

1. Download `LinkUp.jar` from the [v1.4 release assets](https://github.com/AY2627S1-CS2103T-W14-2/tp/releases/tag/v1.4).
2. Place it in a writable folder and open a terminal in that folder.
3. Run `java -jar LinkUp.jar`.
4. Enter `help` to view the command reference.

LinkUp stores contacts in `data/addressbook.json` and window preferences in `preferences.json`, relative to the
folder from which the application is launched.

## Release scope

This release supports `add`, `list`, `find`, `findproject`, `project`, `projects`, `edit`, `replaceproject`,
`deleteproject`, `renameproject`, `sort`, `delete`, `clear`, `undo`, `help`, and `exit`.

Project matching ignores case and normalises whitespace. Replacement and renaming preserve the requested display
capitalisation. Batch project commands validate all selected contacts before applying any changes.

Undo history, Recent search, and Most searched are available only during the current session. There is no redo.
Contact indices always refer to the numbered current contact list.

## Known issue

- A storage failure can leave an unsaved change visible and prevent `help` and `exit` from completing. Correct the
  storage problem and run `list` to retry saving before closing. The Help menu and window close control remain
  available.

## Repository follow-up

After the v1.4 JAR was published, the Gradle `run` task was configured to enable Java assertions. This development
configuration does not change the released application's features or data format.

## Documentation

- [User Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/UserGuide.html)
- [Developer Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/DeveloperGuide.html)
- [GitHub release](https://github.com/AY2627S1-CS2103T-W14-2/tp/releases/tag/v1.4)

LinkUp is based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3) by the SE-EDU initiative.
