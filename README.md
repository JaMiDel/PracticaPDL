# PracticaPDL

## Java 21 upgrade notes

This repository is prepared to be built with Java 21. The GitHub Actions workflow `.github/workflows/java-21.yml` will run a Windows build using Temurin JDK 21.

To build locally on Windows (cmd.exe), either install a JDK 21 and set `JAVA_HOME`, or ensure `javac` from JDK 21 is on your PATH, then run:

```powershell
build.bat
```

If you want me to attempt automated code upgrades (OpenRewrite) I can proceed, but the integrated upgrade planner requires a Copilot Pro/Enterprise plan which isn't available from this environment. I can still apply manual changes and run rewrite recipes locally if you give me the go-ahead.

# PracticaPDL