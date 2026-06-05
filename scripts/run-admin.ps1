$ErrorActionPreference = 'Stop'

$Root = Resolve-Path (Join-Path $PSScriptRoot '..')
& (Join-Path $PSScriptRoot 'compile.ps1')

$ClassPath = @(
	(Join-Path $Root 'build/classes'),
	(Join-Path $Root 'build/lib/jcalendar-1.4.jar'),
	(Join-Path $Root 'build/lib/mysql-connector-java-8.0.19.jar'),
	(Join-Path $Root 'src')
) -join [IO.Path]::PathSeparator

$Arguments = "-cp `"$ClassPath`" adminmanagement.login"
Start-Process -FilePath 'java.exe' -ArgumentList $Arguments -WorkingDirectory $Root
Write-Host 'Started the admin login window.'
