package pe.aioo.openmoa.view.preview

import android.view.View
import pe.aioo.openmoa.config.KeyboardSkin

class KeyPreviewController(private val enabled: Boolean, private val skin: KeyboardSkin = KeyboardSkin.DEFAULT) {

    private var popup: KeyPreviewPopup? = null

    fun show(anchor: View, text: String) {
        if (!enabled) return
        val p = popup ?: KeyPreviewPopup(anchor.context, skin).also { popup = it }
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
