package pe.aioo.openmoa.view.preview

import android.view.View

class KeyPreviewController(private val enabled: Boolean) {

    private var popup: KeyPreviewPopup? = null

    fun show(anchor: View, text: String) {
        if (!enabled) return
        val p = popup ?: KeyPreviewPopup(anchor.context).also { popup = it }
        p.show(anchor, text)
    }

    fun update(anchor: View, text: String) {
        if (!enabled) return
        popup?.update(anchor, text)
    }

    fun hide() {
        popup?.hide()
    }

}
