# Fork 定制功能与标准项目重构说明

> 状态：重构已在 `dev` 分支完成（工作区改动，尚未提交）。源码已固化到根目录，`src` submodule、`.gitmodules` 和构建时补丁步骤均已移除。本文保留重构前的分析作为迁移记录。
>
> 当前结构：根目录就是标准 Gradle 项目，`app/` 为应用模块，Android Studio 直接打开仓库根目录。
>
> 当前代码基准：上游 `BtRemote` v1.12.1，commit `56ea84e6b8f16e45c6467768b72e1bcd8f322f72`，再叠加 8 个定制补丁。
>
> 重构前父仓库 HEAD：`1af2073 构建脚本支持本地 Release 签名`
>
> 历史补丁：`docs/archive/patches/0001-support-pc.patch` 至 `0008-inline-mouse-mode.patch`，仅归档，不参与构建。

## 1. 背景

本仓库最初采用“父仓库 + 上游 submodule + 补丁”的方式维护定制：

- `src/` 是 Git submodule，指向 `https://gitlab.com/Atharok/BtRemote.git`；
- 父仓库只记录 `src` 的 commit 指针，不保存上游源码内容；
- 所有定制写在 `patches/`；
- `build.sh` 每次构建前通过 `git am` 把补丁应用到 `src`；
- 额外资源通过 `res/` 覆盖到 `src/app/src/main/res/`。

这种方式适合短期跟踪上游，但有明显问题：

1. 上游仓库不可用、force-push、删除 tag/commit 时，干净克隆可能失败；
2. `src` 被 `build.sh` 修改后是 dirty 状态，再次构建前必须 reset/clean；
3. 补丁与上游版本强绑定，上游改动后 `git am` 容易冲突；
4. 对自用项目来说，构建链过重，直接修改源码更直观；
5. CI 必须带 `submodules: recursive`，并且依赖 `build.sh` 的补丁步骤。

目标：改成一个普通的、自包含的 Android 项目仓库。构建时不再需要 submodule，也不再需要 `git am` 应用补丁；同时保留现有全部定制功能、签名和发布配置。

## 2. 重构前构建链（历史）

### 2.1 submodule

- `.gitmodules` 指向 `https://gitlab.com/Atharok/BtRemote.git`
- 父仓库 gitlink：`160000 commit 56ea84e6b8f16e45c6467768b72e1bcd8f322f72 src`
- 上游源码实际位于 `src/app/src/main/...`
- `src` 的 `.git` 是文件，指向父仓库的 `.git/modules/src`

### 2.2 `build.sh`

当前流程：

1. 设置版本：`VERSION_CODE=7`、`VERSION_NAME=0.0.7`；
2. 删除残留的 `.git/modules/src/rebase-apply`；
3. 如果提供了签名环境变量，生成 `src/local.properties`；
4. 进入 `src`；
5. 用 `git am` 依次应用 `patches/*.patch`；
6. 用 `cp -R ../res/. app/src/main/res/` 覆盖图标等资源；
7. 用 `sed` 修改 `app/build.gradle.kts`：
   - `applicationId = "io.github.jqssun.btremote"`
   - `versionCode = 7`
   - `versionName = "0.0.7"`
8. 用 `sed` 修改字符串和常量：
   - 平台列表由 `Android TV` 改为 `macOS/iOS/iPadOS/tvOS/Windows/Linux`
   - `app_name` 改为 `Bluetooth Remote`
   - `SOURCE_CODE_LINK` / `WEB_SITE_LINK` 改为当前 fork 地址
9. 之后手动进入 `src` 执行 Gradle。

注意：`build.sh` 会就地修改 `src`。重复执行前需要把 `src` 还原到上游 commit。

### 2.3 `generate.sh`

从 `src` 当前提交重新导出补丁：

```bash
git -C src format-patch <merge-base>..HEAD -o ../patches ...
```

它只是维护补丁的辅助脚本，重构后可以删除或归档。

### 2.4 签名

