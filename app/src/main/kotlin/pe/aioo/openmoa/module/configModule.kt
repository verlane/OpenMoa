package pe.aioo.openmoa.module

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import pe.aioo.openmoa.config.Config
import pe.aioo.openmoa.view.feedback.KeyFeedbackPlayer

val configModule = module {
    single { Config(androidContext()) }
    single { KeyFeedbackPlayer(androidContext()) }
}
