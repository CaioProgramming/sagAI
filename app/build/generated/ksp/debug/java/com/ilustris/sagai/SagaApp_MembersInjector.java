package com.ilustris.sagai;

import com.ilustris.sagai.core.services.BillingService;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class SagaApp_MembersInjector implements MembersInjector<SagaApp> {
  private final Provider<BillingService> billingServiceProvider;

  public SagaApp_MembersInjector(Provider<BillingService> billingServiceProvider) {
    this.billingServiceProvider = billingServiceProvider;
  }

  public static MembersInjector<SagaApp> create(Provider<BillingService> billingServiceProvider) {
    return new SagaApp_MembersInjector(billingServiceProvider);
  }

  @Override
  public void injectMembers(SagaApp instance) {
    injectBillingService(instance, billingServiceProvider.get());
  }

  @InjectedFieldSignature("com.ilustris.sagai.SagaApp.billingService")
  public static void injectBillingService(SagaApp instance, BillingService billingService) {
    instance.billingService = billingService;
  }
}
