$ErrorActionPreference = 'Stop'

$Root = Resolve-Path (Join-Path $PSScriptRoot '..')
$Password = if ($env:OPR_DB_PASSWORD) { $env:OPR_DB_PASSWORD } else { 'kakashijirachisohaer' }
$Database = if ($env:OPR_DB_NAME) { $env:OPR_DB_NAME } else { 'onlineparkingreservation' }

Push-Location $Root
try {
	docker compose up -d db
	$Container = docker compose ps -q db
	if (-not $Container) {
		throw 'Database container was not created.'
	}

	$Deadline = (Get-Date).AddMinutes(3)
	$Ready = $false
	do {
		Start-Sleep -Seconds 3
		docker exec -e MYSQL_PWD=$Password $Container mysqladmin ping -uroot --silent 2>$null | Out-Null
		if ($LASTEXITCODE -eq 0) {
			$Ready = $true
			break
		}
	} while ((Get-Date) -lt $Deadline)

	if (-not $Ready) {
		throw 'Database container did not become ready in time.'
	}

	docker exec -e MYSQL_PWD=$Password $Container mysql -uroot -e "CREATE DATABASE IF NOT EXISTS $Database;"

	$SqlFiles = @(
		'Online Parking Reservation Database\onlineparkingreservation_adminaccount.sql',
		'Online Parking Reservation Database\onlineparkingreservation_useraccount.sql',
		'Online Parking Reservation Database\onlineparkingreservation_inventory.sql',
		'Online Parking Reservation Database\onlineparkingreservation_routines.sql',
		'Online Parking Reservation Database\onlineparkingreservation_enhancements.sql'
	)

	foreach ($SqlFile in $SqlFiles) {
		Get-Content -LiteralPath $SqlFile -Raw | docker exec -i -e MYSQL_PWD=$Password $Container mysql -uroot $Database
	}

	docker exec -e MYSQL_PWD=$Password $Container mysql -uroot -D $Database -e 'SHOW TABLES;'
} finally {
	Pop-Location
}
