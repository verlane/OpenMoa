package pe.aioo.openmoa.hardware

sealed interface ShortcutAction {
    object Pass : ShortcutAction
    object ConsumeToggleLanguage : ShortcutAction
}
