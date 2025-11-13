package com.ilustris.sagai.features.characters.relations.data.source;

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
public final class CharacterRelationDaoImpl_Factory implements Factory<CharacterRelationDaoImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public CharacterRelationDaoImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CharacterRelationDaoImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static CharacterRelationDaoImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new CharacterRelationDaoImpl_Factory(databaseProvider);
  }

  public static CharacterRelationDaoImpl newInstance(SagaDatabase database) {
    return new CharacterRelationDaoImpl(database);
  }
}
