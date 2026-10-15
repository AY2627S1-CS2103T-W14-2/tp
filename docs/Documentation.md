---
  layout: default.md
  title: "Documentation guide"
  pageNav: 3
---

# Documentation Guide

* We use [**MarkBind**](https://markbind.org/) to manage documentation.
* The `docs/` folder contains the source files for the documentation website.
* To learn how set it up and maintain the project website, follow the guide [[se-edu/guides] Working with Forked MarkBind sites](https://se-education.org/guides/tutorials/markbind-forked-sites.html) for project documentation.

## Local preview and build

From the repository root, use the scripts declared in `docs/package.json`:

```sh
cd docs
npm ci
npm run serve
```

Open the local URL printed by MarkBind. Stop the preview with Ctrl+C. To build without a preview server, run
`npm run build` from `docs`; generated pages go into `docs/_site`. On Windows PowerShell, use `npm.cmd` if
execution policy prevents running `npm.ps1`. Generated pages and `node_modules` are not documentation sources.

The local dependency currently specifies MarkBind `^5.1.0`, while `.github/workflows/docs.yml` requests
`^7.2.1` for deployment. These versions have not been aligned in this documentation change. A successful local
build is useful validation but does not establish identical rendering in the deployment workflow.
The workflow runs on pushes to `master`, uses `docs` as its root, and sets `/tp` as the deployed base URL.

## Where to make changes

| File or folder | Purpose |
| --- | --- |
| `UserGuide.md` | Commands, examples, limits, launch instructions and user-facing recovery steps. |
| `DeveloperGuide.md` | Architecture, implementation, current scope, backlog distinctions and manual tests. |
| `SettingUp.md`, `Testing.md`, `DevOps.md` | Developer setup, verification and release workflow. |
| `images/` | Screenshots and other image assets. `Ui.png` is also used by the root README. |
| `diagrams/` | PlantUML sources. Current developer-guide UML is retained but marked as not yet updated. |
| `_markbind/`, `site.json` | Shared layout, navigation and site configuration. |

## Keeping documentation consistent with code

Before describing a feature as available, inspect its command/parser, model validation and storage paths.
Check new-input rules separately from legacy-data loading. For example, `add` requires an initial project,
while old contacts can load with no projects; `edit` currently supports only name, phone and email.
Keep future requirements explicitly labelled as deferred rather than presenting them as current commands.

When changing commands or validation, update both guides and their examples. Use a separate test data folder
for screenshots, with no personal contact data. Update `images/Ui.png` for the main window and
`images/findAlexDavidResult.png` for the corresponding name-search example. Check captions and alt text too.

Before submitting documentation, run `npm run build`, review warnings, and inspect the changed rendered pages
for readable tables, working navigation, correct screenshots and command examples. Run repository-wide checks
as described in [DevOps](DevOps.md). UML updates are deferred for this release; preserve the notice until the
diagrams have been checked against the implementation.

## Style guidance

* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).
* Also relevant is the [_se-edu/guides **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html).


## Converting to PDF

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).
