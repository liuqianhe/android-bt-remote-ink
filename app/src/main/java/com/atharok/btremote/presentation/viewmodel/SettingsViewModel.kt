package com.atharok.btremote.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atharok.btremote.common.utils.AppIconManager
import com.atharok.btremote.domain.entities.RemoteNavigationEntity
import com.atharok.btremote.domain.entities.remoteInput.PhysicalVolumeButtonAction
import com.atharok.btremote.domain.entities.remoteInput.keyboard.KeyboardLanguage
import com.atharok.btremote.domain.entities.settings.AppearanceSettings
import com.atharok.btremote.domain.entities.settings.AppIconStyle
import com.atharok.btremote.domain.entities.settings.TopBarButton
import com.atharok.btremote.domain.entities.settings.RemoteSettings
import com.atharok.btremote.domain.entities.settings.ThemeEntity
import com.atharok.btremote.domain.usecases.SettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val useCase: SettingsUseCase,
    private val context: Context
): ViewModel() {

    // ---- Appearance ----

    val appearanceSettingsFlow: Flow<AppearanceSettings> = useCase.appearanceSettingsFlow

    fun changeTheme(newTheme: ThemeEntity) = viewModelScope.launch {
        useCase.saveTheme(newTheme)
    }

    fun setUseDynamicColors(useDynamicColors: Boolean) = viewModelScope.launch {
        useCase.saveUseDynamicColors(useDynamicColors)
    }

    fun setUseBlackColorForDarkTheme(useBlackColorForDarkTheme: Boolean) = viewModelScope.launch {
        useCase.saveUseBlackColorForDarkTheme(useBlackColorForDarkTheme)
    }

    fun saveUseFullScreen(useFullScreen: Boolean) = viewModelScope.launch {
        useCase.saveUseFullScreen(useFullScreen)
    }

    // ---- Remote ----

    val remoteSettingsFlow: Flow<RemoteSettings> = useCase.remoteSettingsFlow

    fun saveMouseSpeed(mouseSpeed: Float) = viewModelScope.launch {
        useCase.saveMouseSpeed(mouseSpeed)
    }

    fun saveMouseScrollSpeed(scrollSpeed: Float) = viewModelScope.launch {
        useCase.saveMouseScrollSpeed(scrollSpeed)
    }

    fun saveInvertMouseScrollingDirection(invertScrollingDirection: Boolean) = viewModelScope.launch {
        useCase.saveInvertMouseScrollingDirection(invertScrollingDirection)
    }

    fun saveUseGyroscope(useGyroscope: Boolean) = viewModelScope.launch {
        useCase.saveUseGyroscope(useGyroscope)
    }

    fun changeKeyboardLanguage(language: KeyboardLanguage) = viewModelScope.launch {
        useCase.saveKeyboardLanguage(language)
    }

    fun saveMustClearInputField(mustClearInputField: Boolean) = viewModelScope.launch {
        useCase.saveMustClearInputField(mustClearInputField)
    }

    fun saveUseAdvancedKeyboard(useAdvancedKeyboard: Boolean) = viewModelScope.launch {
        useCase.saveUseAdvancedKeyboard(useAdvancedKeyboard)
    }

    fun saveUseAdvancedKeyboardIntegrated(useAdvancedKeyboardIntegrated: Boolean) = viewModelScope.launch {
        useCase.saveUseAdvancedKeyboardIntegrated(useAdvancedKeyboardIntegrated)
    }

    fun saveUseMinimalistRemote(useMinimalistRemote: Boolean) = viewModelScope.launch {
        useCase.saveUseMinimalistRemote(useMinimalistRemote)
    }

    fun saveRemoteNavigation(remoteNavigationEntity: RemoteNavigationEntity) = viewModelScope.launch {
        useCase.saveRemoteNavigation(remoteNavigationEntity)
    }

    fun saveUseEnterForSelection(useEnterForSelection: Boolean) = viewModelScope.launch {
        useCase.saveUseEnterForSelection(useEnterForSelection)
    }

    fun saveUseMouseNavigationByDefault(useMouseNavigationByDefault: Boolean) = viewModelScope.launch {
        useCase.saveUseMouseNavigationByDefault(useMouseNavigationByDefault)
    }

    fun saveShowNavigationToggleInTopBar(showNavigationToggleInTopBar: Boolean) = viewModelScope.launch {
        useCase.saveShowNavigationToggleInTopBar(showNavigationToggleInTopBar)
    }

    fun saveTopBarButtonsOrder(order: List<TopBarButton>) = viewModelScope.launch {
        useCase.saveTopBarButtonsOrder(order)
    }

    fun saveAppIconStyle(appIconStyle: AppIconStyle) = viewModelScope.launch {
        // 先落库再切图标：切换 alias 会让部分 ROM 立刻杀掉进程，顺序反了设置会丢
        useCase.saveAppIconStyle(appIconStyle)
        AppIconManager.apply(context, appIconStyle)
    }

    fun savePhysicalVolumeUpButtonAction(physicalVolumeButtonAction: PhysicalVolumeButtonAction) = viewModelScope.launch {
        useCase.savePhysicalVolumeUpButtonAction(physicalVolumeButtonAction)
    }

    fun savePhysicalVolumeDownButtonAction(physicalVolumeButtonAction: PhysicalVolumeButtonAction) = viewModelScope.launch {
        useCase.savePhysicalVolumeDownButtonAction(physicalVolumeButtonAction)
    }

    // ---- Others ----

    val showRemoteButtonsInNotificationFlow: Flow<Boolean> get() = useCase.showRemoteButtonsInNotification()
    fun saveShowRemoteButtonsInNotification(show: Boolean) = viewModelScope.launch {
        useCase.saveShowRemoteButtonsInNotification(show)
    }

    val hideBluetoothActivationButtonFlow: Flow<Boolean> get() = useCase.hideBluetoothActivationButton()
    fun saveHideBluetoothActivationButton(hide: Boolean) = viewModelScope.launch {
        useCase.saveHideBluetoothActivationButton(hide)
    }
}