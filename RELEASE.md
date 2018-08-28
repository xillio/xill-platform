Releasing Xill
==============

There a different reasons for creating a release, some of them have their own specific flow.

Version Dance
-------------

To release a specific version we have a few steps.

1. Update the maven version number
2. Update the changelog
3. Commit the changes
4. Tag the commit with the new version number
5. Push the changes
6. Set the maven version number to the next patch release suffixed with `-SNAPSHOT`
7. Commit & push the changes

Script: [Version Dance](version-dance.sh)

Patch Release
-------------

To perform a patch release you only have to do the Version Dance.

Minor Release
-------------

Major Release
-------------
