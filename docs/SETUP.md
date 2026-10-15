# One-time machine setup

Do this once per computer. After that, builds are automatic: the Gradle wrapper downloads Gradle itself, Gradle downloads the JDK it compiles with and all libraries, and the Android plugin downloads missing SDK platforms.

What the wrapper **cannot** do on its own is start without Java, or guess where your Android SDK lives. Those two things are this setup.

Run **Terminal → Run Task… → Check setup** in VS Code (or `./scripts/check-setup.sh`) at any time to see what's missing.

## 1. Install Android Studio once

Download it from [developer.android.com/studio](https://developer.android.com/studio), run it, and finish the setup wizard. It installs:
- the **Android SDK** (incl. `adb`)
- a bundled **JDK 17+** ("JBR") that Gradle can use

You don't have to code in it.

## 2. Set three environment variables permanently

This is the step that makes VS Code, the terminal and Claude Code all find Java and the SDK without any per-project fiddling.

### macOS (zsh)
Add to `~/.zshrc`, then restart VS Code completely:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

### Linux (bash)
Add to `~/.bashrc` (adjust the Studio path to where you unpacked it):
```bash
export JAVA_HOME="$HOME/android-studio/jbr"
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

### Windows (PowerShell, once)
```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Android\Android Studio\jbr", "User")
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
$p = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$p;C:\Program Files\Android\Android Studio\jbr\bin;$env:LOCALAPPDATA\Android\Sdk\platform-tools", "User")
```
Then sign out and back in (or reboot) so every app picks them up.

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
| "Android Gradle plugin requires Java 17" | `JAVA_HOME` points to an old Java. Point it at Android Studio's `jbr`. |
| "SDK location not found" | `ANDROID_HOME` not set or VS Code not restarted. |
