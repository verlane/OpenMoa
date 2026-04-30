package pe.aioo.openmoa.hardware

enum class CursorDirection { LEFT, RIGHT, UP, DOWN }

sealed interface VimAction {
    data class MoveCursor(val direction: CursorDirection, val extend: Boolean = false) : VimAction
    data class MoveWord(val forward: Boolean, val extend: Boolean = false) : VimAction
    data class LineHome(val extend: Boolean = false) : VimAction
    data class LineEnd(val extend: Boolean = false) : VimAction
    data class PageScroll(val down: Boolean, val extend: Boolean = false) : VimAction
    data class DocumentEdge(val end: Boolean, val extend: Boolean = false) : VimAction
    data object DeleteWordBack : VimAction
    data object DeleteBack : VimAction
    data object DeleteChar : VimAction
    data object DeleteLine : VimAction
    data object DeleteSelection : VimAction
    data object VisualEnter : VimAction
    data object CollapseSelection : VimAction
    data object Yank : VimAction
    data object YankLine : VimAction
    data object YankSelection : VimAction
    data object Paste : VimAction
    data object NewLineBelow : VimAction
    data object NewLineAbove : VimAction
    data object Undo : VimAction
    data object Redo : VimAction
    data object InjectTab : VimAction
    data object Consume : VimAction
    data object PassThrough : VimAction
}
