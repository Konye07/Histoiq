$ErrorActionPreference = 'Stop'
try {
    $stdin = [Console]::In.ReadToEnd()
    $payload = $stdin | ConvertFrom-Json

    $path = $null
    if ($payload.tool_input -and $payload.tool_input.file_path) { $path = $payload.tool_input.file_path }
    if (-not $path -and $payload.tool_response -and $payload.tool_response.filePath) { $path = $payload.tool_response.filePath }
    if (-not $path) { exit 0 }

    if ($path -notmatch '\.json$') { exit 0 }
    if ($path -notmatch 'app[\\/]src[\\/]main[\\/]assets[\\/]') { exit 0 }
    if (-not (Test-Path -LiteralPath $path)) { exit 0 }

    Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json | Out-Null
    exit 0
} catch {
    $msg = "Malformed JSON in ${path}: $($_.Exception.Message)"
    $result = @{ decision = "block"; reason = $msg } | ConvertTo-Json -Compress
    Write-Output $result
    exit 0
}
