package com.ilustris.sagai.features.characters.ui;

import com.ilustris.sagai.core.services.BillingService;
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase;
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase;
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
public final class CharacterDetailsViewModel_Factory implements Factory<CharacterDetailsViewModel> {
  private final Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider;

  private final Provider<CharacterUseCase> characterUseCaseProvider;

  private final Provider<BillingService> billingServiceProvider;

  public CharacterDetailsViewModel_Factory(Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider,
      Provider<CharacterUseCase> characterUseCaseProvider,
      Provider<BillingService> billingServiceProvider) {
    this.sagaHistoryUseCaseProvider = sagaHistoryUseCaseProvider;
    this.characterUseCaseProvider = characterUseCaseProvider;
    this.billingServiceProvider = billingServiceProvider;
  }

  @Override
  public CharacterDetailsViewModel get() {
    return newInstance(sagaHistoryUseCaseProvider.get(), characterUseCaseProvider.get(), billingServiceProvider.get());
  }

  public static CharacterDetailsViewModel_Factory create(
      Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider,
      Provider<CharacterUseCase> characterUseCaseProvider,
      Provider<BillingService> billingServiceProvider) {
    return new CharacterDetailsViewModel_Factory(sagaHistoryUseCaseProvider, characterUseCaseProvider, billingServiceProvider);
  }

  public static CharacterDetailsViewModel newInstance(SagaHistoryUseCase sagaHistoryUseCase,
      CharacterUseCase characterUseCase, BillingService billingService) {
    return new CharacterDetailsViewModel(sagaHistoryUseCase, characterUseCase, billingService);
  }
}
