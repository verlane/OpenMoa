package pe.aioo.openmoa.hardware

enum class CursorDirection { LEFT, RIGHT, UP, DOWN }

sealed interface VimAction {
    data class MoveCursor(val direction: CursorDirection, val extend: Boolean = false) : VimAction
    data class MoveWord(val forward: Boolean, val extend: Boolean = false) : VimAction
    data object LineHome : VimAction
    data object LineEnd : VimAction
    data object DeleteChar : VimAction
    data object DeleteLine : VimAction
    data object VisualEnter : VimAction
    data object CollapseSelection : VimAction
    data object DeleteSelection : VimAction
    data object Yank : VimAction
    data object InjectTab : VimAction
    data object Consume : VimAction
    data object PassThrough : VimAction
}
