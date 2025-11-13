package com.ilustris.sagai.core.services;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class BillingService_Factory implements Factory<BillingService> {
  private final Provider<Context> contextProvider;

  public BillingService_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BillingService get() {
    return newInstance(contextProvider.get());
  }

  public static BillingService_Factory create(Provider<Context> contextProvider) {
    return new BillingService_Factory(contextProvider);
  }

  public static BillingService newInstance(Context context) {
    return new BillingService(context);
  }
}
