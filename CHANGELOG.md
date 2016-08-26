# Xill Platform - Change Log
All notable changes to this project will be documented in this file.

## [3.4.0] - unreleased

### Add

* Add print xill script to IDE [CTC-1645]
* New option disableRedirect on constructs of XURL plugin [CTC-1710]
* Upload resources to server [CTC-1651]
* Add an overview of all active plugins in the settings about section [CTC-1667]
* Add an archetype for xill plugins [CTC-1284]
* Make non-robot files visible in the Xill IDE project pane [CTC-1652]
* Make non-robot files editable in Xill IDE [CTC-1617]
* File.move construct [CTC-1643]
* Validate Xill robot when uploading to Xill Server [XSVR-21]
* Add Date.isBefore and Date.isAfter constructs [CTC-1672]
* Add Date.fromTimestamp construct [CTC-1616]

### Change

* Removed easter egg [CTC-1708]
* Show the full path to the document on mouse-over on tabs in Xill IDE [CTC-1669]
* MySQL no longer has to be installed manually [CTC-1676]
* Remove paging from the Xill IDE Console [CTC-1593]
* Add py, xill, xillt extensions to the mimetype library [CTC-1626]
* Move cookies in XURL responses to a separate field [CTC-1687]
* Split the "New project" menu option into "New project..." and "New project from existing sources..." [CTC-1691]

### Fix

* Corrected documentation on query construct on JDBC plugin [CTC-1690]
* Error handling on getting next element in ResultSet is only visible in console [CTC-1685]
* Callbot displays unspecific error: "Exception in Robot" [CTC-1646]
* Expression is already closed with nested list [CTC-1679]
* Clicking on link in settings panel crashes IDE in Linux [CTC-1704]
* Exiftool plugin will scan the path variable for executable [CTC-1585]
* Robot Freezes when hitting cancel on auto-save dialog [CTC-1636]
* RegexServiceImpl.getMatcher uses incorrect timeout [CTC-1629]
* Web.download now creates all required folders [CTC-1532]
* Allow custom character sets in Stream.write [CTC-1628]
* Display a descriptive error when a concurrent modification occurs [CTC-1650]
* File exists dialog does not overwrite file [CTC-1647]
* Enable change of casing in robot name [CTC-1444]
* Collection.sort onKeys parameter is ignored for lists [CTC-1700]
* Hotkey input fields can contain illegal keywords [CTC-1696]
* Divide by zero java error for modulo [CTC-1147]
* OutOfMemoryError when trying to preview large XML-files [CTC-1267]
* Add resolution parameter to `Web.screenshot()` [CTC-1043]
* Display of CHANGELOG.md after new install [CTC-1713]
* Certain exceptions are logged twice [CTC-1699]
* Clicking open containing folder crashes Linux [CTC-1698]