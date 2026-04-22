package pe.aioo.openmoa.config

class GestureAngles(val values: IntArray) {

    companion object {
        val RIGHT_HAND = intArrayOf(23, 68, 119, 151, 203, 247, 293, 335)
        val LEFT_HAND = intArrayOf(28, 61, 112, 157, 205, 247, 293, 338)
        val BOTH_HANDS = intArrayOf(23, 68, 113, 157, 203, 247, 293, 338)
        val SIX_DIR = intArrayOf(23, 68, 131, 131, 203, 247, 310, 310)

        fun fromString(s: String?): GestureAngles {
            if (s.isNullOrEmpty()) return GestureAngles(RIGHT_HAND.copyOf())
            val parts = s.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (parts.size != 8) return GestureAngles(RIGHT_HAND.copyOf())
            val v = parts.toIntArray()
            val valid = v[0] >= 0 && v[3] <= 180 && v[4] >= 181 && v[7] <= 359 &&
                (0 until 7).all { v[it] <= v[it + 1] }
            return if (valid) GestureAngles(v) else GestureAngles(RIGHT_HAND.copyOf())
        }
    }

    operator fun get(i: Int): Int = values[i]

    fun toPrefsString(): String = values.joinToString(",")
}
