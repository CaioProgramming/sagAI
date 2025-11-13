package com.ilustris.sagai.features.act.data.source;

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
public final class ActDaoImpl_Factory implements Factory<ActDaoImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public ActDaoImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ActDaoImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static ActDaoImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new ActDaoImpl_Factory(databaseProvider);
  }

  public static ActDaoImpl newInstance(SagaDatabase database) {
    return new ActDaoImpl(database);
  }
}
