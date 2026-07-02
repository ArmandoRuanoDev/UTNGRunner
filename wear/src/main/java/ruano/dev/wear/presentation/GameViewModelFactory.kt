package ruano.dev.wear.presentation

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ruano.dev.wear.data.datasource.PreferencesDataSource
import ruano.dev.wear.data.health.HeartRateDataSource
import ruano.dev.wear.data.repository.ScoreRepositoryImpl
import ruano.dev.wear.domain.usecase.GetHighScoreUseCase
import ruano.dev.wear.domain.usecase.SaveHighScoreUseCase

class GameViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Construir el grafo de dependencias de forma explícita
        val healthClient = HealthServices.getClient(context)
        val heartRateDs  = HeartRateDataSource(healthClient)
        val prefsDs      = PreferencesDataSource(context)
        val repository   = ScoreRepositoryImpl(prefsDs)
        return GameViewModel(
            getHighScore  = GetHighScoreUseCase(repository),
            saveHighScore = SaveHighScoreUseCase(repository),
            heartRateSource = heartRateDs
        ) as T
    }
}
