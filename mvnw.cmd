@IF "%MVNW_VERBOSE%" == "true" ( @ECHO ON ) ELSE ( @ECHO OFF )
@SETLOCAL
@SET "MVNW_DIR=%~dp0"
@IF "%MVNW_DIR:~-1%"=="\" SET "MVNW_DIR=%MVNW_DIR:~0,-1%"
@SET "MAVEN_PROJECTBASEDIR=%MVNW_DIR%"

@IF NOT EXIST "%MVNW_DIR%\.mvn\wrapper\maven-wrapper.jar" (
    @powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $url='https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar'; $file=\"%MVNW_DIR%\.mvn\wrapper\maven-wrapper.jar\"; $dir=\"%MVNW_DIR%\.mvn\wrapper\"; New-Item -ItemType Directory -Force -Path \"$dir\"; (New-Object System.Net.WebClient).DownloadFile($url, \"$file\") }"
)

@java -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -cp "%MVNW_DIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
