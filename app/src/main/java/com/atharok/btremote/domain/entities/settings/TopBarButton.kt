package com.atharok.btremote.domain.entities.settings

/**
 * 遥控页顶部工具栏里可调整先后顺序的功能键。
 *
 * 顺序保存在 [RemoteSettings.topBarButtonsOrder] 中；“更多”按钮不参与排序，始终固定在最右侧。
 */
enum class TopBarButton {
    NAVIGATION_TOGGLE,
    NUMPAD,
    MOUSE,
    KEYBOARD;

    companion object {

        /** 默认顺序：数字键、全屏鼠标、键盘输入、方向键/鼠标 */
        val DEFAULT_ORDER: List<TopBarButton> = listOf(NUMPAD, MOUSE, KEYBOARD, NAVIGATION_TOGGLE)

        /**
         * 解析 DataStore 中保存的顺序字符串（逗号分隔的枚举名）。
         * 非法或缺失的项会自动按 [DEFAULT_ORDER] 补齐，保证返回值总是包含全部按钮。
         */
        fun parseOrder(rawValue: String?): List<TopBarButton> {
            if (rawValue.isNullOrBlank()) return DEFAULT_ORDER

            val parsed = rawValue
                .split(',')
                .mapNotNull { name -> entries.firstOrNull { it.name == name.trim() } }
                .distinct()

            if (parsed.isEmpty()) return DEFAULT_ORDER

            return parsed + DEFAULT_ORDER.filterNot { it in parsed }
        }

        /** 序列化为可保存的字符串。 */
        fun serializeOrder(order: List<TopBarButton>): String = order.joinToString(",") { it.name }
    }
}
