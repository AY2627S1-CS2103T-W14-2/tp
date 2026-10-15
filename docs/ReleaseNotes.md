---
  layout: default.md
  title: "LinkUp v1.3 Release Notes"
  pageNav: 3
---

# LinkUp v1.3 — MVP

LinkUp v1.3 introduces a project-focused contact manager for students working across teams and modules.
This first MVP release builds on AddressBook Level 3 (AB3), bringing Telegram contact details, project
associations, same-name support and more flexible search into a keyboard-driven desktop application.

## What's new

- **Project-focused contacts.** Contact cards display Telegram usernames and project memberships in place of
  AB3's address and tag fields. New contacts include a name, international phone number, email, Telegram
  username and one initial project. Additional projects can be associated with an existing contact.
- **Support for identical names.** Different people can share the same name. LinkUp checks phone numbers,
  emails and non-empty Telegram usernames for conflicts and identifies the conflicting detail when an entry
  is rejected.
- **Consistent contact formatting.** New international phone numbers are stored without spaces or hyphens.
  Emails and Telegram usernames are normalised to lowercase, and Telegram input accepts a leading `@`.
- **Partial name search.** `find` matches parts of names without requiring complete words. Multiple keywords
  use OR matching, and searches ignore case.
- **Project association and search.** `project INDEX pr/PROJECT` adds a project to a contact without replacing
  existing associations. `findproject PHRASE` searches for a phrase within project names, ignoring case.
  Duplicate associations are rejected.
- **Session undo.** `undo` restores additions, deletions, clearing and project associations in reverse order.
  Existing partial edits can also be undone.
- **Expanded command help.** The resizable help window provides a scrollable reference for all ten MVP
  commands and a copyable link to the User Guide.

Listing, deletion, clearing, automatic JSON saving and exiting remain available from the AB3 foundation.

## Screenshots

Contact cards bring Telegram usernames and project memberships together, including multiple projects for
one contact.

![LinkUp contact cards with Telegram usernames and project memberships](https://raw.githubusercontent.com/AY2627S1-CS2103T-W14-2/tp/7e84655a998bf23b65cff8f9e9e23072c83f1c09/docs/images/Ui.png)

Name-search results display matching contacts with their details and project information.

![LinkUp name search showing Alex Yeoh and David Li](https://raw.githubusercontent.com/AY2627S1-CS2103T-W14-2/tp/7e84655a998bf23b65cff8f9e9e23072c83f1c09/docs/images/findAlexDavidResult.png)

### Same-name contacts

Two contacts named Alex Yeoh are stored separately, with different phone numbers, emails and Telegram usernames.
They can also belong to the same project.

![Two Alex Yeoh contacts with distinct contact details](https://raw.githubusercontent.com/yileteoh/tp/60eef49b52b3740b1a746a84dcfc7ac7a3aec785/docs/images/release-same-name.png)

### Duplicate-detail detection

An add command is rejected when its phone number is already in use, even if the other contact details differ.

![Phone conflict rejected for a contact with the same name](https://raw.githubusercontent.com/yileteoh/tp/60eef49b52b3740b1a746a84dcfc7ac7a3aec785/docs/images/release-duplicate-same-name.png)

Changing the name does not bypass the duplicate-phone check.

![Phone conflict rejected for a contact with a different name](https://raw.githubusercontent.com/yileteoh/tp/60eef49b52b3740b1a746a84dcfc7ac7a3aec785/docs/images/release-duplicate-different-name.png)

### Project search

Project search returns Alex Yeoh and Bernice Yu, both associated with CS2103T. Bernice's additional Orbital
association remains visible in the results.

![Project search returns two contacts with their project associations](https://raw.githubusercontent.com/yileteoh/tp/60eef49b52b3740b1a746a84dcfc7ac7a3aec785/docs/images/release-project-search.png)

### Deletion and undo

LinkUp confirms the deletion of the contact named `hi`.

![Deletion confirmation for the contact named hi](https://raw.githubusercontent.com/yileteoh/tp/60eef49b52b3740b1a746a84dcfc7ac7a3aec785/docs/images/release-delete.png)

Running `undo` restores the contact, including their contact details and project association.

![Undo confirmation and the restored contact hi](https://raw.githubusercontent.com/yileteoh/tp/60eef49b52b3740b1a746a84dcfc7ac7a3aec785/docs/images/release-undo.png)

## Getting started

**Requirements:** Java 25 on Windows, macOS or Linux.

1. Download `LinkUp.jar` from the [release assets](https://github.com/AY2627S1-CS2103T-W14-2/tp/releases/tag/v1.3).
2. Place it in a writable folder and open a terminal in that folder.
3. Run `java -jar LinkUp.jar`.
4. Enter `help` to view the command reference.

Start LinkUp from the same folder each time to use the same saved contacts. Contact data is stored in
`data/addressbook.json` relative to that folder; window preferences are stored in `preferences.json`.

## Release scope

The MVP includes `add`, `list`, `find`, `findproject`, `project`, `delete`, `clear`, `undo`, `help`, and `exit`.

Editing is deferred from the MVP. The existing partial `edit` command remains available for names, phone
numbers and emails. Editing Telegram usernames and replacing or removing project associations are not supported.

Undo history is available only during the current session, and there is no redo. Deletion and clearing take
effect without a confirmation prompt. Name-search input is limited to 60 Unicode code points; project-search
input is limited to 40.

## Known issues

- Invalid window preferences containing `"guiSettings": null` can prevent startup. Move `preferences.json`
  aside before restarting to reset window settings.
- A storage failure can leave an unsaved change visible and prevent the `help` and `exit` commands from
  completing. Fix the storage problem and run `list` to retry saving before closing. The Help menu and window
  close control remain available.
- The partial edit command's usage message contains an outdated local-phone example. New phone input must
  start with `+` and include a country code.

## Documentation

- [User Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/UserGuide.html)
- [Developer Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/DeveloperGuide.html)

LinkUp is based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3) by the SE-EDU initiative.
