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

replace-tag-in-file -file distpom.xml -tag version -replacement $version -suffix "<!-- Xill Platform Version -->"
replace-tag-in-file -file xill-parent\pom.xml -tag xill.version -replacement $version
mvn versions:set "-DnewVersion=$version"
cd xill-parent
mvn versions:set "-DnewVersion=$version"
cd ..