package pe.aioo.openmoa.settings

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.aioo.openmoa.clipboard.ClipboardEntry
import pe.aioo.openmoa.hotstring.HotstringRule
import pe.aioo.openmoa.quickphrase.QuickPhraseKey
import pe.aioo.openmoa.quickphrase.QwertyLongKey

class SettingsImportParseTest {

    private val allowedKeys: Set<String> = buildSet {
        addAll(SettingsPreferences.ALL_KEYS)
        QuickPhraseKey.values().forEach { add(it.prefKey) }
        QwertyLongKey.values().forEach { add(it.prefKey) }
    }

    private val booleanKeys = setOf(
        SettingsPreferences.KEY_KEY_PREVIEW,
        SettingsPreferences.KEY_AUTO_SPACE_PERIOD,
        SettingsPreferences.KEY_AUTO_CAPITALIZE_ENGLISH,
        SettingsPreferences.KEY_HOTSTRING_ENABLED,
        SettingsPreferences.KEY_WORD_SUGGESTION_ENABLED,
        SettingsPreferences.KEY_KOREAN_WORD_SUGGESTION_ENABLED,
        SettingsPreferences.KEY_CLIPBOARD_ENABLED,
        SettingsPreferences.KEY_LANDSCAPE_QWERTY,
        SettingsPreferences.KEY_FLOATING_INDICATOR_ENABLED,
        SettingsPreferences.KEY_OVERLAY_PERMISSION_NOTIFIED,
    )