- 密钥文件：`keys/btremote-release.jks`（仓库外，不提交）
- alias：`btremote`
- 密码：`keys/keystore.env`
- 说明：`keys/README.md`
- `build.sh` 读取以下环境变量并写入 `src/local.properties`：
  - `ANDROID_KEY_STORE_FILE`
  - `ANDROID_KEY_STORE_PASSWORD`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEY_PASSWORD`
- 补丁 `0003` 让 `app/build.gradle.kts` 从 `local.properties` 读取签名，而不是直接读环境变量。
- `local.properties` 含密码，已被 gitignore，不能提交。

Release 证书 SHA-256：

```text
21:AF:74:52:6D:E7:9E:A6:DD:92:20:F2:36:1F:CC:9A:C3:3C:CE:B7:10:86:C4:E9:C2:E8:D6:5E:F3:A5:D2:17
```

## 3. 已添加功能与实现

以下功能按补丁顺序说明。当前路径均相对于 `app/src/main/`；重构前位于 `src/app/src/main/`。

### 3.1 `0001-support-pc.patch`：PC 兼容、默认鼠标导航、HID 报告调整

功能与实现：

- 平台列表加入 PC 平台，配合 `build.sh` 的字符串替换；
- 默认开启鼠标导航：`USE_MOUSE_NAVIGATION_BY_DEFAULT = true`；
- 调整鼠标/遥控报告的 report ID 顺序；
- 在 `BluetoothHidCore` 中抽出 `sendAndroidReport` 发送入口，统一走报告发送逻辑，为不同平台的差异处理预留位置。

主要文件：

- `java/com/atharok/btremote/common/utils/Constants.kt`
- `java/com/atharok/btremote/data/bluetooth/BluetoothHidCore.kt`

注意事项：

- report ID 与 HID descriptor 必须匹配，后续重构时不要只改发送端而漏掉 descriptor。
- 默认鼠标导航同时影响主界面初始显示的是方向键还是内嵌鼠标。

### 3.2 `0002-direct-input.patch`：Direct Input 键盘

功能与实现：

- 新增 `AndroidKeyMapper`，把 Android/Compose 的按键事件映射为 HID keyboard usage；
- `VirtualKeyboard` 增加 Direct Input 模式开关和按键预览处理；
- `RemoteScreen` 把 `virtualKeyboardLayout` 传入键盘；
- 顶部键盘输入入口可以进入 Direct Input 模式。

主要文件：

- `java/com/atharok/btremote/common/utils/AppIcons.kt`
- `java/com/atharok/btremote/ui/screens/RemoteScreen.kt`
- `java/com/atharok/btremote/ui/views/keyboard/AndroidKeyMapper.kt`（新增）
- `java/com/atharok/btremote/ui/views/keyboard/VirtualKeyboard.kt`
- `res/values/strings.xml`

注意事项：

- 该补丁只修改默认英文 `strings.xml`，中文环境会回退到英文文案；如需完整中文化，应在重构时补齐 `values-zh-rCN/strings.xml`。

### 3.3 `0003-read-keys.patch`：本地签名与 native 符号

功能与实现：

- `app/build.gradle.kts` 从 `local.properties` 读取 Release 签名；
- 打包时保留 native debug symbols：`jniLibs.keepDebugSymbols += "**/*.so"`；
- `build.sh` 负责从环境变量生成 `local.properties`。

主要文件：

- `app/build.gradle.kts`
- `build.sh`

注意事项：

- 补丁名“read keys”容易误解，它并不读取设备按键，实际是签名/构建配置。
- 该补丁用 `git apply --stat` 可能在 line 16 报 corrupt，但 `git am` 和 `build.sh` 可以正常应用；重构时如果重新应用补丁，优先使用 `git am`。

### 3.4 `0004-ink-theme-numpad.patch`：水墨屏主题 + 全屏数字小键盘

#### 水墨屏主题

功能：

- 水墨屏是默认主题，新安装或未手动选择主题时自动使用；
- 设置中新增“水墨屏（黑白）”主题，系统/浅色/深色主题继续保留；
- 背景纯白 `#FFFFFF`；
- 文字、图标、主要控件接近纯黑 `#000000`；
- 不使用动态取色；
- 卡片/键帽增加黑色描边，避免水墨屏上边界不清；
- 系统栏使用深色图标。

