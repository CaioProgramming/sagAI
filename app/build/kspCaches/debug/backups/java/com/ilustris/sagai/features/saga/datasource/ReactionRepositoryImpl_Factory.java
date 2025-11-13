package com.ilustris.sagai.features.saga.datasource;

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
public final class ReactionRepositoryImpl_Factory implements Factory<ReactionRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public ReactionRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ReactionRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static ReactionRepositoryImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new ReactionRepositoryImpl_Factory(databaseProvider);
  }

  public static ReactionRepositoryImpl newInstance(SagaDatabase database) {
    return new ReactionRepositoryImpl(database);
  }
}
