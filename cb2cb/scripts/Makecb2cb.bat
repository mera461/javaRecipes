set path=%JAVA_HOME%/bin;%PATH%
del cb2cb.jar
jar cvmf MANIFEST.MF cb2cb.jar -C ../../JavaRecipes/target/classes net
jar uf cb2cb.jar -C ../target/classes net
jar uf cb2cb.jar -C ../src/main/config log4j.properties

@REM c:\tools\bin\7za a txt2mx2-1.00.zip txt2mx2.bat.txt txt2mx2.jar txt2mx2.ini commas.txt lib
@REM c:\tools\bin\7za a -sfx7zS.sfx txt2mx2-1.00.exe txt2mx2.bat.txt txt2mx2.jar txt2mx2.ini commas.txt lib

copy ..\src\main\config\cb2cb.ini .
copy ..\src\main\config\"cb2cb - WithDescription.ini" .
copy ..\..\JavaRecipes\commas.txt .
cp -p -r ..\..\JavaRecipes\src\main\templates ..
chmod -R 777 ..\templates

"C:\program files\winrar\rar.exe" a -sfxDefault.sfx cb2cb-1.00.rar cb2cb.jar cb2cb.ini cb2cb.exe "cb2cb - WithDescription.ini" commas.txt ..\lib ..\templates

del cb2cb.ini
del "cb2cb - WithDescription.ini"
del commas.txt






 