Xill
====

Xill makes it easy to perform content related operations like extraction,
manipulation or generation using a domain specific scripting language.

Building Xill
-------------
This repository contains all major components of the Xill platform. This
means the build can get quite complex for Maven beginners.
Take a look at the [build documentation](BUILD.md) to see how it works.

Contributing
------------
Xill is an open source project, this means that any contributions are
more then welcome! Check out the [contribution guidelines](CONTRIBUTING.md)
to see how you can help us make this platform better.

FAQ
---

### How do I build this project?

For more build information you should check out [BUILD.md](BUILD.md)

### The build cannot resolve com.oracle:ojdbc7:12.1.0.1.0

Oracle does not allow this artifact to be published on our Maven repositories.
To build this project you will have to set up your environment to pull the
jar file from their repository: [Take me to the instructions!](http://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9017)

License
-------
The Xill platform is licensed under the [Apache License, Version 2](LICENSE).


