param(
    $File1,
    $File2,
    $OutFile
)

if ($null -eq $File1 -or $null -eq $File2 -or $null -eq $OutFile -or "" -eq $File1 -or "" -eq $File2 -or "" -eq $OutFile) {
    Write-Error "a param isn't defined!"
    return
}

$Ef1 = (Get-Content $File1 | ConvertFrom-Json).engines
$Ef2 = (Get-Content $File2 | ConvertFrom-Json).engines


$map = @{}

$Ef1 | ForEach-Object {
    $engine = $_

    $map[$engine.id] = $engine
}

$Ef2 | ForEach-Object {
    $engine = $_

    $map[$engine.id].time1 += $engine.time1
    $map[$engine.id].number_result1 += $engine.number_result1
    $map[$engine.id].error1 += $engine.error1
}

$engines = ($map.Values | ForEach-Object { [PSCustomObject]$_ })

([PSCustomObject]@{
    "engines"  = $engines
    "precount" = $engines[0].error1.count
}) | ConvertTo-Json -Depth 8 | Out-File $OutFile

