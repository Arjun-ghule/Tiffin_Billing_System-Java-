$JAVA_HOME = "C:\oracleJdk-26"

# Verify JDK path exists
if (-not (Test-Path $JAVA_HOME)) {
    Write-Error "JDK not found at $JAVA_HOME. Please verify your Java installation directory."
    Exit
}

Write-Host "Setting JAVA_HOME to $JAVA_HOME..."
$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$JAVA_HOME\bin;$env:PATH"

# Check if maven (mvn) is available in path
$mvnPath = ""
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvnPath = "mvn"
    Write-Host "System Maven detected."
} else {
    $localMvnDir = Join-Path $PSScriptRoot ".maven"
    $mvnCmd = Get-ChildItem -Path $localMvnDir -Filter "mvn.cmd" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    
    if ($mvnCmd) {
        $mvnPath = $mvnCmd.FullName
        Write-Host "Local Maven detected at $mvnPath."
    } else {
        Write-Host "Maven not found. Downloading a portable Maven distribution..."
        New-Item -ItemType Directory -Force -Path $localMvnDir | Out-Null
        
        $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
        $zipFile = Join-Path $localMvnDir "maven.zip"
        
        Write-Host "Downloading Apache Maven from $mavenUrl..."
        try {
            Invoke-WebRequest -Uri $mavenUrl -OutFile $zipFile -UserAgent "Mozilla/5.0"
            Write-Host "Extracting Maven package..."
            Expand-Archive -Path $zipFile -DestinationPath $localMvnDir
            Remove-Item $zipFile
            
            $mvnCmd = Get-ChildItem -Path $localMvnDir -Filter "mvn.cmd" -Recurse | Select-Object -First 1
            $mvnPath = $mvnCmd.FullName
            Write-Host "Local Maven installed at $mvnPath."
        } catch {
            Write-Error "Failed to download Maven: $_"
            Exit
        }
    }
}

# Run Maven build and start the GUI application
Write-Host "Building and running the Vasudha Tiffin Billing System..."
& $mvnPath clean compile exec:java
