package com.ilustris.sagai.features.settings.domain;

import android.content.Context;
import com.ilustris.sagai.core.datastore.DataStorePreferences;
import com.ilustris.sagai.core.file.BackupService;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.core.file.FileManager;
import com.ilustris.sagai.core.permissions.PermissionService;
import com.ilustris.sagai.core.services.BillingService;
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SettingsUseCaseImpl_Factory implements Factory<SettingsUseCaseImpl> {
  private final Provider<DataStorePreferences> dataStorePreferencesProvider;

  private final Provider<Context> contextProvider;

  private final Provider<SagaRepository> sagaRepositoryProvider;

  private final Provider<BillingService> billingServiceProvider;

  private final Provider<FileHelper> fileHelperProvider;

  private final Provider<PermissionService> permissionServiceProvider;

  private final Provider<BackupService> backupServiceProvider;

  private final Provider<FileManager> fileManagerProvider;

  public SettingsUseCaseImpl_Factory(Provider<DataStorePreferences> dataStorePreferencesProvider,
      Provider<Context> contextProvider, Provider<SagaRepository> sagaRepositoryProvider,
      Provider<BillingService> billingServiceProvider, Provider<FileHelper> fileHelperProvider,
      Provider<PermissionService> permissionServiceProvider,
      Provider<BackupService> backupServiceProvider, Provider<FileManager> fileManagerProvider) {
    this.dataStorePreferencesProvider = dataStorePreferencesProvider;
    this.contextProvider = contextProvider;
    this.sagaRepositoryProvider = sagaRepositoryProvider;
    this.billingServiceProvider = billingServiceProvider;
    this.fileHelperProvider = fileHelperProvider;
    this.permissionServiceProvider = permissionServiceProvider;
    this.backupServiceProvider = backupServiceProvider;
    this.fileManagerProvider = fileManagerProvider;
  }

  @Override
  public SettingsUseCaseImpl get() {
    return newInstance(dataStorePreferencesProvider.get(), contextProvider.get(), sagaRepositoryProvider.get(), billingServiceProvider.get(), fileHelperProvider.get(), permissionServiceProvider.get(), backupServiceProvider.get(), fileManagerProvider.get());
  }

  public static SettingsUseCaseImpl_Factory create(
      Provider<DataStorePreferences> dataStorePreferencesProvider,
      Provider<Context> contextProvider, Provider<SagaRepository> sagaRepositoryProvider,
      Provider<BillingService> billingServiceProvider, Provider<FileHelper> fileHelperProvider,
      Provider<PermissionService> permissionServiceProvider,
      Provider<BackupService> backupServiceProvider, Provider<FileManager> fileManagerProvider) {
    return new SettingsUseCaseImpl_Factory(dataStorePreferencesProvider, contextProvider, sagaRepositoryProvider, billingServiceProvider, fileHelperProvider, permissionServiceProvider, backupServiceProvider, fileManagerProvider);
  }

  public static SettingsUseCaseImpl newInstance(DataStorePreferences dataStorePreferences,
      Context context, SagaRepository sagaRepository, BillingService billingService,
      FileHelper fileHelper, PermissionService permissionService, BackupService backupService,
      FileManager fileManager) {
    return new SettingsUseCaseImpl(dataStorePreferences, context, sagaRepository, billingService, fileHelper, permissionService, backupService, fileManager);
  }
}
