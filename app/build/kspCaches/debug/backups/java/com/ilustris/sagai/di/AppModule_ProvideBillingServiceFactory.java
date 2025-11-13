package com.ilustris.sagai.di;

import android.content.Context;
import com.ilustris.sagai.core.services.BillingService;
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
public final class AppModule_ProvideBillingServiceFactory implements Factory<BillingService> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideBillingServiceFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BillingService get() {
    return provideBillingService(contextProvider.get());
  }

  public static AppModule_ProvideBillingServiceFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideBillingServiceFactory(contextProvider);
  }

  public static BillingService provideBillingService(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBillingService(context));
  }
}
