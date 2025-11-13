package com.ilustris.sagai.features.newsaga.ui.presentation;

import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase;
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase;
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
public final class CreateSagaViewModel_Factory implements Factory<CreateSagaViewModel> {
  private final Provider<NewSagaUseCase> newSagaUseCaseProvider;

  private final Provider<CharacterUseCase> characterUseCaseProvider;

  public CreateSagaViewModel_Factory(Provider<NewSagaUseCase> newSagaUseCaseProvider,
      Provider<CharacterUseCase> characterUseCaseProvider) {
    this.newSagaUseCaseProvider = newSagaUseCaseProvider;
    this.characterUseCaseProvider = characterUseCaseProvider;
  }

  @Override
  public CreateSagaViewModel get() {
    return newInstance(newSagaUseCaseProvider.get(), characterUseCaseProvider.get());
  }

  public static CreateSagaViewModel_Factory create(Provider<NewSagaUseCase> newSagaUseCaseProvider,
      Provider<CharacterUseCase> characterUseCaseProvider) {
    return new CreateSagaViewModel_Factory(newSagaUseCaseProvider, characterUseCaseProvider);
  }

  public static CreateSagaViewModel newInstance(NewSagaUseCase newSagaUseCase,
      CharacterUseCase characterUseCase) {
    return new CreateSagaViewModel(newSagaUseCase, characterUseCase);
  }
}
