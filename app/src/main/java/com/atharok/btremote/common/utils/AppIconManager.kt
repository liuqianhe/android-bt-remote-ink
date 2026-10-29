package com.atharok.btremote.common.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.atharok.btremote.domain.entities.settings.AppIconStyle

/**
 * 应用桌面图标切换工具。
 *
 * 原理：Manifest 中为桌面入口配置了多个 activity-alias（每个图标样式一个），
 * 同一时刻只启用其中一个，系统桌面读到哪个 alias 就显示哪个图标。
 * 启用状态由系统持久化，无需自己存储。
 *
 * 实现要点：
 * 1. 组件名必须用 alias 的 **完整类名**，不能用 `context.packageName` 拼接——
 *    本项目 namespace（com.atharok.btremote）与 applicationId
 *    （io.github.jqssun.btremote）不同，拼出来的组件不存在，切换会静默失败。
 * 2. 顺序必须是「先启用目标 → 再禁用其余」，否则中途应用会从桌面消失。
 * 3. 切换后要校验；万一目标没生效就回滚到默认图标，避免桌面入口全丢。
 */
object AppIconManager {

    private const val TAG = "AppIconManager"

    /**
     * 把桌面图标切换为 [style]。
     *
     * @return true 表示目标图标已生效（旧入口也已禁用）；false 表示切换被系统拒绝。
     */
    fun apply(context: Context, style: AppIconStyle): Boolean {
        val packageManager = context.packageManager

        val target = componentName(context, style)
        val others = AppIconStyle.entries
            .filter { it != style }
            .map { it to componentName(context, it) }

        // 状态已经与目标一致时直接返回，避免每次启动都触发桌面刷新
        val isConsistent = isEnabled(packageManager, target, style.enabledByDefault) == true &&
                others.all { (other, component) ->
                    isEnabled(packageManager, component, other.enabledByDefault) == false
                }
        if (isConsistent) return true

        // 1) 先启用目标入口
        setEnabled(packageManager, target, true)

        // 2) 再禁用其它入口
        others.forEach { (_, component) -> setEnabled(packageManager, component, false) }

        // 3) 校验：目标没启用成功就回滚默认图标，保证桌面至少有入口
        if (isEnabled(packageManager, target, style.enabledByDefault) != true) {
            Log.w(TAG, "切换图标失败（$style），回滚到默认图标")
            setEnabled(packageManager, componentName(context, AppIconStyle.DEFAULT), true)
            return false
        }

        // 4) 校验：个别 ROM 会把 DISABLED 改写回 enabled，补一次
        others.forEach { (other, component) ->
            if (isEnabled(packageManager, component, other.enabledByDefault) == true) {
                setEnabled(packageManager, component, false)
            }
        }

        val allOthersDisabled = others.all { (other, component) ->
            isEnabled(packageManager, component, other.enabledByDefault) == false
        }
        if (!allOthersDisabled) {
            Log.w(TAG, "部分旧图标入口未能禁用（可能受 ROM 限制），桌面可能短暂出现多个图标")
        }

        Log.i(TAG, "图标已切换为 $style（旧入口是否已隐藏: $allOthersDisabled）")
        return allOthersDisabled
    }

    /**
     * 按设置里的样式同步一次桌面图标，供启动时调用。
     */
    fun sync(context: Context, style: AppIconStyle): Boolean = apply(context, style)

    private fun componentName(context: Context, style: AppIconStyle) =
        ComponentName(context.packageName, style.aliasClassName)

    private fun setEnabled(
        packageManager: PackageManager,
        component: ComponentName,
        enabled: Boolean
    ) {
        runCatching {
            packageManager.setComponentEnabledSetting(
                component,
                if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }.onFailure { throwable ->
            Log.e(TAG, "设置组件状态失败: ${component.flattenToShortString()} enabled=$enabled", throwable)
        }
    }

    /**
     * 读取组件的实际启用状态。
     *
     * `getComponentEnabledSetting` 在未被显式设置时返回 DEFAULT，
     * 此时以 Manifest 声明（[AppIconStyle.enabledByDefault]）为准。
     */
    private fun isEnabled(
        packageManager: PackageManager,
        component: ComponentName,
        manifestDefault: Boolean
    ): Boolean? = try {
        when (packageManager.getComponentEnabledSetting(component)) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> manifestDefault
            else -> null
        }
    } catch (throwable: Exception) {
        Log.e(TAG, "读取组件状态失败: ${component.flattenToShortString()}", throwable)
        null
    }
}
