param([switch]$Qa, [switch]$Device, [switch]$SocketWorkaround)
$ErrorActionPreference = 'Stop'
if (-not $env:JAVA_HOME) {
    $studioRuntime = Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'
    if (Test-Path -LiteralPath $studioRuntime) { $env:JAVA_HOME = $studioRuntime }
}
$originalJavaOptions = $env:JAVA_TOOL_OPTIONS
try {
    if ($SocketWorkaround) {
        # A nonexistent Unix socket folder makes the Windows JDK use TCP pipes.
        # This changes only this process, never the machine's Java installation.
        $socketFolder = Join-Path ([IO.Path]::GetPathRoot($env:TEMP)) ('iq-absent-socket-' + [guid]::NewGuid().ToString('N'))
        $env:JAVA_TOOL_OPTIONS = "$originalJavaOptions -Djdk.net.unixdomain.tmpdir=$socketFolder"
    }
    $tasks = @(':app:testDebugUnitTest', ':app:assembleDebug', ':app:lintDebug')
    if ($Qa) { $tasks += '-PpuzzleQa=true' }
    if ($Device) { $tasks += ':app:connectedDebugAndroidTest' }
    & (Join-Path $PSScriptRoot '..\gradlew.bat') @tasks --console=plain
    exit $LASTEXITCODE
} finally { $env:JAVA_TOOL_OPTIONS = $originalJavaOptions }
