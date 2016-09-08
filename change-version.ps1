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
param (
    [Parameter(Mandatory=$true)][string]$version
 )

function replace-tag-in-file {
    param ([Parameter(Mandatory=$true)][string]$file, 
        [Parameter(Mandatory=$true)][string]$tag, 
        [string]$replacement,
        [string]$suffix)
    $searchtext = "(<$tag>)(.*?)(</$tag>.*?$suffix)"
    $replacetext = '${1}' + $replacement + '${3}'
    (Get-Content $file -Encoding utf8) -replace $searchtext, $replacetext | Out-file $file -Encoding utf8
    echo "Changed $tag in $file to $replacement"
}

replace-tag-in-file -file xill-parent\pom.xml -tag xill.version -replacement $version
mvn -f distpom.xml versions:set "-DnewVersion=$version"
mvn versions:set "-DnewVersion=$version"
cd xill-parent
mvn versions:set "-DnewVersion=$version"
cd ..
mvn -f distpom.xml versions:commit
mvn versions:commit
