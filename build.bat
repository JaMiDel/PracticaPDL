@echo off
REM Simple build script for Windows targeting Java 21 (assumes javac on PATH)
if "%JAVA_HOME%"=="" (
  echo WARNING: JAVA_HOME not set. Using javac from PATH.
) else (
  echo Using JAVA_HOME=%JAVA_HOME%
)
mkdir out 2>nul
pushd src
for /R %%f in (*.java) do (
  echo Compiling %%f
  javac -d ..\out "%%f"
)
popd
echo Build complete. Classes are in the out\ directory.
pause
