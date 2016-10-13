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

sed -i "" "s/<xill.version>.*<\/xill.version>/<xill.version>$1<\/xill.version>/" xill-parent/pom.xml
if [ "$2" != "-jenkins" ]
	then
		mvn versions:set -DnewVersion=$1
		mvn versions:commit
		pushd xill-parent/ >> /dev/null
		mvn versions:set -DnewVersion=$1
		mvn versions:commit
		popd >> /dev/null
fi
