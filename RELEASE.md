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

Script:
```bash

echo "What is going to be the release version number?"
read PATCH_VERSION
echo "What is going to be the new development version number?"
read DEVELOP_VERSION

echo "So you want to release ${PATCH_VERSION} and keep developing as ${DEVELOP_VERSION}? ('yes' to confirm)"
read CONFIRM

if [ "${CONFIRM}" != "yes" ]
then
    echo "Aborting..."
    exit 1
fi

mvn versions:set 


```

Patch Release
-------------

To perform a patch release you only have to do the Version Dance.

Minor Release
-------------

Major Release
-------------
