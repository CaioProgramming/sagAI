package com.ilustris.sagai.features.saga.datasource;

import com.ilustris.sagai.core.database.DatabaseBuilder;
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
public final class SagaDaoImpl_Factory implements Factory<SagaDaoImpl> {
  private final Provider<DatabaseBuilder> databaseBuilderProvider;

  public SagaDaoImpl_Factory(Provider<DatabaseBuilder> databaseBuilderProvider) {
    this.databaseBuilderProvider = databaseBuilderProvider;
  }

  @Override
  public SagaDaoImpl get() {
    return newInstance(databaseBuilderProvider.get());
  }

  public static SagaDaoImpl_Factory create(Provider<DatabaseBuilder> databaseBuilderProvider) {
    return new SagaDaoImpl_Factory(databaseBuilderProvider);
  }

  public static SagaDaoImpl newInstance(DatabaseBuilder databaseBuilder) {
    return new SagaDaoImpl(databaseBuilder);
  }
}
