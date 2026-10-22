param(
    [ValidateSet('Test', 'Inspect', 'Compare')][string]$Mode = 'Test',
    [string]$Corpus,
    [int]$Article = 0,
    [int]$Left = 0,
    [int]$Right = 0,
    [switch]$PreserveText,
    [switch]$NormalizeWhitespace,
    [switch]$Lowercase,
    [switch]$NormalizeUnicode,
    [switch]$JoinLineHyphenation,
    [switch]$ReconstructWordSpacing,
    [switch]$HideFontWarnings,
    [ValidateSet('REJECT', 'ZERO', 'ONE')][string]$EmptyUnionPolicy = 'REJECT'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$buildDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ('bibliometria-pdf-' + [guid]::NewGuid())
$utf8 = New-Object System.Text.UTF8Encoding($false)
$previousOutputEncoding = [Console]::OutputEncoding
$previousPipeEncoding = $OutputEncoding
$javaOptions = @('-Dfile.encoding=UTF-8', '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8')
try {
    [Console]::OutputEncoding = $utf8
    $OutputEncoding = $utf8
    foreach ($command in @('java', 'javac', 'mvn')) {
        if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
            throw "Required tool missing from PATH: $command"
        }
    }
    if ($Mode -ne 'Test' -and -not $Corpus) { throw 'Provide -Corpus with a ZIP or PDF directory' }
    New-Item -ItemType Directory -Path $buildDirectory | Out-Null
    if ($HideFontWarnings) {
        # Filter only unmapped-glyph warnings on this execution's console handler.
        $loggingPath = Join-Path $buildDirectory 'logging.properties'
        $loggingConfiguration = @'
handlers=java.util.logging.ConsoleHandler
.level=INFO
java.util.logging.ConsoleHandler.level=INFO
java.util.logging.ConsoleHandler.encoding=UTF-8
java.util.logging.ConsoleHandler.filter=com.uniquindio.bibliometria.ingestion.PdfFontWarningFilter
'@
        [System.IO.File]::WriteAllText($loggingPath, $loggingConfiguration, $utf8)
        $javaOptions += "-Djava.util.logging.config.file=$loggingPath"
    }
    # This descriptor exists only in TEMP; it does not initialize the team's Maven project.
    $descriptor = @'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>local.validation</groupId><artifactId>pdf-processing</artifactId><version>1</version>
  <dependencies>
    <dependency><groupId>org.apache.pdfbox</groupId><artifactId>pdfbox</artifactId><version>3.0.8</version></dependency>
    <dependency><groupId>org.junit.platform</groupId><artifactId>junit-platform-console-standalone</artifactId><version>1.11.4</version></dependency>
  </dependencies>
</project>
'@
    $pomPath = Join-Path $buildDirectory 'pom.xml'
    [System.IO.File]::WriteAllText($pomPath, $descriptor, $utf8)
    & mvn -q -f $pomPath dependency:copy-dependencies "-DoutputDirectory=$buildDirectory\lib"
    if ($LASTEXITCODE -ne 0) { throw 'Dependency resolution failed' }
    $packageRoot = Join-Path $projectRoot 'src/main/java/com/uniquindio/bibliometria'
    $sourceDirectories = @('ingestion', 'preprocessing', 'algorithms/similarity/classic')
    $sources = @($sourceDirectories | ForEach-Object {
        Get-ChildItem -LiteralPath (Join-Path $packageRoot $_) -Filter *.java |
            Where-Object { $_.Name -ne 'test.java' } | ForEach-Object FullName
    })
    $classDirectory = Join-Path $buildDirectory 'classes'
    & javac -encoding UTF-8 -cp "$buildDirectory\lib\*" -d $classDirectory @sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
    $classpath = "$classDirectory;$buildDirectory\lib\*"
    if ($Mode -eq 'Test') {
        $testRoot = Join-Path $projectRoot 'src/test/java/com/uniquindio/bibliometria'
        $tests = @(@('ingestion', 'preprocessing', 'algorithms/similarity/classic') | ForEach-Object {
            Get-ChildItem -LiteralPath (Join-Path $testRoot $_) -Filter *Test.java | ForEach-Object FullName
        })
        & javac -encoding UTF-8 -cp $classpath -d $classDirectory @tests
        if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
        if ($Corpus) { $javaOptions += "-Dbibliometria.corpus=$Corpus" }
        & java @javaOptions -cp $classpath org.junit.platform.console.ConsoleLauncher execute --scan-classpath --disable-banner --details=summary
    } else {
        $cliArguments = @($Mode.ToLowerInvariant(), $Corpus)
        if ($Mode -eq 'Inspect' -and $Article -gt 0) { $cliArguments += "$Article" }
        if ($Mode -eq 'Compare') {
            if ($Left -lt 1 -or $Right -lt 1) { throw 'Provide -Left and -Right article numbers' }
            $cliArguments += @("$Left", "$Right", "--empty=$EmptyUnionPolicy")
        }
        if ($Mode -eq 'Compare' -or ($Mode -eq 'Inspect' -and $Article -gt 0)) {
            if ($PreserveText) { $cliArguments += '--preserve-text' }
            if ($NormalizeWhitespace) { $cliArguments += '--whitespace' }
            if ($Lowercase) { $cliArguments += '--lowercase' }
            if ($NormalizeUnicode) { $cliArguments += '--unicode' }
            if ($JoinLineHyphenation) { $cliArguments += '--join-hyphenation' }
            if ($ReconstructWordSpacing) { $cliArguments += '--reconstruct-spacing' }
        }
        & java @javaOptions -cp $classpath com.uniquindio.bibliometria.ingestion.PdfProcessingCli @cliArguments
    }
    if ($LASTEXITCODE -ne 0) { throw 'Execution failed' }
} finally {
    [Console]::OutputEncoding = $previousOutputEncoding
    $OutputEncoding = $previousPipeEncoding
    # Never delete a computed path without checking its resolved parent first.
    if (Test-Path -LiteralPath $buildDirectory) {
        $resolvedBuild = [System.IO.Path]::GetFullPath($buildDirectory)
        $resolvedTemp = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath()).TrimEnd('\')
        if ([System.IO.Path]::GetDirectoryName($resolvedBuild) -eq $resolvedTemp -and
            [System.IO.Path]::GetFileName($resolvedBuild).StartsWith('bibliometria-pdf-')) {
            Remove-Item -LiteralPath $resolvedBuild -Recurse -Force
        }
    }
}