实现：

- `ThemeEntity` 新增 `E_INK` 枚举值，设置页的主题列表自动出现该选项；
- `Constants.kt` 的 `DEFAULT_THEME` 设为 `ThemeEntity.E_INK`；
- `Color.kt` 增加水墨屏背景/前景/描边颜色；
- `Theme.kt` 增加 `eInkColorScheme`，并在主题分支中固定为浅色、非动态取色；
- `Card.kt` 通过 `MaterialTheme.colorScheme.background == Color.White` 判断水墨屏，并给 `ElevatedCard` 加 1dp 黑色描边；
- 数字小键盘和鼠标界面通过相同判断切换为水墨样式。

主要文件：

- `java/com/atharok/btremote/common/utils/Constants.kt`
- `java/com/atharok/btremote/domain/entities/settings/ThemeEntity.kt`
- `java/com/atharok/btremote/ui/theme/Color.kt`
- `java/com/atharok/btremote/ui/theme/Theme.kt`
- `java/com/atharok/btremote/ui/components/Card.kt`
- `java/com/atharok/btremote/ui/screens/RemoteScreen.kt`
- `res/values/strings.xml`、`res/values-zh-rCN/strings.xml`

#### 全屏数字小键盘

功能：

- 从遥控页右上角菜单打开；
- 接近全屏显示；
- 布局接近实体键盘数字区：
  - 第一行：`NumLock` `/` `*` `-`
  - 第二行：`7` `8` `9` `+`
  - 第三行：`4` `5` `6`
  - 第四行：`1` `2` `3` `Enter`
  - 第五行：`0` `.`
  - `+` 和 `Enter` 纵向跨两行；
- 支持正常/水墨两种样式；
- 按键按下发送 key down，抬起发送 key up。

实现：

- `KeyboardKey` 增加 HID Keypad 键码（`0x53`–`0x63`）；
- 新增 `NumpadDialog.kt`，内部用 `NumpadLayout`、`NumpadRow`、`NumpadKey` 组织布局；
- 按下：`sendKeyboardKeyReport(byteArrayOf(0x00, key.byte))`；
- 抬起：`sendKeyboardKeyReport(REMOTE_INPUT_NONE)`；
- 正常样式使用 `TextKeyboardKey`，水墨样式使用带黑边的 `RawButton`；
- `RemoteScreen` 增加 `isNumpadVisible` 状态和菜单入口；
- `AppIcons` 增加 `Numpad`、`Close` 图标。

主要文件：

- `java/com/atharok/btremote/common/utils/AppIcons.kt`
- `java/com/atharok/btremote/domain/entities/remoteInput/keyboard/KeyboardKey.kt`
- `java/com/atharok/btremote/ui/screens/RemoteScreen.kt`
- `java/com/atharok/btremote/ui/views/keyboard/NumpadDialog.kt`（新增）
- `res/values/strings.xml`、`res/values-zh-rCN/strings.xml`

### 3.5 `0005-numpad-position-adjust.patch`：小键盘位置调整

功能：

- 小键盘标题栏增加“调整位置”模式；
- 可上下拖动小键盘；
- 位置限制在顶部控制栏与底部安全区域之间；
- 关闭再打开后保留位置；
- 放大正常样式下的数字字号。

实现：

- `NumpadDialog` 增加 `offsetFraction` / `onOffsetFractionChange`；
- `NUMPAD_DEFAULT_OFFSET_FRACTION = 0.67f`，`NUMPAD_HEIGHT_FRACTION = 0.65f`；
- `RemoteScreen` 用 `rememberSaveable` 保存 `numpadOffsetFraction`；
- `TextKeyboardKey` 增加 `textHeightFraction` 参数，数字键使用 `0.4f`，让数字在小键盘键帽内更醒目；
- 拖动模式与正常按键模式互斥，避免拖动时误发按键。

