---
  layout: default.md
  title: "v1.3 MVP release checklist and notes"
  pageNav: 3
---

# v1.3 MVP release checklist and notes

This page prepares the MVP release; it does not record a published release or completed manual acceptance.
The release version is `v1.3`; the team should record the selected commit and verification results before publication.

## Week 9 delivery requirements

The [Week 9 project instructions](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/project.html)
set the v1.3 deadline at **Thursday, 15 October 2026, 23:59**. The remaining delivery steps are:

- Assign release tasks to the `v1.3` milestone and responsible team members, with their order and due dates.
- Review and merge the release PRs, then verify the final team commit.
- Publish a GitHub release tagged `v1.3`, with itemised changes compared with AB3 and screenshots or recordings
  in the release description. A documentation page alone does not replace that description.
- Attach the runnable `LinkUp.jar` asset, built from the tagged commit.
- After release, close completed issues, move deferred work out of `v1.3`, and close the milestone.

Keep editing deferred rather than expanding scope before the deadline. This preparation does not change
`build.gradle` or add a web-view dependency.

## Release scope

| Command | Included behaviour |
| --- | --- |
| `add` | Save name, international phone, email, Telegram username and one required initial project. |
| `list` | Show all contacts and clear the search filter. |
| `find` | Case-insensitive partial-name search; whitespace-separated keywords use OR matching. |
| `findproject` | Case-insensitive whole-phrase substring search across project names. |
| `project` | Append a project to the contact at a displayed index; reject an existing association. |
| `delete` | Delete the contact at a displayed index. |
| `clear` | Delete all contacts, including those hidden by a filter, without confirmation. |
| `undo` | Restore supported changes in reverse order during the current session; no redo. |
| `help` | Show the MVP command reference and a copyable user-guide URL. |
| `exit` | Close the application. |

Contacts may share names. Shared phone numbers, emails or non-empty Telegram usernames are rejected.
Contacts save automatically after successfully executed commands and reload on restart. Legacy loading and
backup behaviour are described in the [User Guide](UserGuide.md#saving-the-data).

Editing is outside MVP scope. The existing partial `edit` command changes name, phone and email and remains
undoable. Telegram editing and project replacement/removal are not implemented. Notes, roles, sorting,
archiving and redo are also not included. UML updates are deferred and marked in the Developer Guide.

## Acceptance checklist

Run the [manual scenarios](DeveloperGuide.md#appendix-instructions-for-manual-testing) in a disposable data
folder using the packaged JAR. Record actual outcomes; do not infer these checks from a coverage percentage.

- [ ] Record the release version, commit SHA, OS and `java -version` output.
- [ ] Check that release issues have owners and the `v1.3` milestone; review outstanding PRs and deferred work.
- [ ] Run `clean check coverage shadowJar`; confirm the CI matrix passes for the selected commit.
- [ ] Build the documentation and inspect the published-guide links and screenshots.
- [ ] Launch `LinkUp.jar` with Java 25 in a new folder; verify sample contacts and window preferences.
- [ ] Add two contacts with the same name and distinct contact details; reject conflicting phone/email/Telegram values.
- [ ] Verify name search, project search and `list`, including empty results and length limits.
- [ ] Add a project using a filtered index; reject a duplicate project association.
- [ ] Delete using a filtered index, then undo; verify contact details and order are restored.
- [ ] Clear a filtered list, verify all contacts are removed, then undo to restore them.
- [ ] Open help through `help`, the Help menu and F1. Resize it and scroll to `exit` and the user-guide button.
- [ ] Exit and relaunch from the same folder; verify persisted contacts and that undo history has reset.
- [ ] Exercise unreadable/legacy-data recovery using the documented test fixtures.
- [ ] Paste the release-note draft below into the GitHub release description, including both screenshots.
- [ ] Tag the verified team commit as `v1.3` and upload the `LinkUp.jar` built from that commit.
- [ ] Download the uploaded release asset and check that it launches as `java -jar LinkUp.jar`.
- [ ] Close completed issues, reschedule deferred work, and close the `v1.3` milestone after publication.

The existing edit tests remain regression checks; editing is not an MVP acceptance condition.
Build and publishing commands are in the [DevOps Guide](DevOps.md#making-a-release).

## Release-note draft

Copy the following subsections into the GitHub release description, using **LinkUp v1.3 — MVP** as the title.
The screenshot URLs use an existing team commit so they also work outside the documentation website.
Confirm that they still represent the final release build before publishing.

### What's new compared with AB3

- **Project-focused contact cards:** Telegram usernames and ordered project associations replace address and
  tag fields. New contacts require a name, international phone, email, Telegram username and initial project.
- **Same-name contacts:** Save different people with the same name. Shared phone numbers, emails and non-empty
  Telegram usernames are rejected, with a message identifying the conflicting detail and existing contact.
- **Normalised contact input:** International phone formatting is normalised; new emails and Telegram usernames
  are lowercased. Telegram input accepts a leading `@`.
- **Partial name search:** `find` matches any whitespace-separated keyword within a name, ignoring case.
- **Project association and search:** `project INDEX pr/PROJECT` appends an association; `findproject PHRASE`
  finds contacts whose project names contain the whole phrase. Duplicate associations are rejected.
- **Session undo:** Reverse additions, deletions, clearing and project associations in reverse order.
  Existing partial edits are also undoable, although editing is outside the MVP scope.
- **Legacy-data recovery:** Older contact records can load without Telegram or projects. Backups preserve
  retired fields during migration and unreadable data before starting with an empty address book.
- **Expanded in-app help:** A scrollable, resizable reference covers all ten MVP commands and provides a
  copyable user-guide URL.

The MVP also retains listing, deleting, clearing, automatic JSON saving and exiting. These are available
alongside the new features rather than being presented as new additions to AB3.

### Product screenshots

Contact cards show Telegram usernames and project memberships, including multiple projects for one contact:

![LinkUp contact cards with Telegram usernames and project memberships](https://raw.githubusercontent.com/AY2627S1-CS2103T-W14-2/tp/7e84655a998bf23b65cff8f9e9e23072c83f1c09/docs/images/Ui.png)

Name-search results retain the matching contacts' details and project information:

![LinkUp name search showing Alex Yeoh and David Li](https://raw.githubusercontent.com/AY2627S1-CS2103T-W14-2/tp/7e84655a998bf23b65cff8f9e9e23072c83f1c09/docs/images/findAlexDavidResult.png)

### Install and run

**Requirements and launch:** Java 25 on Windows, macOS or Linux. Download the `LinkUp.jar` asset into a writable
folder, open a terminal there, and run `java -jar LinkUp.jar`. Use that same launch folder on subsequent runs
to load the same `data/addressbook.json` file.

### Scope and limitations

The MVP commands are `add`, `list`, `find`, `findproject`, `project`, `delete`, `clear`, `undo`, `help`, and `exit`.
Editing is outside MVP scope; the partial command only changes name, phone and email.
Undo history lasts only for the current session and redo is unavailable. Clear/delete have no confirmation
prompt. Search limits are 60 Unicode code points for name input and 40 for a project phrase.

**Known deferred issues:** A preferences file containing `"guiSettings": null` can prevent startup; move the
test/affected `preferences.json` aside to reset window settings. A storage failure also prevents the command
forms of `help` and `exit` from completing; the Help menu and window close control remain available. Ordinary
save failures can leave unsaved changes visible: fix storage and run `list` to retry saving before closing.
The partial edit usage message still contains an outdated local-phone example; new phone input requires `+`.

See the [User Guide](https://ay2627s1-cs2103t-w14-2.github.io/tp/UserGuide.html) for command syntax and recovery steps.
