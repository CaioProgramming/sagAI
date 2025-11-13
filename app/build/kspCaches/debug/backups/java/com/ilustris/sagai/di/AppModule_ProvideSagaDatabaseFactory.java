package com.ilustris.sagai.di;

import com.ilustris.sagai.core.database.DatabaseBuilder;
import com.ilustris.sagai.core.database.SagaDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideSagaDatabaseFactory implements Factory<SagaDatabase> {
  private final Provider<DatabaseBuilder> databaseBuilderProvider;

  public AppModule_ProvideSagaDatabaseFactory(Provider<DatabaseBuilder> databaseBuilderProvider) {
    this.databaseBuilderProvider = databaseBuilderProvider;
  }

  @Override
  public SagaDatabase get() {
    return provideSagaDatabase(databaseBuilderProvider.get());
  }

  public static AppModule_ProvideSagaDatabaseFactory create(
      Provider<DatabaseBuilder> databaseBuilderProvider) {
    return new AppModule_ProvideSagaDatabaseFactory(databaseBuilderProvider);
  }

  public static SagaDatabase provideSagaDatabase(DatabaseBuilder databaseBuilder) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSagaDatabase(databaseBuilder));
  }
}
