# One-time machine setup

Do this once per computer. After that, builds are automatic: the Gradle wrapper downloads Gradle itself, Gradle downloads the JDK it compiles with and all libraries, and the Android plugin downloads missing SDK platforms.

What the wrapper **cannot** do on its own is start without Java, or guess where your Android SDK lives. Those two things are this setup.

Run **Terminal → Run Task… → Check setup** in VS Code (or `./scripts/check-setup.sh`) at any time to see what's missing.

## 1. Install Android Studio once

Download it from [developer.android.com/studio](https://developer.android.com/studio), run it, and finish the setup wizard. It installs the **Android SDK** (incl. `adb`). You don't have to code in it.

> Android Studio also bundles its own Java, but recent versions bundle **Java 25**, which is too new for this project's Gradle 8.14 (it supports Java up to 24). So we install a separate **JDK 21** (long-term support) for builds.
> Inside Android Studio, if it asks "Please Select Gradle JVM", choose **Use JVM 21**.

## 2. Install JDK 21 and set environment variables permanently

This is the step that makes VS Code, the terminal and Claude Code all find Java and the SDK without any per-project fiddling.

### macOS (zsh)
```bash
brew install --cask temurin@21
```
Add to `~/.zshrc`, then restart VS Code completely:
```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

### Linux (Debian/Ubuntu)
```bash
sudo apt install openjdk-21-jdk
```
Add to `~/.bashrc`:
```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

### Windows (PowerShell, once)
```powershell
winget install EclipseAdoptium.Temurin.21.JDK
$jdk = (Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-21*" | Select-Object -First 1).FullName
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk, "User")
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
$p = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$p;$jdk\bin;$env:LOCALAPPDATA\Android\Sdk\platform-tools", "User")
```
Then sign out and back in (or reboot) so every app picks them up.

*Already have a JDK 21?* Android Studio shows the path of the JDK it picked under **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**. You can point `JAVA_HOME` there instead of installing Temurin.

> **Important:** fully quit and reopen VS Code after setting these. VS Code only reads environment variables when it starts. On macOS, apps launched from the Dock may not see `~/.zshrc`; if the check still fails, start VS Code from a terminal with `code .`.

## 3. Accept the Android SDK licenses once

Lets Gradle download missing SDK platforms by itself:
```bash
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses
```
If `cmdline-tools` is missing: Android Studio → Settings → Android SDK → SDK Tools → tick **Android SDK Command-line Tools**.

## 4. Phone
Settings → About phone → tap **Build number** 7× → Developer options → enable **USB debugging**. Connect, accept the prompt, `adb devices` lists it.

## Still failing?

| Symptom | Likely cause |
|---|---|
| "Could not download gradle-8.14.3-bin.zip" / timeout | VPN, proxy or firewall blocking `services.gradle.org`. Set proxy in `~/.gradle/gradle.properties` (`systemProp.https.proxyHost=…`, `systemProp.https.proxyPort=…`). |
| "JAVA_HOME is not set" / "no java" | Step 2, then fully restart VS Code. |
| "Android Gradle plugin requires Java 17" | `JAVA_HOME` points to an old Java. Point it at JDK 21 (step 2). |
| "Gradle 8.14.3 is incompatible with Gradle JVM version 25" | Java is too new. In Android Studio choose **Use JVM 21**; for VS Code/terminal point `JAVA_HOME` at JDK 21 (step 2). |
| "SDK location not found" | `ANDROID_HOME` not set or VS Code not restarted. |
