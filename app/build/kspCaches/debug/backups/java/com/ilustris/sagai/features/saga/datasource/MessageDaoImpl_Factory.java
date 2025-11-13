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
public final class MessageDaoImpl_Factory implements Factory<MessageDaoImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public MessageDaoImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MessageDaoImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static MessageDaoImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new MessageDaoImpl_Factory(databaseProvider);
  }

  public static MessageDaoImpl newInstance(SagaDatabase database) {
    return new MessageDaoImpl(database);
  }
}
