# v1.3 release preparation

Internal checklist and screenshot instructions. User-facing notes are in docs/ReleaseNotes.md.

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
backup behaviour are described in the [User Guide](../docs/UserGuide.md#saving-the-data).

Editing is outside MVP scope. The existing partial `edit` command changes name, phone and email and remains
undoable. Telegram editing and project replacement/removal are not implemented. Notes, roles, sorting,
archiving and redo are also not included. UML updates are deferred and marked in the Developer Guide.

## Acceptance checklist

Run the [manual scenarios](../docs/DeveloperGuide.md#appendix-instructions-for-manual-testing) in a disposable data
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
- [ ] Add the supplied feature screenshots, then paste the release notes into the GitHub release description.
- [ ] Tag the verified team commit as `v1.3` and upload the `LinkUp.jar` built from that commit.
- [ ] Download the uploaded release asset and check that it launches as `java -jar LinkUp.jar`.
- [ ] Close completed issues, reschedule deferred work, and close the `v1.3` milestone after publication.

The existing edit tests remain regression checks; editing is not an MVP acceptance condition.
Build and publishing commands are in the [DevOps Guide](../docs/DevOps.md#making-a-release).

## Screenshot capture instructions (not part of the published notes)

Six supplied screenshots are included in the formal release notes: same-name contacts, duplicate-phone
rejection with matching and different names, project search, deletion and undo. Their captions describe the
actual Alex Yeoh/Bernice Yu/`hi` examples shown, rather than the suggested fixture below.
Only the help-window top and bottom captures remain pending. The fixture below is retained for optional retakes.
Image links use the immutable asset commit in the personal repository so they work in a GitHub release description.

Use a separate folder with a copy of `LinkUp.jar` so these steps do not change real contacts. Launch it from
that folder with `java -jar LinkUp.jar`. Keep the command result and relevant contact cards visible in each
capture. Save the requested files under `docs/images/`; add the images and captions to docs/ReleaseNotes.md once
the pictures are supplied. Do not paste this preparation file into the GitHub release description.

Prepare two contacts in this disposable instance:

```text
clear
add n/Alex Tan p/+6591234567 e/alex@example.com tele/alex_tan pr/Team Alpha
add n/Alex Tan p/+6587654321 e/other@example.com tele/other_alex pr/Orbital
```

Then capture these views in order:

| Filename | Action and what to show |
| --- | --- |
| `release-same-name.png` | Run `find Alex`. Show both Alex Tan cards, distinct phone/email/Telegram values and their different projects. |
| `release-duplicate.png` | Run `add n/New Person p/+6591234567 e/new@example.com tele/new_person pr/Test`. Show the phone-conflict message and the unchanged two contacts. |
| `release-project-search.png` | Run `project 2 pr/Team Alpha`, then `findproject Team Alpha`. Show both matching Alex cards; the second should display both Orbital and Team Alpha. |
| `release-delete.png` | Run `delete 2`. Show the deletion message and only the first Alex remaining. |
| `release-undo.png` | Run `undo`. Show the undo-success message and both Alex cards restored with their projects. |
| `release-help-top.png` | Run `help`. Capture the top of the help window, including the title and add/search commands. |
| `release-help-bottom.png` | Scroll down in the same help window. Show clear, undo, help, exit, the editing-scope note and guide button; enlarge the window if needed. |

The two existing screenshots in the release notes can stay. These additional pictures illustrate specific behaviours rather
than repeating the main-window overview. No edit screenshot is needed for the MVP.
