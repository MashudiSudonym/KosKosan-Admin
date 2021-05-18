package c.m.koskosanadmin.di

import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.ui.form.add.user.profile.AddUserProfileViewModel
import c.m.koskosanadmin.ui.main.MainViewModel
import c.m.koskosanadmin.ui.profile.ProfileViewModel
import c.m.koskosanadmin.ui.splash.SplashScreenViewModel
import c.m.koskosanadmin.ui.transaction.list.TransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { SplashScreenViewModel(get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { AddUserProfileViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { TransactionViewModel(get(), get()) }
}

val repositoryModule: Module = module {
    single { AuthRepository() }
    single { FirebaseRepository() }
}