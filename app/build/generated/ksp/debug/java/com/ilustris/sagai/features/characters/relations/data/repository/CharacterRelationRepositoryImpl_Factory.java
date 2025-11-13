package com.ilustris.sagai.features.characters.relations.data.repository;

import com.ilustris.sagai.core.database.SagaDatabase;
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
public final class CharacterRelationRepositoryImpl_Factory implements Factory<CharacterRelationRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public CharacterRelationRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CharacterRelationRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static CharacterRelationRepositoryImpl_Factory create(
      Provider<SagaDatabase> databaseProvider) {
    return new CharacterRelationRepositoryImpl_Factory(databaseProvider);
  }

  public static CharacterRelationRepositoryImpl newInstance(SagaDatabase database) {
    return new CharacterRelationRepositoryImpl(database);
  }
}
