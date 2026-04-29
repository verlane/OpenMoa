package pe.aioo.openmoa.floating

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingSnapCalculatorTest {

    @Test
    fun `중앙 좌측에 있으면 좌측 가장자리로 스냅`() {
        val result = FloatingSnapCalculator.snap(
            screenWidth = 1000,
            viewWidth = 100,
            currentX = 200,
            edgeMargin = 16,
        )
        assertEquals(16, result)
    }

    @Test
    fun `중앙 우측에 있으면 우측 가장자리로 스냅`() {
        val result = FloatingSnapCalculator.snap(
            screenWidth = 1000,
            viewWidth = 100,
            currentX = 600,
            edgeMargin = 16,
        )
        assertEquals(1000 - 100 - 16, result)
    }

    @Test
    fun `정확히 중앙이면 우측으로 스냅`() {
        val result = FloatingSnapCalculator.snap(
            screenWidth = 1000,
            viewWidth = 100,
            currentX = 450,
            edgeMargin = 16,
        )
        assertEquals(1000 - 100 - 16, result)
    }

    @Test
    fun `세로 좌표는 화면 범위 안으로 clamp`() {
        assertEquals(
            16,
            FloatingSnapCalculator.clampY(
                screenHeight = 2000,
                viewHeight = 100,
                currentY = -50,
                topMargin = 16,
                bottomMargin = 16,
            ),
        )
        assertEquals(
            2000 - 100 - 16,
            FloatingSnapCalculator.clampY(
                screenHeight = 2000,
                viewHeight = 100,
                currentY = 5000,
                topMargin = 16,
                bottomMargin = 16,
            ),
        )
        assertEquals(
            500,
            FloatingSnapCalculator.clampY(
                screenHeight = 2000,
                viewHeight = 100,
                currentY = 500,
                topMargin = 16,
                bottomMargin = 16,
            ),
        )
    }

    @Test
    fun `기본 위치는 우측 중앙 부근`() {
        val (x, y) = FloatingSnapCalculator.defaultPosition(
            screenWidth = 1000,
            screenHeight = 2000,
            viewWidth = 100,
            viewHeight = 100,
            edgeMargin = 16,
        )
        assertEquals(1000 - 100 - 16, x)
        assertEquals((2000 - 100) / 2, y)
    }
}
