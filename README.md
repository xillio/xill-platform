Xill
====

Xill makes it easy to perform content related operations like extraction,
manipulation or generation using a domain specific scripting language.

Installation
------------

Just by copying the entire folder in which you found this file to a location
of your choice you are already done installing Xill. Please refer to the
[Xillio Support Site](https://support.xillio.com/) for more information

### What versions of the Java Runtime Environment are supported?

* Xill IDE comes with a very recent Java 8 JRE, usually the latest.
* If you run the .jar directly or you run Xill CLI, then you need either JRE 8u101 or JRE 8u152 or later

### How about the command line interface?

Currently you still need to download and install Xill CLI separately

* Obtain Xill CLI from the same location where you found Xill IDE
* Install the latest Java 8 Runtime Environment from https://java.com/
* To run .xill scripts on the command line, please add the location of
  `xill.bat` (Windows) or `xill.sh` (Mac/Unix) to your `PATH` environment
  variable.

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
* execute `xill [name of robot]` on the command line

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


Building Xill
-------------
This repository contains all major components of the Xill platform. This
means the build can get quite complex for Maven beginners.
Take a look at the [build documentation](BUILD.md) to see how it works.

Contributing
------------
Xill is an open source project, this means that any contributions are
more than welcome! Check out the [contribution guidelines](CONTRIBUTING.md)
to see how you can help us improve this platform.

FAQ
---

### How do I build this project?

For more build information you should check out [BUILD.md](BUILD.md)

### The build cannot resolve `com.oracle:ojdbc7:12.1.0.1.0`

Oracle does not allow this artifact to be published on our Maven repositories.
To build this project you will have to set up your environment to pull the
jar file from their repository: [Take me to the instructions!](http://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9017)

License
-------
The Xill platform is licensed under the [Apache License, Version 2](LICENSE).


