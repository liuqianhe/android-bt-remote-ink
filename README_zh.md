# Android 蓝牙 HID 遥控器

[English](README.md) | **简体中文**

[![Stars](https://img.shields.io/github/stars/liuqianhe/android-bt-remote-ink?label=stars&logo=GitHub)](https://github.com/liuqianhe/android-bt-remote-ink)
[![GitHub](https://img.shields.io/github/downloads/liuqianhe/android-bt-remote-ink/total?label=GitHub&logo=GitHub)](https://github.com/liuqianhe/android-bt-remote-ink/releases)
[![license](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![build](https://img.shields.io/github/actions/workflow/status/liuqianhe/android-bt-remote-ink/apk.yml?label=build)](https://github.com/liuqianhe/android-bt-remote-ink/actions/workflows/apk.yml)
[![release](https://img.shields.io/github/v/release/liuqianhe/android-bt-remote-ink)](https://github.com/liuqianhe/android-bt-remote-ink/releases)

**Remote** 可将任意 Android 设备变成蓝牙（Classic）HID 键盘、鼠标和触控板，适用于所有接受通用蓝牙输入的平台。

它基于最初为 Android TV 设计的 [BT Remote](https://gitlab.com/Atharok/BtRemote) 构建，但经过改造后可用于 macOS、iOS（含 iPadOS）、Windows、Android、ChromeOS、Linux（含 SteamOS）以及其他支持蓝牙输入设备的平台。

与网络遥控工具不同，它不需要在目标设备上安装配套应用。Android 设备会以通用蓝牙 HID（Human Interface Device，人机接口设备）的身份出现，并通过蓝牙直接发送符合 HID 标准的键盘报告、媒体控制和鼠标输入。

[<img height="48" alt="从 GitHub Releases 下载" src="https://jqssun.github.io/images/badges/github.svg">](https://github.com/liuqianhe/android-bt-remote-ink/releases/latest)

<video loop src='https://github.com/user-attachments/assets/9bddf132-a861-4693-b408-2539646b7e8b' alt="演示" width="1200" style="display: block; margin: auto;"></video>

## 兼容性

- HID 设备（控制端）：Android 9.0+，且支持蓝牙
- HID 主机（目标设备）：
    - Android 4 及更高版本
    - Android TV、Google TV 和 Fire OS
    - ChromeOS
    - iOS 13（iPadOS 13）及更高版本
    - iOS 4 及更高版本（仅键盘）
    - tvOS 9.2 及更高版本
    - Mac OS X 10.3 及更高版本
    - Linux kernel 2.6 及更高版本
    - SteamOS
    - Windows XP Service Pack 2 及更高版本

## 实现方式

**Remote** 注册 Android 的 `BluetoothHidDevice` profile，并通过 HID 报告发送键盘、鼠标和遥控操作。

本项目是一个自包含的 Android 工程，不使用 Git submodule，也不会在构建时应用补丁。上游源码已直接导入 `app/`，并在此基础上添加了下面的定制功能。重构前的历史补丁归档在 [`docs/archive/patches`](docs/archive/patches)，不参与构建。

## 构建

要求：JDK 21 和 Android SDK。可以用 Android Studio 打开仓库根目录，或直接使用 Gradle wrapper。

```shell
./gradlew assembleDefaultDebug
./gradlew assembleDefaultRelease
```

Release 签名从 `local.properties` 读取，该文件不应提交到 Git：

```properties
storeFile=D:/path/to/btremote-release.jks
storePassword=...
keyAlias=btremote
keyPassword=...
sdk.dir=C:/Paths/Android_Path
```

Debug 构建产物：

```shell
app/build/outputs/apk/default/debug/app-default-debug.apk
```

已签名 Release 构建产物：

```shell
app/build/outputs/apk/default/release/app-default-release.apk
```

GitHub Actions 发布流程见 [CI](.github/workflows/apk.yml)。

## 定制功能

- 水墨屏主题：默认主题，纯白背景、黑色前景、卡片带描边；浅色、深色和跟随系统主题仍然保留。
- 全屏数字小键盘：HID Keypad 布局，可拖动位置，支持普通/水墨样式。
- 全屏鼠标：触控板、滚轮和三个按键，支持普通/水墨样式。
- 主界面底部导航：可在溢出菜单中切换方向键和鼠标触控板。
- Direct Input 键盘模式：将 Android 按键事件映射为 HID usage。
- 通过 `local.properties` 进行 Release 签名；密钥库不会提交到仓库。

## 致谢

- [Atharok](https://gitlab.com/Atharok/BtRemote)：原始 BT Remote
- [jqssun](https://github.com/jqssun/android-bt-remote)：多平台 HID 分支
