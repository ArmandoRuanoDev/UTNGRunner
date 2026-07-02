package ruano.dev.wear.domain.usecase

import ruano.dev.wear.domain.repository.ScoreRepository

/** Caso de uso: encapsula UNA operación de negocio */
class GetHighScoreUseCase(private val repository: ScoreRepository) {
    suspend operator fun invoke(): Int = repository.getHighScore()
}

class SaveHighScoreUseCase(private val repository: ScoreRepository) {
    suspend operator fun invoke(score: Int) {
        val current = repository.getHighScore()
        if (score > current) repository.saveHighScore(score)
    }
}
