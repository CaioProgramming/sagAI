package com.ilustris.sagai.features.characters.data.source;

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
public final class CharacterDaoImpl_Factory implements Factory<CharacterDaoImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public CharacterDaoImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CharacterDaoImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static CharacterDaoImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new CharacterDaoImpl_Factory(databaseProvider);
  }

  public static CharacterDaoImpl newInstance(SagaDatabase database) {
    return new CharacterDaoImpl(database);
  }
}
