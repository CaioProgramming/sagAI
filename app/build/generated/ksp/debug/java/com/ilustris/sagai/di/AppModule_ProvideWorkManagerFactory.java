package com.ilustris.sagai.di;

import android.content.Context;
import androidx.work.WorkManager;
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
public final class AppModule_ProvideWorkManagerFactory implements Factory<WorkManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideWorkManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WorkManager get() {
    return provideWorkManager(contextProvider.get());
  }

  public static AppModule_ProvideWorkManagerFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideWorkManagerFactory(contextProvider);
  }

  public static WorkManager provideWorkManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWorkManager(context));
  }
}