主要文件：

- `java/com/atharok/btremote/ui/views/keyboard/NumpadDialog.kt`
- `java/com/atharok/btremote/ui/screens/RemoteScreen.kt`
- `java/com/atharok/btremote/ui/components/customButtons/TextButton.kt`
- `res/values/strings.xml`、`res/values-zh-rCN/strings.xml`

### 3.6 `0006-mouse-dialog.patch`：独立全屏鼠标

功能：

- 新增独立全屏鼠标界面；
- 从遥控页打开，铺满屏幕；
- 包含鼠标触控板、滚轮、左/中/右键；
- 支持关闭按钮和标题栏；
- 对话框打开时适配系统栏图标颜色。

实现：

- 新增 `MouseDialog.kt`，用全屏 `Dialog` + `Surface` 承载 `MousePad`；
- 新增 `DialogSystemBarAppearance.kt`，等待窗口 attach 后设置浅色/深色系统栏图标，并在退出时恢复；
- `RemoteScreen` 增加 `isMouseDialogVisible` 状态和入口；
- `NumpadDialog` 复用系统栏适配逻辑。

主要文件：

- `java/com/atharok/btremote/ui/components/DialogSystemBarAppearance.kt`（新增）
- `java/com/atharok/btremote/ui/screens/RemoteScreen.kt`
- `java/com/atharok/btremote/ui/views/keyboard/NumpadDialog.kt`
- `java/com/atharok/btremote/ui/views/mouse/MouseDialog.kt`（新增）

### 3.7 `0007-mouse-ink-topbar.patch`：鼠标水墨样式 + 顶栏小键盘入口

功能：

- 数字小键盘和全屏鼠标共用“正常/水墨”样式切换控件；
- 鼠标界面增加水墨样式：
  - 触控板白底黑边；
  - 左/中/右键白底黑边；
  - 按下时黑底白字；
  - 滚轮按钮同样白底黑边；
- 顶栏增加小键盘快捷入口。

实现：

- 新增 `DialogInkStyleToggle.kt`，渲染两个胶囊按钮；
- `MouseDialog` 增加 `inkStyle` 状态和样式切换；
- `MousePad` 增加 `inkStyle`，水墨模式下使用白底黑边触控板；
- `MousePadLayout` 增加 `inkStyle` 参数，并向下传递到 `InkMouseButton` / `InkIconHoldButton`；
- `NumpadDialog` 改用共享的 `DialogInkStyleToggle`；
- `RemoteScreen` 顶栏增加小键盘按钮。

主要文件：

- `java/com/atharok/btremote/ui/components/DialogInkStyleToggle.kt`（新增）
- `java/com/atharok/btremote/ui/views/mouse/MouseDialog.kt`
- `java/com/atharok/btremote/ui/views/mouse/MousePad.kt`
- `java/com/atharok/btremote/ui/views/mouse/MousePadLayout.kt`
- `java/com/atharok/btremote/ui/views/keyboard/NumpadDialog.kt`
- `java/com/atharok/btremote/ui/screens/RemoteScreen.kt`
- `res/values/strings.xml`、`res/values-zh-rCN/strings.xml`

### 3.8 `0008-inline-mouse-mode.patch`：恢复主界面内嵌鼠标

功能：

- 主界面底部恢复“方向键 / 鼠标”内嵌切换；
- 溢出菜单新增“方向键 / 鼠标”切换入口；
- 独立全屏鼠标继续保留，并改名为“全屏鼠标”；
- 独立全屏鼠标不再随默认鼠标导航设置自动弹出；
- 顶栏仍保留小键盘、鼠标、键盘输入三个图标。

实现：

