# Bluetooth HID Remote for Android

**English** | [简体中文](README_zh.md)

[![Stars](https://img.shields.io/github/stars/liuqianhe/android-bt-remote-ink?label=stars&logo=GitHub)](https://github.com/liuqianhe/android-bt-remote-ink)
[![GitHub](https://img.shields.io/github/downloads/liuqianhe/android-bt-remote-ink/total?label=GitHub&logo=GitHub)](https://github.com/liuqianhe/android-bt-remote-ink/releases)
[![license](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![build](https://img.shields.io/github/actions/workflow/status/liuqianhe/android-bt-remote-ink/apk.yml?label=build)](https://github.com/liuqianhe/android-bt-remote-ink/actions/workflows/apk.yml)
[![release](https://img.shields.io/github/v/release/liuqianhe/android-bt-remote-ink)](https://github.com/liuqianhe/android-bt-remote-ink/releases)

**Remote** turns any Android device into a Bluetooth (Classic) HID keyboard, mouse, and trackpad designed for all platforms that accept generic Bluetooth input. 

It is built on top of the original [BT Remote designed for Android TV](https://gitlab.com/Atharok/BtRemote) but instead made to work with macOS, iOS (+ iPadOS), Windows, Android, ChromeOS, Linux (+ SteamOS) as well as all other platforms that support Bluetooth input devices.

Unlike network remote-control tools, this does not require a companion application on the target. The Android device presents itself as a generic Bluetooth Human Interface Device (HID) and sends HID-compliant keyboard reports, media controls, and mouse input directly over Bluetooth.

[<img height="48" alt="Download from GitHub Releases" src="https://jqssun.github.io/images/badges/github.svg">](https://github.com/liuqianhe/android-bt-remote-ink/releases/latest)

<video loop src='https://github.com/user-attachments/assets/9bddf132-a861-4693-b408-2539646b7e8b' alt="demo" width="1200" style="display: block; margin: auto;"></video>

## Compatibility

- HID device (controller): Android 9.0+ with Bluetooth
- HID host (target): 
    - Android 4 and later
    - Android TV, Google TV, and Fire OS
    - ChromeOS
    - iOS 13 (iPadOS 13) and later
    - iOS 4 and later (keyboard only)
    - tvOS 9.2 and later
    - Mac OS X 10.3 and later
    - Linux kernel 2.6 and later
    - SteamOS
    - Windows XP Service Pack 2 and later

## Implementation

**Remote** registers with Android's `BluetoothHidDevice` profile and sends HID reports for keyboard, mouse, and remote control actions.

This fork is a self-contained Android project. It does not use a Git submodule and does not apply patches at build time. Upstream source was imported directly into `app/`, then extended with the customizations below. The historical pre-refactor patches are archived under [`docs/archive/patches`](docs/archive/patches) and are not part of the build.

## Build

Requirements: JDK 21 and Android SDK. Open the repository root in Android Studio, or use the Gradle wrapper directly.

```shell
./gradlew assembleDefaultDebug
./gradlew assembleDefaultRelease
```

Release signing is read from `local.properties`, which must stay outside Git:

```properties
storeFile=D:/path/to/btremote-release.jks
storePassword=...
keyAlias=btremote
keyPassword=...
sdk.dir=C:/Paths/Android_Path
```

Debug build:

```shell
app/build/outputs/apk/default/debug/app-default-debug.apk
```

Signed Release build:

```shell
app/build/outputs/apk/default/release/app-default-release.apk
```

Check out the [CI](.github/workflows/apk.yml) for the GitHub Actions release flow.

## Customizations

- E-ink theme: default theme, pure white background, black foreground, outlined cards. Light, dark, and system themes are still available.
- Full-screen numeric keypad: HID Keypad layout, draggable position, normal/ink styles.
- Full-screen mouse: trackpad, scroll wheel, three buttons, normal/ink styles.
- Inline bottom navigation: switch between direction pad and mouse from the overflow menu.
- Direct Input keyboard mode: maps Android key events to HID usages.
- Release signing through `local.properties`; keystores are never committed.

## Credits

- [Atharok](https://gitlab.com/Atharok/BtRemote) for the original BT Remote
- [jqssun](https://github.com/jqssun/android-bt-remote) for the multi-platform HID fork
