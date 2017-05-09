Xill CLI
========

Xill makes it easy to perform content related operations like extraction,
manipulation or generation using a domain specific scripting language.

Installation
------------

* Copy the contents of this folder to a location of your choice (may also be
  your install folder of Xill IDE)
* Install the latest Java 8 Runtime Environment from https://java.com/
* To run .xill scripts on the command line, please add the location of
  `xill.bat` (Windows) or the `xill` shell script (Mac/Unix) to your `PATH`
  environment variable.

### How to use the Unified Data Model

If you want to use the Unified Data Model that comes with Xill, please
install MongoDB Community Edition:
https://www.mongodb.com/download-center#community.

For the security configuration, please check out this advisory:
https://support.xillio.com/support/solutions/articles/24000000970-mongodb-security-advisory

### What versions of the Java Runtime Environment are supported?

* Xill CLI runs with practically any Java 8 JRE, we recommend using the latest.

### How to add a plugin?

Currently for Xill CLI there is only one way to install a plugin. It is
not possible for Xill CLI to pick up globally installed plugins from the Xill IDE app/plugins folder.

### Installing a plugin in the user's home folder

1. Go to your user's home folder
2. Create the folder `.xillio/xill/3.0/plugins`
3. Place your plugin in that folder
4. If needed for your plugin, copy the license file that you received into the `.xillio/xill` folder
5. Start Xill CLI

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

* execute `xill [name of robot]` on the command line using Xill CLI

### How do I access parameters passed via the command line?

The input, output and error streams are exposed to robots through the 'argument' keyword. You can use the 'Stream' plugin
to read from the input and write to the output and error stream.

For Example:

    use Stream;

    // This argument will contain the streams
    argument std;

    // Write a line to the output
    Stream.write("-------------\n", std.output);

    // Copy all input to the output
    Stream.write(std.input, std.output);

    // Write another line to the output
    Stream.write("-------------\n", std.output);


The script above can be used by piping data into the command:

    echo "Hello World!" | xill MyRobot

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