- 新增私有枚举 `NavigationToggle { DIRECTION, MOUSE }`；
- `RemoteScreen` 用 `rememberSaveable` 保存 `navigationToggle`，初值由 `useMouseNavigationByDefault` 决定；
- `NavigationLayout` 在 `RemotePadLayout` 和 `MousePadLayout` 之间切换；
- 内嵌鼠标同样使用当前主题判断是否启用 `inkStyle`；
- `isMouseDialogVisible` 初始值改为 `false`，只由用户点击“全屏鼠标”打开；
- 溢出菜单加入“方向键 / 鼠标”切换。

主要文件：

- `java/com/atharok/btremote/ui/screens/RemoteScreen.kt`
- `res/values/strings.xml`、`res/values-zh-rCN/strings.xml`

### 3.9 资源覆盖与打包信息

- `res/drawable/ic_launcher_foreground.xml`
- `res/drawable/ic_splash_screen.xml`
- `res/drawable/notification_icon.xml`

`build.sh` 会把 `res/` 整个复制到 `src/app/src/main/res/`，因此这些文件是整文件覆盖，不是补丁。

打包信息：

- applicationId：`io.github.jqssun.btremote`
- Debug applicationId：`io.github.jqssun.btremote.debug`
- versionCode：`7`
- versionName：`0.0.7`
- app 名称：`Bluetooth Remote`
- Release 签名：`keys/btremote-release.jks`

## 4. 重构为标准项目

### 4.0 执行结果

本次已在 `dev` 分支完成以下操作：

- 把 `src` 还原到上游 `56ea84e`，应用全部 8 个补丁，并复制 `res/` 覆盖资源；
- 把 `src/app`、`src/gradle`、Gradle wrapper、根构建文件移动到仓库根目录；
- 删除 `src`、`.gitmodules`、`build.sh`、`generate.sh` 和根目录 `res/`；
- 把历史补丁移动到 `docs/archive/patches/`，不再参与构建；
- 保留 `applicationId=io.github.jqssun.btremote`、`versionCode=7`、`versionName=0.0.7`；
- 保留从 `local.properties` 读取 Release 签名的方式；
- 更新 CI 为根目录 Gradle 构建，去掉 submodule 和 Patch 步骤；
- Debug 和 Release 均已构建成功；Release 证书 SHA-256 仍为 `21:AF:74:...:D2:17`；
- 尚未提交、尚未推送。

### 4.1 目标结构

推荐直接拍平为根目录 Gradle 项目：

```text
android-bt-remote-ink/
├── .github/
├── app/
├── gradle/
├── fastlane/
├── docs/
├── keys/                  # 仓库外，不提交
├── res/                   # 可合并进 app/src/main/res 后删除
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── README.md
└── LICENSE
```

关键点：根目录就是 Android/Gradle 项目，Android Studio 直接打开根目录；不再需要 `src` submodule，也不再需要构建时应用补丁。

如果希望降低一次性改动风险，也可以采用保守方案：

```text
android-bt-remote-ink/
├── app/ ...              # 保持 src 作为普通目录
├── src/                  # 普通源码目录，不再是 submodule
├── build.sh              # 去掉 git am / reset
└── ...
```

保守方案路径改动少，但不如拍平方案标准。建议最终仍拍平。

### 4.2 执行步骤（已完成）

1. **冻结当前状态**
   - 先打 tag 或备份分支，例如 `pre-refactor`；
   - 确认当前 Release APK 可构建、可安装；
   - 保留 `patches/` 作为历史归档。

2. **生成最终源码树**
   - 在干净的 `src` 上应用全部补丁并复制 `res/`；
   - 或直接从现有已构建状态导出；
   - 确认 `src` 中包含全部 8 个补丁的最终效果。

3. **转换 submodule 为普通目录**
   - 删除 `src/.git` 文件（它只是指向父仓库 `.git/modules/src` 的指针）；
   - 删除父仓库 `.gitmodules`；
   - 删除 `.git/modules/src`；
   - 把 `src` 作为普通目录加入父仓库。

