package com.ilustris.sagai.core.lifecycle;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AppLifecycleManagerImpl_Factory implements Factory<AppLifecycleManagerImpl> {
  @Override
  public AppLifecycleManagerImpl get() {
    return newInstance();
  }

  public static AppLifecycleManagerImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AppLifecycleManagerImpl newInstance() {
    return new AppLifecycleManagerImpl();
  }

  private static final class InstanceHolder {
    static final AppLifecycleManagerImpl_Factory INSTANCE = new AppLifecycleManagerImpl_Factory();
  }
}
