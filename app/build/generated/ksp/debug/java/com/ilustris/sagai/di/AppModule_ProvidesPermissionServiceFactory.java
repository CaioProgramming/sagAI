package com.ilustris.sagai.di;

import android.content.Context;
import com.ilustris.sagai.core.permissions.PermissionService;
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
public final class AppModule_ProvidesPermissionServiceFactory implements Factory<PermissionService> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvidesPermissionServiceFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PermissionService get() {
    return providesPermissionService(contextProvider.get());
  }

  public static AppModule_ProvidesPermissionServiceFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvidesPermissionServiceFactory(contextProvider);
  }

  public static PermissionService providesPermissionService(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providesPermissionService(context));
  }
}
