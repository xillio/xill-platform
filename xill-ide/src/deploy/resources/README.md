Xill IDE
========

Xill makes it easy to perform content related operations like extraction,
manipulation or generation using a domain specific scripting language.

Installation
------------

Just by copying the entire folder in which you found this file to a location
of your choice you are already done installing Xill. Please refer to the
[Xillio Support Site](https://support.xillio.com/) for more information

Get started by reading https://support.xillio.com/support/solutions/folders/6000135504

### How to use the Unified Data Model

If you want to use the Unified Data Model that comes with Xill, please
install MongoDB Community Edition:
https://www.mongodb.com/download-center#community.

For the security configuration, please check out this advisory: 
https://support.xillio.com/support/solutions/articles/24000000970-mongodb-security-advisory

### Where do I find the Xill IDE configuration?

If you need to tweak any configuration or logging, please take a look at
`Xill IDE.cfg` in the app folder. Use with caution and at your own risk.

### What versions of the Java Runtime Environment are supported?

* Xill IDE comes with a very recent Java 8 JRE, usually the latest.
* If you run the .jar directly then you need either JRE 8u101 or JRE 8u152 or later

### How about the command line interface?

Currently you still need to download and install Xill CLI separately from the same location where you found Xill IDE

### How to add a plugin?

There are two ways to install a plugin. Globally in the app folder (not upgrade safe) or in your user folder (recommended).

### Installing a plugin in the user's home folder

1. Run Xill IDE at least once. This will create a folder in your user's home folder called `.xillio`.
2. Create the folder `.xillio/xill/3.0/plugins`
3. Place your plugin in that folder
4. If needed for your plugin, copy the license file that you received into the `.xillio/xill` folder
5. (Re)start Xill IDE

### Installing a plugin in the Xill IDE app folder

1. Locate your Xill IDE installation folder and open it (on a Mac: `right click on app` > `show package contents`)
2. Find the `app/` subfolder
3. Create a folder called `plugins/` inside the app folder
4. Place your plugin in that folder
5. If needed for your plugin, copy the license file that you received into your user's `.xillio/xill` folder
6. (Re)start Xill IDE

Using Xill
----------

### How do I write a Xill robot?

To edit Xill robots, open the native xill-ide application applicable to your
system:

* Windows: `xill-ide.exe`
* Mac: `xill-ide.app`
* Unix: `xill-ide`

### How do I run a Xill robot?

To run a certain `.xill` robot, either:

* open it in Xill IDE and press the play button
* execute `xill [name of robot]` on the command line using Xill CLI

### What does it do?

Xill IDE is our integrated development environment that comes bundled 
with the Xill language. Xill provides super simple content scripting.
You can use it for migrating, extracting and analysing any kind of data 
in your file storage, Enterprise Content Management, 
Web Content Management or Asset Management Systems. 

Xill IDE connects to any type of webservice, most JDBC databases and any 
web front-end out of the box and there are many connectors available to 
make connecting to your specific system nice and easy.
We provide additional plugins for power users, including a super fast 
File System Analysis plugin, a Concurrency plugin and a Deduplicator.

### How to learn Xill?

* Check out the great built in help in the lower left pane, it has links to our support site that explain the basics of the language.
* There is a Samples folder inside the zip file. To open it, put it somewhere where you have read and write access and open it in Xill as a _new project from existing sources_ (click the (+) icon in the project pane) 
* If you have a function selected in the editor, pressing F1 will give you context sensitive help with a usage example.

### Where to get support?

Commercial support is available via https://support.xillio.com/

The open source community around Xill has it's home at http://xill.io/

License
-------

The Xill platform is licensed under the [Apache License, Version 2](LICENSE).


https://www.xillio.com/ - Xillio B.V.
Making Content Work