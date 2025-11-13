package com.ilustris.sagai.features.characters.events.data.repository;

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
public final class CharacterEventRepositoryImpl_Factory implements Factory<CharacterEventRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public CharacterEventRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CharacterEventRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static CharacterEventRepositoryImpl_Factory create(
      Provider<SagaDatabase> databaseProvider) {
    return new CharacterEventRepositoryImpl_Factory(databaseProvider);
  }

  public static CharacterEventRepositoryImpl newInstance(SagaDatabase database) {
    return new CharacterEventRepositoryImpl(database);
  }
}
