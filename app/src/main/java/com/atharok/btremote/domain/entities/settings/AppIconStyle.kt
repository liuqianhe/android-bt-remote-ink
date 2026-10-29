package com.atharok.btremote.domain.entities.settings

/**
 * 应用桌面图标的可选样式。
 *
 * 每个样式对应 Manifest 里的一个 activity-alias，切换时通过 PackageManager
 * 启用目标 alias、禁用其余 alias，桌面图标即随之改变。
 *
 * 注意：[aliasClassName] 必须是 **完整类名**，不能拿 `context.packageName` 去拼。
 * 本项目的 `namespace`（com.atharok.btremote）与 `applicationId`
 * （io.github.jqssun.btremote，flavor 还带后缀）并不相同，用包名拼接会得到
 * 一个不存在的组件名，setComponentEnabledSetting 会静默失败。
 */
enum class AppIconStyle(
    val aliasClassName: String,
    /** 与 Manifest 中 activity-alias 的 android:enabled 声明保持一致 */
    val enabledByDefault: Boolean
) {
    // 默认图标（深色底）
    DEFAULT(
        aliasClassName = "com.atharok.btremote.presentation.activities.MainActivityLauncherDefault",
        enabledByDefault = true
    ),

    // 水墨屏图标（白底黑字）
    E_INK(
        aliasClassName = "com.atharok.btremote.presentation.activities.MainActivityLauncherEInk",
        enabledByDefault = false
    );

    companion object {
        val DEFAULT_STYLE: AppIconStyle = DEFAULT

        fun parse(value: String?): AppIconStyle =
            try {
                valueOf(value ?: DEFAULT_STYLE.name)
            } catch (_: IllegalArgumentException) {
                DEFAULT_STYLE
            }
    }
}
