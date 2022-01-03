# Xill Platform - Change Log
All notable changes to this project will be documented in this file.

## [3.6.31] - 2022-01-03

### Fix

* Update version of log4j to 2.17.1

## [3.6.30] - 2021-12-16

### Fix

* Update version of following libraries:
  - log4j
  - Jackson
  - httpclient
  - poi
  - jsoup
  - mysql

## [3.6.26] - 2019-10-03

### Add
* PostgreSQL Plugin

## [3.6.22] - 2019-07-23

### Fix

* Update version of Jackson library

## [3.6.21] - 2019-05-24

### Fix

* Update version of Jackson library

## [3.6.20] - 2019-04-08

### Fix

* Web plugin will no longer run out of entities because of Windows errors.

## [3.6.19] - 2019-02-21

### Fix

* Collection.remove documentation now shows that no value is returned

## [3.6.18] - 2018-12-06

### Fix

* Fix a bug in iterators ([ticket 1981](https://support.xillio.com/a/tickets/1981))

## [3.6.17] - 2018-11-09

### Fix

* Update version of Jackson library

## [3.6.16] - 2018-11-06

### Fix

* Correct the documentation of the `Document.save` construct (ticket 1970)
* Add check for ancestors/descendants during project creation (ticket 1971)

## [3.6.15] - 2018-09-21

### Change

* Remove java version warning at startup

### Fix

* Lock version of eclipse-codegen to prevent security bugs which crash newly opened tab

## [3.6.13] - 2018-09-06

### Change

* Update native installation guide

## [3.6.12] - 2018-08-29

### Add

* Add an additional parameter to the `XML.xsdCheck` construct, to output the list of errors if the XML is invalid:
    * name: `outputAsList`
    * type: `ATOMIC` (boolean)
    * _optional_
    * default: `false`

### Fix

* Fix a bug when starting with a corrupted settings file would result in an immediate crash
* Add support for `Decimal128` type in the Mongo plugin

## [3.6.11] - 2018-08-09

### Add

* Add `Mongo.regex` construct

## Fix

* Fix a bug in `XML.xsdCheck`

## Change

* In the `Mongo.Aggregate` construct, the option `allowDiskUsage` is renamed to `allowDiskUse`.
The old option still works, but is deprecated and should be replaced eventually.

## [3.6.10] - 2018-05-14

## Fix

* Add `Mongo.find` `batchSize` option documentation [#5]
* Deadlock when compiling multiple robots at the same time [#8]

### Fix

* XML Plugins's XSD validator fails on Linux [#1933]

## [3.6.9] - 2018-04-19

### Add

* Add binary support for the Mongo plugin [#1]

## [3.6.8] - 2018-02-27

### Fix

* Fix issue where constructs were not loaded from external plugins
* Fix issue where xill projects without dependencies failed to package

## [3.6.7] - 2018-02-09

### Fix

* Fix issue where specialized classloaders did not load constructs

## [3.6.6] - 2018-02-05

### Add

* Add support for Mongo UUID types by introducing the `Mongo.uuid` construct [CTC-2306]

## [3.6.5] - 2018-02-01

### Add

* Add `ignoreConnectionCache` option to `XURL` constructs to disable the connection caching [CTC-2305]

## [3.6.4] - 2017-08-30

### Fix
* Fix incompatibility of `Concurrency` plugin with xill-platform 3.6.x [CF-132]

## [3.6.3] - 2017-08-01

### Add
* Add `locale` parameter to `Date.parse()` and `Date.format()` [CF-102].
* Add `timeout` parameter to `XURL.get()` and set default to 5 minutes [XDE-64]

### Change
* Deprecate `Date.localizedFormat()` construct [CON-14]

### Fix
* Fixed encoding issue when maven local repository path contains spaces [CF-115]
* Fix issue where robots could not be addressed by path in Xill CLI [CF-116]
* Improve error message when trying to get member of an atomic value [CF-61]
* Fix issue where username and passwords were required for proxy in XURL [CF-49]
* Fix issue where including dependencies in a package would overwrite project robots with dependency robots on Windows [CON-12]

## [3.6.2] - 2017-06-30

### Change
* Update Log4J version to 2.8.2 [XDE-78]

## [3.6.1] - 2017-06-13

### Add
* Option for fat archives built by maven [CF-31]
* Add `Date.isDate()` construct [CTC-2134]
* Add `-i`/`--ignore-errors` flag to CLI to continue after errors [CF-110]

### Change
* `File.isFolder()` no longer errors if file does not exist [CF-60]

## [3.6.0] - 2017-06-02

### Add
* Git integration. Push and pull can be done from the IDE [CTC-1919]
* Store git credentials [CTC-1975]
* Show status of changed files in git [CTC-1977]
* New `String.stream` construct [CF-21]
* Add `resourcePath` field to `System.info()` construct [CTC-1971]
* Add `qualifiedName` field to `System.info()` construct [CTC-1971]
* Add Xill Command Line Interface (CLI) and manual [CTC-2115]
* Add `xill.robotUrl` to Properties plugin [CTC-2135]

### Change
* Remove `as` keyword from `use` statement [CTC-1384]
* Remove deprecated packages `Database` and `REST` [CTC-2102]
* Remove deprecated constructs `File.getText()` and `Excel.setCell()` [CTC-2102]
* Upgrade version of Freemarker library for `Template` plugin to 2.3.25 for more functionality [CTC-2109]
* Casing of included libraries is now checked (update of Xill language) [CTC-1943]
* Only enable version control buttons when a remote repo is found [CF-16]
* Rename the `robotPath` field from `System.info()` to `robotUrl` (now always returns a URL to support resource loading) [CTC-1971]
* Rename the `rootRobotPath` field from `System.info()` to `rootRobotUrl` (now always returns a URL to support resource loading) [CTC-1971]
* Rename `robotPath` from Xill properties in the `Properties` plugin to `robotUrl` (now always contains an URL to support resource loading) [CTC-2135]

### Fix
* Fixed issues where breakpoints would not trigger [CTC-2135]
* Fixed an issue where breakpoints would reset after a robot was stopped manually [CTC-2138]
* Fixed an issue where validate calls lock the resource set [CTC-2121]

## [3.5.2] - 2017-04-17

### Add
* `String.lastIndexOf()` to find the last occurrence of a substring in a string [CTC-1991]
* Xill Command Line Interface (released separately) [CTC-2201]
* Add .ftl, .ftlh and .ftlx extensions to supported extensions [CTC-2131]

### Change
* Decrease minimum height of help pane [CTC-2133]

### Fix
* Unexpected behaviour of `continue` statement, where code in outer loop might be skipped when using `continue` in a nested loop structure [CTC-1987]
* (Asserted) error messages not displayed when calling a bot using `callbot()` [CTC-1986]
* File.getMimeType should not check if file exists [CTC-2128]
* Dialogs take up entire container on alternative window managers [CTC-2205]

## [3.5.1] - 2017-02-28

### Add
* XURL ntlm support [CTC-2004]


### Fix
* Web.loadPage does not work with file:// protocol [CTC-1935]
* Cannot divide by zero error in progress bar [CTC-1884]
* Merge 'Add Project' and 'Create Project from Sources' dialogs [CTC-2088]

## [3.5.0] - 2017-02-17

### Add
* Qualified includes for xill libraries using the new `as` keyword [CTC-1946]
* Function overloading based on parameters [CTC-1613]
* `Template` plugin [CTC-1799]
* `System.parseJSON()` now parses LIST [CTC-1769]
* Warn users about invalid asset names [CTC-1798]
* Support external logging by adding logging handler that exposes all logging events and exceptions [CTC-2031]
* Progress bar dialog and `System.setProgress()` construct for overview of robot progress [CTC-1101]
* `Collection.range()` construct to easily create oprderediterators for loops [CTC-1592]
* `Excel.recalculate()` construct to recalculate a previously saved workbook [CTC-1940]

### Change
* Make `System.print` automatically pretty print LISTs and OBJECTs [CTC-1675]
* `Math.HungarianAlgorithm()` now returns an object [CTC-1732]
* Correct typing inconsistencies for booleans: now the strings "false" and "0" evaluate to `false` [CTC-1733]
* Make StringBehavior conform to type conversion specification: a non-empty string will now evaluate to `true`, unless it contains any of the values "false", "0" or "null" [CTC-1865]
* Deprecate `File.getText()` [CTC-1860]
* Update File and Stream help files [CTC-1869]
* Improve layout for help files. Add a parameter description to all help files [CTC-1511]
* Using `XML.xPath()` with `@*` now returns an `OBJECT` that includes the attribute names [CTC-2054]
* Using `XML.xPath()` with `//@*` now returns a `LIST` of `OBJECTs` that includes the attribute names [CTC-2083]

### Fix
* Settings file not found for paths with whitespaces [CTC-1887]
* Upload project throws 'already exists' after 'invalid names' popup [CTC-1871]
* Name of included robot not shown during debugging [CTC-1885]
* Multiple changed robots dialogs and robot tab selection issue [CTC-1441]
* On server threads are not closed when tasks finishes [XSVR-139]
* Renaming of robot does not work properly on Linux [CTC-1787]
* `Web.xPath()` result in variable preview is incorrect [CTC-1802]
* Incorrect error handling in function parameter [CTC-1811]
* Excel plugin has an integer limit [CTC-1590]
* Under Linux reassigning key bindings to Ctrl + letter does not work [CTC-1758]
* Exiftool plugin does not check if process was started [CTC-1587]
* `String.regexEscape()` can result in invalid regular expressions [CTC-1807]
* Calling `Excel.setCellValue()` on a formula cell only sets cached value [CTC-1814]
* String.absoluteUrl does not allow protocols other than http(s) [CTC-1806]
* Dialogs do not render on (very) fast machines [CTC-1850]
* Concurrency pipeline changes dates and ObjectIds to strings [CTC-1833]
* Parsing String list results from xpath query floods log and is slow [CTC-1885] 
* `Excel.setCellFormula()` throws caught error: "Cannot get a numeric value from a text formula cell [CTC-1528]
* Order of package names in help file is off in Linux [CTC-1937]
* Formatting in Usage headers in help file [CTC-1709]
* `Excel.setCellFormula()` does not create formula in Excel sheet [CTC-1930]
* Pipeline functions do not support qualified includes [CTC-2001]
* Unable to use multibyte characters in Properties package [CTC-1846]
* Autocomplete shows only local results [CTC-2056]
* Debugger unreliable [CTC-1892]

## [3.4.1] - 2016-10-28

### Fix
* Concurrency pipeline changes dates and ObjectIds 

## [3.4.0] - 2016-10-07

### Add

* Send selected xill script standard printer by pressing Ctrl+Shift+P [CTC-1645][CTC-1752]
* New option `enableRedirect` on constructs of XURL plugin [CTC-1710][CTC-1753]
* Upload resources to server [CTC-1651]
* Add an overview of all active plugins in the settings about section [CTC-1667]
* Add an archetype for xill plugins [CTC-1284]
* Make non-robot files visible in the Xill IDE project pane [CTC-1652]
* Make non-robot files editable in Xill IDE [CTC-1617]
* Add `File.move()` construct [CTC-1643]
* Validate Xill robot when uploading to Xill Server [XSVR-21]
* Add `Date.isBefore()` and `Date.isAfter()` constructs [CTC-1672]
* Add `Date.fromTimestamp()` construct [CTC-1616]
* Add `Properties` plugin package [CTC-1663]
* Add "new folder" and "new file" options to project pane context menu [CTC-1739]

### Change

* Removed easter egg [CTC-1708]
* Show the full path to the document on mouse-over on tabs in Xill IDE [CTC-1669]
* MySQL no longer has to be installed manually [CTC-1676]
* Remove paging from the Xill IDE Console [CTC-1593]
* Add `py`, `xill`, `xillt` extensions to the mimetype library [CTC-1626]
* Move cookies in XURL responses to a separate field [CTC-1687]
* Split the "New project" menu option into "New project..." and "New project from existing sources..." [CTC-1691]
* `System.exec()` outputs one string including EOL charactedrs, instead of a list of lines [CTC-1466]
* Warn user when uploading .xill file with illegal name [CTC-1723]
* Add whitelisted filetypes which can be edited without warning: "xill", "txt", "properties", "html", "htm", "css", "xslt", "xml", "json", "js", "md", "cfg", "ini", "bat", "sh", "sbot" [CTC-1755]
* Remove non-functional infor (i) button above help pane [CTC-1379]

### Fix

* Corrected documentation on query construct on JDBC plugin [CTC-1690]
* Error handling on getting next element in ResultSet is only visible in console [CTC-1685]
* `Callbot` displays unspecific error: "Exception in Robot" [CTC-1646]
* Expression is already closed with nested list [CTC-1679]
* Clicking on link in settings panel crashes IDE in Linux [CTC-1704]
* `Exiftool` plugin will scan the path variable for executable [CTC-1585]
* Robot Freezes when hitting cancel on auto-save dialog [CTC-1636]
* Change timeouts to milliseconds for all regex related constructs [CTC-1629]
* `Web.download()` now creates all required folders [CTC-1532]
* Allow custom character sets in Stream.write [CTC-1628]
* Display a descriptive error when a concurrent modification occurs [CTC-1650]
* File exists dialog does not overwrite file [CTC-1647]
* Enable change of casing in robot name [CTC-1444]
* `Collection.sort()` `onKeys` parameter is now ignored for lists [CTC-1700]
* Hotkey input fields can contain illegal keywords [CTC-1696]
* Divide by zero java error for modulo [CTC-1147]
* OutOfMemoryError when trying to preview large XML-files [CTC-1267]
* Add resolution parameter to `Web.screenshot()` [CTC-1043]
* Display of `CHANGELOG.md` after new install [CTC-1713]
* Certain exceptions are logged twice [CTC-1699]
* Clicking open containing folder crashes Linux [CTC-1698]
* Alert windows do not automatically get scaled in linux, which causes text to disappear [CTC-1742]
* Error messages for errors in (transitive) includes [CTC-1514]
* Auto-save does not work for non-robot files [CTC-1754]
* HttpResponseException on upload of large project [CTC-1751]
* Expression is evaluated even after an exception happens [CTC-1721]
* String.absoluteUrl does not allow reusing protocol [CTC-1792]
* MySQL Error: The server time zone value 'W. Europe Daylight Time' is unrecognized or represents more than one time zone. [CTC-1796]
* XURL login via cookie authentication fails [CTC-1804]
* Console does not autoscroll to latest entry [CTC-1794]
