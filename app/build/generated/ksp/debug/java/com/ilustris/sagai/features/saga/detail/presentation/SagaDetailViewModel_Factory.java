package com.ilustris.sagai.features.saga.detail.presentation;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.ilustris.sagai.core.services.BillingService;
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase;
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
public final class SagaDetailViewModel_Factory implements Factory<SagaDetailViewModel> {
  private final Provider<SagaDetailUseCase> sagaDetailUseCaseProvider;

  private final Provider<FirebaseRemoteConfig> remoteConfigProvider;

  private final Provider<BillingService> billingServiceProvider;

  public SagaDetailViewModel_Factory(Provider<SagaDetailUseCase> sagaDetailUseCaseProvider,
      Provider<FirebaseRemoteConfig> remoteConfigProvider,
      Provider<BillingService> billingServiceProvider) {
    this.sagaDetailUseCaseProvider = sagaDetailUseCaseProvider;
    this.remoteConfigProvider = remoteConfigProvider;
    this.billingServiceProvider = billingServiceProvider;
  }

  @Override
  public SagaDetailViewModel get() {
    return newInstance(sagaDetailUseCaseProvider.get(), remoteConfigProvider.get(), billingServiceProvider.get());
  }

  public static SagaDetailViewModel_Factory create(
      Provider<SagaDetailUseCase> sagaDetailUseCaseProvider,
      Provider<FirebaseRemoteConfig> remoteConfigProvider,
      Provider<BillingService> billingServiceProvider) {
    return new SagaDetailViewModel_Factory(sagaDetailUseCaseProvider, remoteConfigProvider, billingServiceProvider);
  }

  public static SagaDetailViewModel newInstance(SagaDetailUseCase sagaDetailUseCase,
      FirebaseRemoteConfig remoteConfig, BillingService billingService) {
    return new SagaDetailViewModel(sagaDetailUseCase, remoteConfig, billingService);
  }
}
