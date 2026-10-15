---
  layout: default.md
  title: "MVP release checklist and notes"
  pageNav: 3
---

# MVP release checklist and notes

This page prepares the MVP release; it does not record a published release or completed manual acceptance.
The team should fill in the selected version, commit and verification results before publication.

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
- [ ] Download the uploaded release asset and check that it launches as `java -jar LinkUp.jar`.

The existing edit tests remain regression checks; editing is not an MVP acceptance condition.
Build and publishing commands are in the [DevOps Guide](DevOps.md#making-a-release).

## Release-note draft

LinkUp's MVP helps students manage project contacts using ten keyboard commands. It supports same-name
contacts, unique contact-detail checks, partial name search, project association and search, deletion, clearing,
session undo, and automatic JSON persistence. The help window lists all MVP commands.

**Requirements and launch:** Java 25 on Windows, macOS or Linux. Download the `LinkUp.jar` asset into a writable
folder, open a terminal there, and run `java -jar LinkUp.jar`. Use that same launch folder on subsequent runs
to load the same `data/addressbook.json` file.

**Limitations:** Editing is outside MVP scope; the partial command only changes name, phone and email.
Undo history lasts only for the current session and redo is unavailable. Clear/delete have no confirmation
prompt. Search limits are 60 Unicode code points for name input and 40 for a project phrase.

**Known deferred issues:** A preferences file containing `"guiSettings": null` can prevent startup; move the
test/affected `preferences.json` aside to reset window settings. A storage failure also prevents the command
forms of `help` and `exit` from completing; the Help menu and window close control remain available. Ordinary
save failures can leave unsaved changes visible: fix storage and run `list` to retry saving before closing.
The partial edit usage message still contains an outdated local-phone example; new phone input requires `+`.

Before publishing, add the chosen version, date and commit to the GitHub release, and record the acceptance
results above. Do not describe a platform or scenario as manually verified unless it was actually exercised.
