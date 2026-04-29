package pe.aioo.openmoa.floating

object FloatingSnapCalculator {

    fun snap(screenWidth: Int, viewWidth: Int, currentX: Int, edgeMargin: Int): Int {
        val viewCenterX = currentX + viewWidth / 2
        val screenCenterX = screenWidth / 2
        return if (viewCenterX < screenCenterX) {
            edgeMargin
        } else {
            screenWidth - viewWidth - edgeMargin
        }
    }

    fun clampY(
        screenHeight: Int,
        viewHeight: Int,
        currentY: Int,
        topMargin: Int,
        bottomMargin: Int,
    ): Int {
        val minY = topMargin
        val maxY = (screenHeight - viewHeight - bottomMargin).coerceAtLeast(minY)
        return currentY.coerceIn(minY, maxY)
    }

    fun defaultPosition(
        screenWidth: Int,
        screenHeight: Int,
        viewWidth: Int,
        viewHeight: Int,
        edgeMargin: Int,
    ): Pair<Int, Int> {
        val x = screenWidth - viewWidth - edgeMargin
        val y = (screenHeight - viewHeight) / 2
        return x to y
    }
}
