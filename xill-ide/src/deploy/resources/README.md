# Welcome to Xill IDE

## What does it do?

Xill IDE is our integrated development environment that comes bundled 
with the Xill language Xill provides super simple content scripting. 
You can use it for migrating, extracting and analysing any kind of data 
in your file storage, Enterprise Content Management, 
Web Content Management or Asset Management Systems. 

Xill IDE connects to any type of webservice, most JDBC databases and any 
web front-end out of the box and there are many connectors available to 
make connecting to your specific system nice and easy.
We provide additional plugins for power users, including a super fast 
File System Analysis plugin, a Concurrency plugin and a Deduplicator.

## How to install?

Windows
Simply put the contents of this (zip) archive anywhere on your system and run 
Xill IDE.

OSX or Unix
Unpack the contents of this archive anywhere on your system and run the installer.

Get started by reading https://support.xillio.com/support/solutions/folders/6000135504

If you want to use the Unified Data Model that comes with Xill, please 
install MongoDB Community Edition: 
https://www.mongodb.com/download-center#community. 
The Windows Server version should work on any modern Windows system.

If you need to tweak any configuration or logging, please take a look at 
`Xill IDE.cfg` in the app folder. Use with caution and at your own risk.

## How to add a plugin?

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

## How to learn Xill?

* Check out the great built in help in the lower left pane, it has links to our support site that explain the basics of the language.
* There is a Samples folder inside the zip file. To open it, put it somewhere where you have read and write access and open it in Xill as a _new project from existing sources_ (click the (+) icon in the project pane) 
* If you have a function selected in the editor, pressing F1 will give you context sensitive help with a usage example.

## Where to get support?

Commercial support is available via https://support.xillio.com/

The open source community around Xill has it's home at http://xill.io/

## How is it licensed?

Xill IDE is licensed as open source software under the Apache 2 License.
See the included LICENSE file that comes with this software.

https://www.xillio.com/ - Xillio B.V.
Making Content Work