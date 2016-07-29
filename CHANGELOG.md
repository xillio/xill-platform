# Xill Platform - Change Log
All notable changes to this project will be documented in this file.

## [3.4.0] - unreleased

### Add

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

* Show the full path to the document on mouse-over on tabs in Xill IDE [CTC-1669]
* MySQL no longer has to be installed manually [CTC-1676]
* Remove paging from the Xill IDE Console [CTC-1593]
* Add py, xill, xillt extensions to the mimetype library [CTC-1626]
* Move cookies in XURL responses to a separate field [CTC-1687]

### Fix

* Exiftool plugin will scan the path variable for executable [CTC-1585]
* Robot Freezes when hitting cancel on auto-save dialog [CTC-1636]
* RegexServiceImpl.getMatcher uses incorrect timeout [CTC-1629]
* Web.download now creates all required folders [CTC-1532]
* Allow custom character sets in Stream.write [CTC-1628]
* Display a descriptive error when a concurrent modification occurs [CTC-1650]
