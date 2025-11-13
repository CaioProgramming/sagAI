package com.ilustris.sagai.features.characters.presentation;

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
public final class CharacterViewModel_Factory implements Factory<CharacterViewModel> {
  private final Provider<CharacterUseCase> characterUseCaseProvider;

  private final Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider;

  public CharacterViewModel_Factory(Provider<CharacterUseCase> characterUseCaseProvider,
      Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider) {
    this.characterUseCaseProvider = characterUseCaseProvider;
    this.sagaHistoryUseCaseProvider = sagaHistoryUseCaseProvider;
  }

  @Override
  public CharacterViewModel get() {
    return newInstance(characterUseCaseProvider.get(), sagaHistoryUseCaseProvider.get());
  }

  public static CharacterViewModel_Factory create(
      Provider<CharacterUseCase> characterUseCaseProvider,
      Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider) {
    return new CharacterViewModel_Factory(characterUseCaseProvider, sagaHistoryUseCaseProvider);
  }

  public static CharacterViewModel newInstance(CharacterUseCase characterUseCase,
      SagaHistoryUseCase sagaHistoryUseCase) {
    return new CharacterViewModel(characterUseCase, sagaHistoryUseCase);
  }
}
