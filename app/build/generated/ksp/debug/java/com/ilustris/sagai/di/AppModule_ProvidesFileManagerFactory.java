package com.ilustris.sagai.di;

import android.content.Context;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.core.file.FileManager;
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
public final class AppModule_ProvidesFileManagerFactory implements Factory<FileManager> {
  private final Provider<Context> contextProvider;

  private final Provider<FileHelper> fileHelperProvider;

  public AppModule_ProvidesFileManagerFactory(Provider<Context> contextProvider,
      Provider<FileHelper> fileHelperProvider) {
    this.contextProvider = contextProvider;
    this.fileHelperProvider = fileHelperProvider;
  }

  @Override
  public FileManager get() {
    return providesFileManager(contextProvider.get(), fileHelperProvider.get());
  }

  public static AppModule_ProvidesFileManagerFactory create(Provider<Context> contextProvider,
      Provider<FileHelper> fileHelperProvider) {
    return new AppModule_ProvidesFileManagerFactory(contextProvider, fileHelperProvider);
  }

  public static FileManager providesFileManager(Context context, FileHelper fileHelper) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providesFileManager(context, fileHelper));
  }
}
