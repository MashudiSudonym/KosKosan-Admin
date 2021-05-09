package c.m.koskosanadmin

import android.app.Application
import androidx.multidex.BuildConfig
import c.m.koskosanadmin.di.repositoryModule
import c.m.koskosanadmin.di.viewModelModule
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

class KosKosanAdminApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            FirebaseFirestore.setLoggingEnabled(true)
            Timber.plant(DebugTree())
        }

        startKoin {
            androidLogger()
            androidContext(this@KosKosanAdminApplication)
            modules(listOf(viewModelModule, repositoryModule))
        }
    }
}