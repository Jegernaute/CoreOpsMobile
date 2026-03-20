package com.example.coreops.data.repository

import com.example.coreops.data.remote.api.ProjectsApi
import com.example.coreops.data.remote.models.ProjectDto
import com.example.coreops.domain.repository.ProjectRepository
import javax.inject.Inject

/**
 * Фізична реалізація репозиторію, яка ходить у мережу за задачами.
 */
class ProjectRepositoryImpl @Inject constructor(
    private val api: ProjectsApi
) : ProjectRepository {

    override suspend fun getProjects(): Result<List<ProjectDto>> {
        return try {
            // Робить запит до API
            val response = api.getProjects()
            // Якщо все ок, загортає список проєктів (results) в Result.success
            Result.success(response.results)
        } catch (e: Exception) {
            // Якщо сталася помилка (немає інтернету, 404, 500 тощо), повертає Result.failure
            Result.failure(e)
        }
    }
}