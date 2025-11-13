package com.ilustris.sagai.di;

import com.ilustris.sagai.core.services.RemoteConfigService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvidesRemoteConfigServiceFactory implements Factory<RemoteConfigService> {
  @Override
  public RemoteConfigService get() {
    return providesRemoteConfigService();
  }

  public static AppModule_ProvidesRemoteConfigServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RemoteConfigService providesRemoteConfigService() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providesRemoteConfigService());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvidesRemoteConfigServiceFactory INSTANCE = new AppModule_ProvidesRemoteConfigServiceFactory();
  }
}
