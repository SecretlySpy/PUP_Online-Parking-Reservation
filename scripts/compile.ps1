$ErrorActionPreference = 'Stop'

$Root = Resolve-Path (Join-Path $PSScriptRoot '..')
$Build = Join-Path $Root 'build'
$Lib = Join-Path $Build 'lib'
$Classes = Join-Path $Build 'classes'

New-Item -ItemType Directory -Force -Path $Lib, $Classes | Out-Null

$Dependencies = @(
	@{
		Url = 'https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar'
		Out = Join-Path $Lib 'mysql-connector-java-8.0.19.jar'
	},
	@{
		Url = 'https://repo.maven.apache.org/maven2/com/toedter/jcalendar/1.4/jcalendar-1.4.jar'
		Out = Join-Path $Lib 'jcalendar-1.4.jar'
	}
)

foreach ($Dependency in $Dependencies) {
	if (!(Test-Path $Dependency.Out) -or ((Get-Item $Dependency.Out).Length -lt 1024)) {
		Invoke-WebRequest -Uri $Dependency.Url -OutFile $Dependency.Out -UseBasicParsing
	}
}

Remove-Item -Recurse -Force -LiteralPath $Classes -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $Classes | Out-Null

$ClassPath = @(
	(Join-Path $Lib 'jcalendar-1.4.jar'),
	(Join-Path $Lib 'mysql-connector-java-8.0.19.jar'),
	(Join-Path $Root 'src')
) -join [IO.Path]::PathSeparator

$Sources = Get-ChildItem -Recurse -Path (Join-Path $Root 'src') -Filter *.java | ForEach-Object { $_.FullName }
javac -cp $ClassPath -d $Classes $Sources

Write-Host "Compiled Java classes to $Classes"
