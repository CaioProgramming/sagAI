package com.ilustris.sagai.features.wiki.data.repository;

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
public final class WikiRepositoryImpl_Factory implements Factory<WikiRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public WikiRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public WikiRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static WikiRepositoryImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new WikiRepositoryImpl_Factory(databaseProvider);
  }

  public static WikiRepositoryImpl newInstance(SagaDatabase database) {
    return new WikiRepositoryImpl(database);
  }
}
