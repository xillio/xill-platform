Building Xill
=============

Xill is built using Maven. This project contains almost the whole
platform.

Launching Xill IDE
------------------
To launch Xill IDE you run `mvn` in the xill-ide module.

In [IntelliJ] you can launch [nl.xillio.contenttools.Application] by
[creating a run configuration] with the following properties:

    Main Class: nl.xillio.contenttools.Application
    VM Options: -Dfile.encoding=utf-8
    Use classpath of module: xill-ide

Building Xill IDE
-----------------
To build Xill IDE you:

1. Check out the repository
2. Run `mvn package -P build-native` to build a native package

You can now find your native application at `xill-ide-native/target/jfx/native`.

Updating the Project Version
----------------------------
To update the project versions in the poms you run 
`mvn versions:set -DnewVersion=X.XX.XX -pl xillio-parent -P build-native` 
in the project directory.

[xill-parent/pom.xml]: xill-parent/pom.xml
[nl.xillio.contenttools.Application]: xill-ide-launcher/src/main/java/nl/xillio/contenttools/Application.java
[IntelliJ]: https://www.jetbrains.com/idea/
[creating a run configuration]: https://www.jetbrains.com/help/idea/2016.1/creating-and-editing-run-debug-configurations.html
