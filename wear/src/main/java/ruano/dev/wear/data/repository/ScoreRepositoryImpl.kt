package ruano.dev.wear.data.repository

import ruano.dev.wear.data.datasource.PreferencesDataSource
import ruano.dev.wear.domain.repository.ScoreRepository

/** Implementación concreta — la capa de datos implementa la interfaz del dominio */
class ScoreRepositoryImpl(
    private val dataSource: PreferencesDataSource
) : ScoreRepository {

    override suspend fun getHighScore(): Int =
        dataSource.getHighScore()

    override suspend fun saveHighScore(score: Int) =
        dataSource.saveHighScore(score)
}
