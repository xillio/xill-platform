Xill
====

Xill makes it easy to perform content related operations like extraction,
manipulation or generation using a domain specific scripting language.

Legacy notice
----------

Please be aware that this project is no longer being actively maintained. As such, the automated builds via Jenkins no longer work due to JavaFX issues.
Currently, the only way to build the project is to use a Windows machine with Oracle Java 8 JDK, and follow the [build instructions](BUILD.md).
This also means that any new release needs to be done completely manually.

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

Getting Xill
------------
The built packages are available on [bintray](https://bintray.com/xillio/Xill-Platform).

Building Xill
-------------
This repository contains all major components of the Xill platform. This
means the build can get quite complex for Maven beginners.
Take a look at the [build documentation](BUILD.md) to see how it works.

FAQ
---

### How do I build this project?

For more build information you should check out [BUILD.md](BUILD.md)

### How do I release this project?

For more release information you should check out [RELEASE.md](RELEASE.md)

### The build cannot resolve `com.oracle:ojdbc7:12.1.0.1.0`

Oracle does not allow this artifact to be published on our Maven repositories.
To build this project you will have to set up your environment to pull the
jar file from their repository: [Take me to the instructions!](http://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9017)

License
-------
The Xill platform is licensed under the [Apache License, Version 2](LICENSE).
