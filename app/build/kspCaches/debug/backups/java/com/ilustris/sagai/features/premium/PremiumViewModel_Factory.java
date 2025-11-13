package com.ilustris.sagai.features.premium;

import com.ilustris.sagai.core.services.BillingService;
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
public final class PremiumViewModel_Factory implements Factory<PremiumViewModel> {
  private final Provider<BillingService> billingServiceProvider;

  public PremiumViewModel_Factory(Provider<BillingService> billingServiceProvider) {
    this.billingServiceProvider = billingServiceProvider;
  }

  @Override
  public PremiumViewModel get() {
    return newInstance(billingServiceProvider.get());
  }

  public static PremiumViewModel_Factory create(Provider<BillingService> billingServiceProvider) {
    return new PremiumViewModel_Factory(billingServiceProvider);
  }

  public static PremiumViewModel newInstance(BillingService billingService) {
    return new PremiumViewModel(billingService);
  }
}
