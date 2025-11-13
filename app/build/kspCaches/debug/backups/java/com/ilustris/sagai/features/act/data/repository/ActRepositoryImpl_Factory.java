package com.ilustris.sagai.features.act.data.repository;

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
public final class ActRepositoryImpl_Factory implements Factory<ActRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public ActRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ActRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static ActRepositoryImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new ActRepositoryImpl_Factory(databaseProvider);
  }

  public static ActRepositoryImpl newInstance(SagaDatabase database) {
    return new ActRepositoryImpl(database);
  }
}
