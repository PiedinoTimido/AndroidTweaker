package com.android.tweaker.model

enum class TweakCategoryType(val title: String, val description: String) {
    INFO("Information", "Read-only system & device diagnostics"),
    APP_CONTROL("App Control", "Package management & permission controls"),
    SETTINGS("Settings", "UI customization & system behavior tweaks"),
    DANGER("Danger-Tweaks", "Advanced options - Risk of bootloop or malfunction")
}

sealed class InputType {
    object None : InputType()
    data class SingleText(val label: String, val placeholder: String, val defaultValue: String = "") : InputType()
    data class TwoText(
        val label1: String, val placeholder1: String,
        val label2: String, val placeholder2: String
    ) : InputType()
    data class Options(val label: String, val options: List<String>, val defaultIndex: Int = 0) : InputType()
}

data class TweakItem(
    val id: String,
    val category: TweakCategoryType,
    val title: String,
    val description: String,
    val commandTemplate: String,
    val inputType: InputType = InputType.None,
    val isDanger: Boolean = false,
    val dangerDescription: String = ""
)
