package com.ilustris.sagai.core.file.backup.ui;

import com.ilustris.sagai.core.file.BackupService;
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
public final class BackupViewModel_Factory implements Factory<BackupViewModel> {
  private final Provider<BackupService> backupServiceProvider;

  private final Provider<SagaBackupService> sagaBackupServiceProvider;

  private final Provider<SagaRepository> sagaRepositoryProvider;

  public BackupViewModel_Factory(Provider<BackupService> backupServiceProvider,
      Provider<SagaBackupService> sagaBackupServiceProvider,
      Provider<SagaRepository> sagaRepositoryProvider) {
    this.backupServiceProvider = backupServiceProvider;
    this.sagaBackupServiceProvider = sagaBackupServiceProvider;
    this.sagaRepositoryProvider = sagaRepositoryProvider;
  }

  @Override
  public BackupViewModel get() {
    return newInstance(backupServiceProvider.get(), sagaBackupServiceProvider.get(), sagaRepositoryProvider.get());
  }

  public static BackupViewModel_Factory create(Provider<BackupService> backupServiceProvider,
      Provider<SagaBackupService> sagaBackupServiceProvider,
      Provider<SagaRepository> sagaRepositoryProvider) {
    return new BackupViewModel_Factory(backupServiceProvider, sagaBackupServiceProvider, sagaRepositoryProvider);
  }

  public static BackupViewModel newInstance(BackupService backupService,
      SagaBackupService sagaBackupService, SagaRepository sagaRepository) {
    return new BackupViewModel(backupService, sagaBackupService, sagaRepository);
  }
}
