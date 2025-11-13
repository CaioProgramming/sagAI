package com.ilustris.sagai.di;

import com.ilustris.sagai.core.ai.TextGenClient;
import com.ilustris.sagai.core.services.RemoteConfigService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvidesTextGenClientFactory implements Factory<TextGenClient> {
  private final Provider<RemoteConfigService> remoteConfigServiceProvider;

  public AppModule_ProvidesTextGenClientFactory(
      Provider<RemoteConfigService> remoteConfigServiceProvider) {
    this.remoteConfigServiceProvider = remoteConfigServiceProvider;
  }

  @Override
  public TextGenClient get() {
    return providesTextGenClient(remoteConfigServiceProvider.get());
  }

  public static AppModule_ProvidesTextGenClientFactory create(
      Provider<RemoteConfigService> remoteConfigServiceProvider) {
    return new AppModule_ProvidesTextGenClientFactory(remoteConfigServiceProvider);
  }

  public static TextGenClient providesTextGenClient(RemoteConfigService remoteConfigService) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providesTextGenClient(remoteConfigService));
  }
}
