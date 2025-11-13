package com.ilustris.sagai.features.home.data.usecase;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.file.BackupService;
import com.ilustris.sagai.core.services.BillingService;
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService;
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository;
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
public final class HomeUseCaseImpl_Factory implements Factory<HomeUseCaseImpl> {
  private final Provider<SagaRepository> sagaRepositoryProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  private final Provider<BackupService> backupServiceProvider;

  private final Provider<SagaBackupService> sagaBackupServiceProvider;

  private final Provider<FirebaseRemoteConfig> remoteConfigProvider;

  private final Provider<BillingService> billingServiceProvider;

  public HomeUseCaseImpl_Factory(Provider<SagaRepository> sagaRepositoryProvider,
      Provider<GemmaClient> gemmaClientProvider, Provider<BackupService> backupServiceProvider,
      Provider<SagaBackupService> sagaBackupServiceProvider,
      Provider<FirebaseRemoteConfig> remoteConfigProvider,
      Provider<BillingService> billingServiceProvider) {
    this.sagaRepositoryProvider = sagaRepositoryProvider;
    this.gemmaClientProvider = gemmaClientProvider;
    this.backupServiceProvider = backupServiceProvider;
    this.sagaBackupServiceProvider = sagaBackupServiceProvider;
    this.remoteConfigProvider = remoteConfigProvider;
    this.billingServiceProvider = billingServiceProvider;
  }

  @Override
  public HomeUseCaseImpl get() {
    return newInstance(sagaRepositoryProvider.get(), gemmaClientProvider.get(), backupServiceProvider.get(), sagaBackupServiceProvider.get(), remoteConfigProvider.get(), billingServiceProvider.get());
  }

  public static HomeUseCaseImpl_Factory create(Provider<SagaRepository> sagaRepositoryProvider,
      Provider<GemmaClient> gemmaClientProvider, Provider<BackupService> backupServiceProvider,
      Provider<SagaBackupService> sagaBackupServiceProvider,
      Provider<FirebaseRemoteConfig> remoteConfigProvider,
      Provider<BillingService> billingServiceProvider) {
    return new HomeUseCaseImpl_Factory(sagaRepositoryProvider, gemmaClientProvider, backupServiceProvider, sagaBackupServiceProvider, remoteConfigProvider, billingServiceProvider);
  }

  public static HomeUseCaseImpl newInstance(SagaRepository sagaRepository, GemmaClient gemmaClient,
      BackupService backupService, SagaBackupService sagaBackupService,
      FirebaseRemoteConfig remoteConfig, BillingService billingService) {
    return new HomeUseCaseImpl(sagaRepository, gemmaClient, backupService, sagaBackupService, remoteConfig, billingService);
  }
}
