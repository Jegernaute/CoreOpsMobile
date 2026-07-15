package com.example.coreops.data.repository

import com.example.coreops.data.remote.api.ProjectsApi
import com.example.coreops.data.remote.models.ProjectDto
import com.example.coreops.domain.repository.ProjectRepository
import javax.inject.Inject
import com.example.coreops.data.remote.models.PaginatedResponse
/**
 * Фізична реалізація репозиторію, яка ходить у мережу за задачами.
 */
class ProjectRepositoryImpl @Inject constructor(
    private val api: ProjectsApi
) : ProjectRepository {

    override suspend fun getProjects(
        cursor: String?,
        search: String?,
        ordering: String?,
        showArchived: Boolean?,
        status: String?,
        hasActiveTasks: Boolean?,
        isCompleted: Boolean?
    ): Result<PaginatedResponse<ProjectDto>> {
        return try {
            // Робить запит до API та передає параметри пошуку і пагінації
            val response = api.getProjects(
                cursor = cursor,
                search = search,
                ordering = ordering,
                showArchived = showArchived,
                status = status,
                hasActiveTasks = hasActiveTasks,
                isCompleted = isCompleted
            )
            // Якщо все ок повертає об'єкт
            Result.success(response)
        } catch (e: Exception) {
            // Якщо сталася помилка (немає інтернету, 404, 500 тощо), повертає Result.failure
            Result.failure(e)
        }
    }
    override suspend fun getProjectById(id: Int): Result<ProjectDto> {
        return try {
            val response = api.getProjectById(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}