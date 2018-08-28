#!/bin/bash
set -e

# This function asks the user to type 'yes'. Otherwise it exits the script.
function userConfirm {
    echo "$1 ('yes' to confirm)"
    read CONFIRM

    if [ "${CONFIRM}" != "yes" ]
    then
        echo "Aborting..."
        exit 1
    fi
}

# Gather some user data
echo "What is going to be the release version number?"
read NEW_VERSION
echo "What is going to be the new development version number?"
read DEVELOP_VERSION
userConfirm "So you want to release ${NEW_VERSION} and keep developing as ${DEVELOP_VERSION}?"

# Bump the version
mvn versions:set -DnewVersion="${NEW_VERSION}" -pl xillio-parent
userConfirm "Please check if your CHANGELOG is ok!"

# Push the release
git add -A
git commit -m "Bump version to ${NEW_VERSION} for release"
git tag ${NEW_VERSION}
git push

# Bump the version again
mvn versions:set -DnewVersion="${DEVELOP_VERSION}" -pl xillio-parent
git add -A
git commit -m "Bump version to ${DEVELOP_VERSION} for development"
git push
