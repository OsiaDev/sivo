package com.coljuegos.sivo.di

import android.content.Context
import androidx.room.Room
import com.coljuegos.sivo.data.dao.DepartamentoDao
import com.coljuegos.sivo.data.dao.MunicipioDao
import com.coljuegos.sivo.data.database.SivoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): SivoDatabase {
        return Room.databaseBuilder(
            context, SivoDatabase::class.java, "sivo_database"
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideDepartamentoDao(database: SivoDatabase): DepartamentoDao = database.departamentoDao()

    @Provides
    fun provideMunicipioDao(database: SivoDatabase): MunicipioDao = database.municipioDao()

}