REM Admin scripts starts in c:\windows\system32, then cd to script dir
cd %~dp0
java -Dlogging.path=logs -Djava.io.tmpdir=tmp -server -jar recipefoxServer.jar