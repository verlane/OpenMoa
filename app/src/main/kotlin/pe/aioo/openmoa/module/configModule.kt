package pe.aioo.openmoa.module

import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.aioo.openmoa.config.Config
import pe.aioo.openmoa.suggestion.Dictionary
import pe.aioo.openmoa.suggestion.KoreanSuggestionEngine
import pe.aioo.openmoa.suggestion.KoreanTrieDictionary
import pe.aioo.openmoa.suggestion.SharedPreferencesUserWordStore
import pe.aioo.openmoa.suggestion.SuggestionEngine
import pe.aioo.openmoa.suggestion.TrieDictionary
import pe.aioo.openmoa.suggestion.UserWordStore
import pe.aioo.openmoa.view.feedback.KeyFeedbackPlayer

val configModule = module {
    single { Config(androidContext()) }
    single { KeyFeedbackPlayer(androidContext()) }
    single<Dictionary>(named("en")) { TrieDictionary(androidContext()) }
    single<Dictionary>(named("ko")) { KoreanTrieDictionary(androidContext()) }
    single<UserWordStore>(named("en")) {
        SharedPreferencesUserWordStore(androidContext(), SharedPreferencesUserWordStore.Language.EN)
    }
    single<UserWordStore>(named("ko")) {
        SharedPreferencesUserWordStore(androidContext(), SharedPreferencesUserWordStore.Language.KO)
    }
    single { SuggestionEngine(get(named("en")), get(named("en"))) }
    single { KoreanSuggestionEngine(get(named("ko")), get(named("ko"))) }
}