4. **拍平到根目录**
   - 把 `src/app`、`src/gradle`、`src/gradlew`、`src/gradlew.bat`、`src/build.gradle.kts`、`src/settings.gradle.kts`、`src/gradle.properties` 移到根目录；
   - `src/gradle/libs.versions.toml` 等版本目录一并移动；
   - 不覆盖根目录已有的 `.github`、`fastlane`、`README.md`、`LICENSE`；
   - 上游 `README.md`、`LICENSE`、`fastlane`、`.gitlab-ci.yml`、`.idea`、`.gradle`、`build` 不作为 fork 主文件；
   - 合并 `.gitignore` 规则，忽略 `local.properties`、`build/`、`.gradle/`、`*.jks` 等。

5. **合并资源覆盖**
   - 把 `res/drawable/*` 复制到 `app/src/main/res/drawable/`；
   - 确认覆盖后的图标、启动图、通知图标生效；
   - 删除根目录 `res/` 和 `build.sh` 中的 `cp -R` 步骤。

6. **固化包名/版本号/文案**
   - 直接在 `app/build.gradle.kts` 写入 `applicationId`、`versionCode`、`versionName`；
   - 直接把平台列表、`app_name`、源码/官网链接写入资源或 `Constants.kt`；
   - 删除 `build.sh` 中对应的 `sed` 步骤；
   - 可选：把版本号放到 `gradle.properties`，避免每次手改。

7. **保留签名**
   - 保留 `app/build.gradle.kts` 从 `local.properties` 读取签名的逻辑；
   - 保留 `keys/` 在仓库外；
   - 构建前由脚本或手动生成 `local.properties`，不要提交。

8. **简化构建脚本**
   - 删除 `git am`；
   - 删除 `git reset/clean`；
   - 删除 `cp res`；
   - 删除 `sed` 修改（如果第 6 步已固化）；
   - 保留一个轻量 `build.sh` 仅负责生成 `local.properties` 和调用 Gradle，或直接使用 Gradle 命令。

9. **更新 CI**
   - `.github/workflows/apk.yml` 去掉 `submodules: recursive`；
   - 去掉 Patch 步骤；
   - `cd src` 改为根目录；
   - 产物路径从 `src/app/build/...` 改为 `app/build/...`；
   - 签名仍通过 secrets 生成 `local.properties` 或 `upload.jks`；
   - 确认 CI 的 JDK 21、Gradle cache 仍然可用。

10. **更新 README 和 AGENTS**
    - README 删除 submodule/patches 说明，改为普通 Gradle 构建；
    - AGENTS 删除 submodule reset、`git am`、`generate.sh` 相关说明；
    - 保留签名、密钥位置、Release 构建命令、设备验证说明。

11. **验证**
    - 干净克隆后不需要 `--recursive` 即可构建；
    - `git submodule status` 为空，`.gitmodules` 不存在；
    - Debug/Release 都能构建；
    - Release 签名证书 SHA-256 与旧版本一致；
    - 安装后检查主题、小键盘、全屏鼠标、内嵌鼠标、Direct Input、HID 按键发送；
    - 检查仓库中没有 `local.properties`、`.jks`、密码等敏感文件。

### 4.3 方案对比

| 方案 | 优点 | 缺点 | 建议 |
| --- | --- | --- | --- |
| 直接拍平到根目录 | 最标准；Android Studio 直接打开；路径最短 | 一次性移动文件多；CI/README 要改 | 推荐 |
| 保留 `src/` 普通目录 | 改动小；路径和 CI 变化少 | 仍有一个非标准源码子目录 | 可作为过渡 |
| 继续 submodule + patches | 上游跟踪方便 | 依赖上游、构建复杂、易冲突 | 不再使用 |

### 4.4 上游更新策略

重构后不再让构建依赖上游仓库。需要跟进上游时，采用手动方式：

1. 下载/克隆上游指定版本；
2. 与当前 fork 做目录对比；
3. 只把需要的上游改动合并进来；
4. 重新构建并验证定制功能。

如果希望保留 upstream 历史，可以在重构时以某个上游 tag 为基线，把 8 个定制补丁作为普通提交重放，再合并到主分支。这样以后可以用 `git merge` 跟进上游，但历史整理成本更高。

### 4.5 风险与注意事项

