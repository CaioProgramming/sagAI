package com.ilustris.sagai.di;

import android.content.Context;
import com.ilustris.sagai.core.datastore.DataStorePreferences;
import com.ilustris.sagai.core.file.BackupService;
import com.ilustris.sagai.core.file.FileHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvidesBackupServiceFactory implements Factory<BackupService> {
  private final Provider<Context> contextProvider;

  private final Provider<DataStorePreferences> preferencesProvider;

  private final Provider<FileHelper> fileHelperProvider;

  public AppModule_ProvidesBackupServiceFactory(Provider<Context> contextProvider,
      Provider<DataStorePreferences> preferencesProvider, Provider<FileHelper> fileHelperProvider) {
    this.contextProvider = contextProvider;
    this.preferencesProvider = preferencesProvider;
    this.fileHelperProvider = fileHelperProvider;
  }

  @Override
  public BackupService get() {
    return providesBackupService(contextProvider.get(), preferencesProvider.get(), fileHelperProvider.get());
  }

  public static AppModule_ProvidesBackupServiceFactory create(Provider<Context> contextProvider,
      Provider<DataStorePreferences> preferencesProvider, Provider<FileHelper> fileHelperProvider) {
    return new AppModule_ProvidesBackupServiceFactory(contextProvider, preferencesProvider, fileHelperProvider);
  }

  public static BackupService providesBackupService(Context context,
      DataStorePreferences preferences, FileHelper fileHelper) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providesBackupService(context, preferences, fileHelper));
  }
}