    // export 로직과 동일한 방식으로 테스트용 JSON 생성
    private fun buildExportJson(): JSONObject {
        val settingsObj = JSONObject().apply {
            // boolean 항목 (export 시 JSON boolean 타입으로 저장됨)
            put(SettingsPreferences.KEY_KEY_PREVIEW, true)
            put(SettingsPreferences.KEY_HOTSTRING_ENABLED, false)
            put(SettingsPreferences.KEY_WORD_SUGGESTION_ENABLED, true)
            put(SettingsPreferences.KEY_CLIPBOARD_ENABLED, true)
            put(SettingsPreferences.KEY_AUTO_SPACE_PERIOD, false)
            put(SettingsPreferences.KEY_AUTO_CAPITALIZE_ENGLISH, true)
            put(SettingsPreferences.KEY_KOREAN_WORD_SUGGESTION_ENABLED, true)
            put(SettingsPreferences.KEY_LANDSCAPE_QWERTY, false)
            put(SettingsPreferences.KEY_FLOATING_INDICATOR_ENABLED, true)
            put(SettingsPreferences.KEY_OVERLAY_PERMISSION_NOTIFIED, true)
            // string 항목
            put(SettingsPreferences.KEY_HANGUL_INPUT_MODE, "MOAKEY_PLUS")
            put(SettingsPreferences.KEY_KEYBOARD_SKIN, "DARK_GRAY")
            put(SettingsPreferences.KEY_KEYPAD_HEIGHT, "LOW")
            put(SettingsPreferences.KEY_LONG_PRESS_TIME, "MS_300")
            put(SettingsPreferences.KEY_ONE_HAND_MODE, "NONE")
        }

        val hotstringsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "hs-1")
                put("trigger", "ㅎㄹ")
                put("expansion", "안녕하세요")
                put("enabled", true)
            })
            put(JSONObject().apply {
                put("id", "hs-2")
                put("trigger", "gd")
                put("expansion", "감사합니다")
                put("enabled", false)
            })
        }

        val clipboardArray = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "cb-1")
                put("text", "클립보드 항목 1")
                put("pinned", false)
                put("createdAt", 1700000000000L)
            })
            put(JSONObject().apply {
                put("id", "cb-2")
                put("text", "pinned item")
                put("pinned", true)
                put("createdAt", 1700000001000L)
            })
        }

        val learnedWords = JSONObject().apply {
            put("ko", JSONObject().apply {
                put("안녕", 5)
                put("감사", 3)
            })
            put("en", JSONObject().apply {
                put("hello", 2)
                put("world", 4)
            })
        }

        val blacklist = JSONObject().apply {
            put("ko", JSONArray().apply { put("나쁜말") })
            put("en", JSONArray())
        }

        return JSONObject().apply {
            put("version", 3)
            put("exportedAt", System.currentTimeMillis())
            put("settings", settingsObj)
            put("hotstrings", hotstringsArray)
            put("clipboard", clipboardArray)
            put("learnedWords", learnedWords)
            put("blacklist", blacklist)
        }
    }

    @Test
    fun `version 파싱`() {
        val root = buildExportJson()
        val version = root.optInt("version")
        assertTrue("version은 2 또는 3이어야 함, 실제: $version", version == 2 || version == 3)
    }

    @Test
    fun `settings 섹션 파싱 - boolean과 string 타입 구분`() {
        val root = buildExportJson()
        val settingsObj = root.optJSONObject("settings")
        assertNotNull("settings 섹션 없음", settingsObj)

        val settingsEdits = mutableListOf<Pair<String, Any>>()
        settingsObj!!.keys().forEach { key ->
            if (key !in allowedKeys) return@forEach
            if (key in booleanKeys) {
                settingsEdits.add(key to settingsObj.optBoolean(key))
            } else {
                val value = settingsObj.optString(key)
                if (value.isNotEmpty()) settingsEdits.add(key to value)
            }
        }

        val boolEdits = settingsEdits.filter { it.second is Boolean }
        val strEdits = settingsEdits.filter { it.second is String }
        println("Boolean: ${boolEdits.size}개, String: ${strEdits.size}개")
        assertTrue("boolean 항목이 하나 이상 있어야 함", boolEdits.isNotEmpty())
        assertTrue("string 항목이 하나 이상 있어야 함", strEdits.isNotEmpty())

        // boolean인데 booleanKeys에 없는 키 탐지
        val wrongTypeKeys = mutableListOf<String>()
        settingsObj.keys().forEach { key ->
            if (key !in allowedKeys) return@forEach
            if (key !in booleanKeys && settingsObj.opt(key) is Boolean) {
                wrongTypeKeys.add(key)
            }
        }
        assertTrue("boolean 타입 오분류 키 발견: $wrongTypeKeys", wrongTypeKeys.isEmpty())
    }

    @Test
    fun `settings 값 정확성 검증`() {
        val root = buildExportJson()
        val settingsObj = root.optJSONObject("settings")!!

        val edits = mutableMapOf<String, Any>()
        settingsObj.keys().forEach { key ->
            if (key !in allowedKeys) return@forEach
            if (key in booleanKeys) {
                edits[key] = settingsObj.optBoolean(key)
            } else {
                val value = settingsObj.optString(key)
                if (value.isNotEmpty()) edits[key] = value
            }
        }

        assertEquals(true, edits[SettingsPreferences.KEY_KEY_PREVIEW])
        assertEquals(false, edits[SettingsPreferences.KEY_HOTSTRING_ENABLED])
        assertEquals("MOAKEY_PLUS", edits[SettingsPreferences.KEY_HANGUL_INPUT_MODE])
        assertEquals("DARK_GRAY", edits[SettingsPreferences.KEY_KEYBOARD_SKIN])
    }

    @Test
    fun `hotstrings 파싱`() {
        val root = buildExportJson()
        val array = root.optJSONArray("hotstrings")
        assertNotNull("hotstrings 섹션 없음", array)

        val hotstrings = List(array!!.length()) { i ->
            val obj = array.getJSONObject(i)
            HotstringRule(
                id = obj.getString("id"),
                trigger = obj.getString("trigger"),
                expansion = obj.getString("expansion"),
                enabled = obj.optBoolean("enabled", true)
            )
        }
        assertEquals(2, hotstrings.size)
        assertEquals("hs-1", hotstrings[0].id)
        assertEquals("ㅎㄹ", hotstrings[0].trigger)
        assertEquals("안녕하세요", hotstrings[0].expansion)
        assertTrue(hotstrings[0].enabled)
        assertFalse(hotstrings[1].enabled)
    }

    @Test
    fun `clipboard 파싱`() {
        val root = buildExportJson()
        val array = root.optJSONArray("clipboard")
        assertNotNull("clipboard 섹션 없음", array)

        val entries = List(array!!.length()) { i ->
            val obj = array.getJSONObject(i)
            ClipboardEntry(
                id = obj.getString("id"),
                text = obj.getString("text"),
                pinned = obj.optBoolean("pinned", false),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
        assertEquals(2, entries.size)
        assertEquals("클립보드 항목 1", entries[0].text)
        assertFalse(entries[0].pinned)
        assertTrue(entries[1].pinned)
    }

    @Test
    fun `learnedWords 파싱`() {
        val root = buildExportJson()
        val learnedKo = parseLearnedData(
            root.optJSONObject("learnedWords")?.optJSONObject("ko"),
            root.optJSONObject("blacklist")?.optJSONArray("ko")
        )
        val learnedEn = parseLearnedData(
            root.optJSONObject("learnedWords")?.optJSONObject("en"),
            root.optJSONObject("blacklist")?.optJSONArray("en")
        )

        assertEquals(2, learnedKo.words.size)
        assertEquals(5, learnedKo.words["안녕"])
        assertEquals(1, learnedKo.blacklist.size)
        assertTrue(learnedKo.blacklist.contains("나쁜말"))
        assertEquals(2, learnedEn.words.size)
        assertEquals(0, learnedEn.blacklist.size)
    }

    @Test
    fun `전체 export-import 라운드트립`() {
        val jsonText = buildExportJson().toString(2)
        val root = JSONObject(jsonText)
        val version = root.optInt("version")
        assertTrue("Unsupported version: $version", version == 2 || version == 3)

        val settingsObj = root.optJSONObject("settings")
            ?: error("Invalid format: missing settings")

        val settingsEdits = mutableListOf<Pair<String, Any>>()
        settingsObj.keys().forEach { key ->
            if (key !in allowedKeys) return@forEach
            if (key in booleanKeys) {
                settingsEdits.add(key to settingsObj.optBoolean(key))
            } else {
                val value = settingsObj.optString(key)
                if (value.isNotEmpty()) settingsEdits.add(key to value)
            }
        }

        val hotstrings = root.optJSONArray("hotstrings")?.let { array ->
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                HotstringRule(
                    id = obj.getString("id"),
                    trigger = obj.getString("trigger"),
                    expansion = obj.getString("expansion"),
                    enabled = obj.optBoolean("enabled", true)
                )
            }
        }

        val clipboard = root.optJSONArray("clipboard")?.let { array ->
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                ClipboardEntry(
                    id = obj.getString("id"),
                    text = obj.getString("text"),
                    pinned = obj.optBoolean("pinned", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        }

        val learnedKo = parseLearnedData(
            root.optJSONObject("learnedWords")?.optJSONObject("ko"),
            root.optJSONObject("blacklist")?.optJSONArray("ko")
        )
        val learnedEn = parseLearnedData(
            root.optJSONObject("learnedWords")?.optJSONObject("en"),
            root.optJSONObject("blacklist")?.optJSONArray("en")
        )

        println("=== 라운드트립 성공 ===")
        println("  settings: ${settingsEdits.size}개")
        println("  hotstrings: ${hotstrings?.size}개")
        println("  clipboard: ${clipboard?.size}개")
        println("  learnedKo: ${learnedKo.words.size}개 / blacklist: ${learnedKo.blacklist.size}개")
        println("  learnedEn: ${learnedEn.words.size}개 / blacklist: ${learnedEn.blacklist.size}개")

        assertTrue(settingsEdits.isNotEmpty())
        assertEquals(2, hotstrings?.size)
        assertEquals(2, clipboard?.size)
        assertTrue(learnedKo.words.isNotEmpty())
    }

    private data class LearnedData(val words: Map<String, Int>, val blacklist: Set<String>)

    private fun parseLearnedData(wordsObj: JSONObject?, blacklistArr: JSONArray?): LearnedData {
        val words = mutableMapOf<String, Int>()
        wordsObj?.keys()?.forEach { key -> words[key] = wordsObj.getInt(key) }
        val bl = mutableSetOf<String>()
        if (blacklistArr != null) {
            for (i in 0 until blacklistArr.length()) bl.add(blacklistArr.getString(i))
        }
        return LearnedData(words, bl)
    }
}
