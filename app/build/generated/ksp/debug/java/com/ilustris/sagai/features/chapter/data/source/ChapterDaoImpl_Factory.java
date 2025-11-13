package com.ilustris.sagai.features.chapter.data.source;

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
public final class ChapterDaoImpl_Factory implements Factory<ChapterDaoImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public ChapterDaoImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ChapterDaoImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static ChapterDaoImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new ChapterDaoImpl_Factory(databaseProvider);
  }

  public static ChapterDaoImpl newInstance(SagaDatabase database) {
    return new ChapterDaoImpl(database);
  }
}
