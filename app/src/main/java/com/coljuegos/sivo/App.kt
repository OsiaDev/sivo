package com.coljuegos.sivo

import android.app.Application
import com.coljuegos.sivo.utils.DataLoaderService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var dataLoaderService: DataLoaderService

    override fun onCreate() {
        super.onCreate()

        // Cargar datos iniciales en background
        CoroutineScope(Dispatchers.IO).launch {
            dataLoaderService.loadLocationData()
        }
    }

}