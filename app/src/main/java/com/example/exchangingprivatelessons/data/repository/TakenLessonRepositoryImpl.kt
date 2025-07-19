package com.example.exchangingprivatelessons.data.repository

import com.example.exchangingprivatelessons.common.di.IoDispatcher
import com.example.exchangingprivatelessons.data.local.dao.TakenLessonDao
import com.example.exchangingprivatelessons.data.local.entity.TakenLessonEntity
import com.example.exchangingprivatelessons.data.mapper.TakenLessonMapper
import com.example.exchangingprivatelessons.data.remote.cloud.FunctionsDataSource
import com.example.exchangingprivatelessons.data.remote.dto.TakenLessonDto
import com.example.exchangingprivatelessons.data.remote.firestore.FirestoreDataSource
import com.example.exchangingprivatelessons.data.repository.base.NetworkCacheRepository
import com.example.exchangingprivatelessons.domain.model.TakenLesson
import com.example.exchangingprivatelessons.domain.repository.TakenLessonRepository
import kotlinx.coroutines.CoroutineDispatcher

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton



/* ────────────────────  TakenLessonRepositoryImpl  ─────────────────────── */
@Singleton
class TakenLessonRepositoryImpl @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val function: FunctionsDataSource,
    private val dao: TakenLessonDao,
    private val mapper: TakenLessonMapper,
    @IoDispatcher
    private val io: CoroutineDispatcher
) : NetworkCacheRepository<TakenLessonEntity, TakenLessonDto, TakenLesson>(
    io
), TakenLessonRepository {

    override fun queryLocal()                  = dao.observeAll()
    override suspend fun fetchRemote()         = firestore.getTakenLessons()
    override suspend fun saveRemote(remote: List<TakenLessonDto>) {
        val entities = remote.map(mapper::dtoToEntity)   // ← שם‑הפונקציה ייחודי
        dao.upsertAll(entities)
    }

    override fun map(local: TakenLessonEntity) = mapper.toDomain(local)

    override fun observeTakenLessons() = observe()
}
