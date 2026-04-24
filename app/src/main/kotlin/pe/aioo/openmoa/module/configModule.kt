package pe.aioo.openmoa.module

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import pe.aioo.openmoa.config.Config
import pe.aioo.openmoa.suggestion.Dictionary
import pe.aioo.openmoa.suggestion.SharedPreferencesUserWordStore
import pe.aioo.openmoa.suggestion.SuggestionEngine
import pe.aioo.openmoa.suggestion.TrieDictionary
import pe.aioo.openmoa.suggestion.UserWordStore
import pe.aioo.openmoa.view.feedback.KeyFeedbackPlayer

val configModule = module {
    single { Config(androidContext()) }
    single { KeyFeedbackPlayer(androidContext()) }
    single<Dictionary> { TrieDictionary(androidContext()) }
    single<UserWordStore> { SharedPreferencesUserWordStore(androidContext()) }
    single { SuggestionEngine(get(), get()) }
}
