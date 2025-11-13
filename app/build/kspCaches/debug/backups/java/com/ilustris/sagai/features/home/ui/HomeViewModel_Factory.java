package com.ilustris.sagai.features.home.ui;

import com.ilustris.sagai.core.file.BackupService;
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<HomeUseCase> homeUseCaseProvider;

  private final Provider<BackupService> backupServiceProvider;

  public HomeViewModel_Factory(Provider<HomeUseCase> homeUseCaseProvider,
      Provider<BackupService> backupServiceProvider) {
    this.homeUseCaseProvider = homeUseCaseProvider;
    this.backupServiceProvider = backupServiceProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(homeUseCaseProvider.get(), backupServiceProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<HomeUseCase> homeUseCaseProvider,
      Provider<BackupService> backupServiceProvider) {
    return new HomeViewModel_Factory(homeUseCaseProvider, backupServiceProvider);
  }

  public static HomeViewModel newInstance(HomeUseCase homeUseCase, BackupService backupService) {
    return new HomeViewModel(homeUseCase, backupService);
  }
}
