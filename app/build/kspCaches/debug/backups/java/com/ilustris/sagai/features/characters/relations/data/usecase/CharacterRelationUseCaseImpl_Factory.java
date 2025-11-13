package com.ilustris.sagai.features.characters.relations.data.usecase;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepository;
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
public final class CharacterRelationUseCaseImpl_Factory implements Factory<CharacterRelationUseCaseImpl> {
  private final Provider<GemmaClient> gemmaClientProvider;

  private final Provider<CharacterRelationRepository> relationRepositoryProvider;

  public CharacterRelationUseCaseImpl_Factory(Provider<GemmaClient> gemmaClientProvider,
      Provider<CharacterRelationRepository> relationRepositoryProvider) {
    this.gemmaClientProvider = gemmaClientProvider;
    this.relationRepositoryProvider = relationRepositoryProvider;
  }

  @Override
  public CharacterRelationUseCaseImpl get() {
    return newInstance(gemmaClientProvider.get(), relationRepositoryProvider.get());
  }

  public static CharacterRelationUseCaseImpl_Factory create(
      Provider<GemmaClient> gemmaClientProvider,
      Provider<CharacterRelationRepository> relationRepositoryProvider) {
    return new CharacterRelationUseCaseImpl_Factory(gemmaClientProvider, relationRepositoryProvider);
  }

  public static CharacterRelationUseCaseImpl newInstance(GemmaClient gemmaClient,
      CharacterRelationRepository relationRepository) {
    return new CharacterRelationUseCaseImpl(gemmaClient, relationRepository);
  }
}
