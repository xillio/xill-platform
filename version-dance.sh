#!/bin/bash
#
# Copyright (C) 2014 Xillio (support@xillio.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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
git push --tags

userConfirm "Are you ready to push the development version?"

# Bump the version again
mvn versions:set -DnewVersion="${DEVELOP_VERSION}" -pl xillio-parent
git add -A
git commit -m "Bump version to ${DEVELOP_VERSION} for development"
git push
