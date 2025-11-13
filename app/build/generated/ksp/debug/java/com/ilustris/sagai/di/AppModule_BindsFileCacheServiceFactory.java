package com.ilustris.sagai.di;

import android.content.Context;
import com.ilustris.sagai.core.file.FileCacheService;
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
public final class AppModule_BindsFileCacheServiceFactory implements Factory<FileCacheService> {
  private final Provider<Context> contextProvider;

  public AppModule_BindsFileCacheServiceFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FileCacheService get() {
    return bindsFileCacheService(contextProvider.get());
  }

  public static AppModule_BindsFileCacheServiceFactory create(Provider<Context> contextProvider) {
    return new AppModule_BindsFileCacheServiceFactory(contextProvider);
  }

  public static FileCacheService bindsFileCacheService(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.bindsFileCacheService(context));
  }
}
