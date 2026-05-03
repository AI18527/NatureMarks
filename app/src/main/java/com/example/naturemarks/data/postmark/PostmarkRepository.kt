package com.example.naturemarks.data.postmark

import androidx.core.net.toUri
import com.example.naturemarks.data.memory.MemoryRepository
import com.example.naturemarks.database.AppDatabase
import com.example.naturemarks.ui.model.PostmarkUiModel
import com.example.naturemarks.ui.screens.markdetails.model.MarkDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface PostmarkRepositoryInterface {
    suspend fun getAllMarks(): List<PostmarkUiModel>?
    suspend fun getMarkById(imageId: String): PostmarkUiModel
    suspend fun addMark(mark: PostmarkUiModel)
    fun getMarkFromQrCode(rawData: String): PostmarkUiModel?
    suspend fun getMarkDetails(markId: String): MarkDetails
    fun addNewMemoryForMark(markId: String)
    suspend fun updateMarkMemoryPhoto(memoryId: Int, photoPath: String)
    suspend fun updateMarkMemoryNotes(memoryId: Int, notes: String)
}
class PostmarkRepository (
    private val db: AppDatabase,
    private val memoryRepository: MemoryRepository
): PostmarkRepositoryInterface {
    val mapper = PostmarkMapper
    override suspend fun getAllMarks(): List<PostmarkUiModel>? =
        withContext(Dispatchers.IO) {
            db.postmarkDao().getAll()?.map{ mapper.mapToMarkModel(it) }
        }

    override suspend fun getMarkById(imageId: String): PostmarkUiModel =
        withContext(Dispatchers.IO) {
            mapper.mapToMarkModel(db.postmarkDao().getMarkById(imageId))
        }
    override suspend fun addMark(mark: PostmarkUiModel) {
        val markRecord = mapper.mapToMarkEntry(mark)
        getAllMarks()?.let {
            if (it.any { it.imageId == markRecord.imageId }) {
                throw IllegalArgumentException("Postmark with imageId ${markRecord.imageId} already exists")
            }
            else {
                db.postmarkDao().insert(markRecord)
            }
        }
    }

    override fun getMarkFromQrCode(rawData: String): PostmarkUiModel? =
        try {
            val mark = Json.decodeFromString<PostmarkModel>(rawData)
            mapper.mapToMarkModel(mark)
        } catch (e: Exception) {
            null

    }

    override suspend fun getMarkDetails(markId: String): MarkDetails {
        val mark = getMarkById(markId)
        val memory = memoryRepository.getMemoryByMarkId(markId)

        return MarkDetails(
            postmark = mark,
            memoryId = memory.id,
            notes = memory.notes,
            photoUri = memory.photoPath.takeIf { !it.isNullOrBlank() }?.toUri()
        )
    }

    override fun addNewMemoryForMark(markId: String) {
        memoryRepository.addMemory(markId)
    }

    override suspend fun updateMarkMemoryPhoto(memoryId: Int, photoPath: String) {
        memoryRepository.updateMemoryPhoto(memoryId, photoPath)
    }

    override suspend fun updateMarkMemoryNotes(memoryId: Int, notes: String) {
        memoryRepository.updateMemoryNotes(memoryId, notes)
    }
}