package com.ilustris.sagai.di;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
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
public final class AppModule_ProvideFirebaseRemoteConfigFactory implements Factory<FirebaseRemoteConfig> {
  @Override
  public FirebaseRemoteConfig get() {
    return provideFirebaseRemoteConfig();
  }

  public static AppModule_ProvideFirebaseRemoteConfigFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseRemoteConfig provideFirebaseRemoteConfig() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFirebaseRemoteConfig());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideFirebaseRemoteConfigFactory INSTANCE = new AppModule_ProvideFirebaseRemoteConfigFactory();
  }
}