- **GPLv3**：上游使用 GPLv3，分发 APK 时必须保留 LICENSE 并提供对应源码；不要把上游版权/许可证删掉。
- **不要提交密钥**：`keys/`、`local.properties`、`*.jks` 保持在仓库外或被 gitignore。
- **report ID**：`0001` 改过 HID report ID，重构时确认发送端与 descriptor 一致。
- **主题判断**：当前用 `background == Color.White` 判断水墨屏；如果以后新增其他纯白主题，需要改成显式主题标识。
- **小键盘位置**：位置是 `rememberSaveable`，进程重启后可能不保留；如果需要持久化，应写入设置存储。
- **中文文案**：`0002` 只补了英文，重构时可统一补齐中英文。
- **CI 路径**：当前 workflow 大量引用 `src/`，拍平后必须同步修改，否则 release 产物路径会错。
- **构建脚本**：`build.sh` 是 Bash 脚本，Windows 下用 Git Bash 运行；重构后可以保留，但不应再依赖 `git am`。

## 5. 重构前文件索引（历史）

### 历史补丁归档

- `docs/archive/patches/0001-support-pc.patch`
- `docs/archive/patches/0002-direct-input.patch`
- `docs/archive/patches/0003-read-keys.patch`
- `docs/archive/patches/0004-ink-theme-numpad.patch`
- `docs/archive/patches/0005-numpad-position-adjust.patch`
- `docs/archive/patches/0006-mouse-dialog.patch`
- `docs/archive/patches/0007-mouse-ink-topbar.patch`
- `docs/archive/patches/0008-inline-mouse-mode.patch`

### 核心源码（重构前路径）

- `src/app/src/main/java/com/atharok/btremote/common/utils/Constants.kt`
- `src/app/src/main/java/com/atharok/btremote/data/bluetooth/BluetoothHidCore.kt`
- `src/app/src/main/java/com/atharok/btremote/domain/entities/settings/ThemeEntity.kt`
- `src/app/src/main/java/com/atharok/btremote/domain/entities/remoteInput/keyboard/KeyboardKey.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/theme/Color.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/theme/Theme.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/components/Card.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/components/DialogInkStyleToggle.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/components/DialogSystemBarAppearance.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/components/customButtons/TextButton.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/screens/RemoteScreen.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/views/keyboard/AndroidKeyMapper.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/views/keyboard/VirtualKeyboard.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/views/keyboard/NumpadDialog.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/views/mouse/MouseDialog.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/views/mouse/MousePad.kt`
- `src/app/src/main/java/com/atharok/btremote/ui/views/mouse/MousePadLayout.kt`
- `src/app/src/main/res/values/strings.xml`
- `src/app/src/main/res/values-zh-rCN/strings.xml`

### 构建与发布（重构前）

- `build.sh`（已删除）
- `generate.sh`（已删除）
- `.github/workflows/apk.yml`（已更新）
- `README.md`（已更新）
- `keys/btremote-release.jks`（仓库外）
- `keys/keystore.env`（仓库外）
- `keys/README.md`（仓库外）

## 6. 重构后文件索引

### 根目录

- `app/`
- `gradle/`
- `gradlew`
- `gradlew.bat`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `.github/workflows/apk.yml`
- `.gitignore`
- `README.md`
- `LICENSE`
- `docs/fork-customizations.md`
- `docs/archive/patches/`

### 应用源码

- `app/src/main/java/com/atharok/btremote/`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-zh-rCN/strings.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/drawable/ic_splash_screen.xml`
- `app/src/main/res/drawable/notification_icon.xml`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`

### 本地签名（不提交）

- `local.properties`（已被 gitignore）
- `D:\A_notebooks\A_Github\android-bt-remote-ink\keys\btremote-release.jks`
- `D:\A_notebooks\A_Github\android-bt-remote-ink\keys\keystore.env`

### 构建产物

- `app/build/outputs/apk/default/debug/app-default-debug.apk`
- `app/build/outputs/apk/default/release/app-default-release.apk`
