---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

LinkUp stores window size and position in `preferences.json` in the folder from which you launch the application. It saves these preferences automatically when you close the application.

Contact data is stored separately in `data/addressbook.json`, relative to the same launch folder. These file locations are fixed; LinkUp does not use a `config.json` file or offer a command to change them.

To reset the window size and position, close LinkUp and remove `preferences.json` from the launch folder. LinkUp creates default preferences the next time it starts. This does not delete your contacts.

For contact-file recovery, see [Saving the data in the User Guide](UserGuide.md#saving-the-data). For transfer instructions, see the [FAQ](UserGuide.md#faq).
