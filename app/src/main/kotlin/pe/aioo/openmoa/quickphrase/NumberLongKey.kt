package pe.aioo.openmoa.quickphrase

enum class NumberLongKey(
    val digit: String,
    override val defaultPhrase: String,
    override val prefKey: String,
) : PhraseKey {
    NUM_1("1", "!", "number_long_key_1"),
    NUM_2("2", "@", "number_long_key_2"),
    NUM_3("3", "#", "number_long_key_3"),
    NUM_4("4", "$", "number_long_key_4"),
    NUM_5("5", "%", "number_long_key_5"),
    NUM_6("6", "^", "number_long_key_6"),
    NUM_7("7", "&", "number_long_key_7"),
    NUM_8("8", "*", "number_long_key_8"),
    NUM_9("9", "(", "number_long_key_9"),
    NUM_0("0", ")", "number_long_key_0");

    override val displayName get() = digit

    companion object {
        fun fromDigit(digit: String): NumberLongKey? = values().find { it.digit == digit }
    }
}
