package com.ilustris.sagai.core.ai;

import com.ilustris.sagai.core.services.RemoteConfigService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GemmaClient_Factory implements Factory<GemmaClient> {
  private final Provider<RemoteConfigService> remoteConfigServiceProvider;

  public GemmaClient_Factory(Provider<RemoteConfigService> remoteConfigServiceProvider) {
    this.remoteConfigServiceProvider = remoteConfigServiceProvider;
  }

  @Override
  public GemmaClient get() {
    return newInstance(remoteConfigServiceProvider.get());
  }

  public static GemmaClient_Factory create(
      Provider<RemoteConfigService> remoteConfigServiceProvider) {
    return new GemmaClient_Factory(remoteConfigServiceProvider);
  }

  public static GemmaClient newInstance(RemoteConfigService remoteConfigService) {
    return new GemmaClient(remoteConfigService);
  }
}
