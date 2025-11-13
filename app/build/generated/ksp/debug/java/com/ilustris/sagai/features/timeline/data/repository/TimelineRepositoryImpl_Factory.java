package com.ilustris.sagai.features.timeline.data.repository;

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
public final class TimelineRepositoryImpl_Factory implements Factory<TimelineRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public TimelineRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public TimelineRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static TimelineRepositoryImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new TimelineRepositoryImpl_Factory(databaseProvider);
  }

  public static TimelineRepositoryImpl newInstance(SagaDatabase database) {
    return new TimelineRepositoryImpl(database);
  }
}
