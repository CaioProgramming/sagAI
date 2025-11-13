package com.ilustris.sagai.features.characters.repository;

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
public final class CharacterRepositoryImpl_Factory implements Factory<CharacterRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public CharacterRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CharacterRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static CharacterRepositoryImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new CharacterRepositoryImpl_Factory(databaseProvider);
  }

  public static CharacterRepositoryImpl newInstance(SagaDatabase database) {
    return new CharacterRepositoryImpl(database);
  }
}
