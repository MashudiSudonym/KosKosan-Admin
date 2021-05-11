package c.m.koskosanadmin.di

import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.ui.splash.SplashScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { SplashScreenViewModel(get()) }
}

val repositoryModule: Module = module {
    single { AuthRepository() }
    single { FirebaseRepository() }
}