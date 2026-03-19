package com.example.coreops.data.repository

import com.example.coreops.data.remote.api.ProjectsApi
import com.example.coreops.data.remote.models.ProjectDto
import com.example.coreops.domain.repository.ProjectRepository
import javax.inject.Inject

/**
 * Реалізація репозиторію. Саме тут відбувається фізичний похід у мережу.
 */
class ProjectRepositoryImpl @Inject constructor(
    private val api: ProjectsApi
) : ProjectRepository {

    override suspend fun getProjects(): Result<List<ProjectDto>> {
        return try {
            // Робимо запит до API
            val response = api.getProjects()
            // Якщо все ок, загортаємо список проєктів (results) в Result.success
            Result.success(response.results)
        } catch (e: Exception) {
            // Якщо сталася помилка (немає інтернету, 404, 500 тощо), повертаємо Result.failure
            Result.failure(e)
        }
    }
}