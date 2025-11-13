package com.ilustris.sagai.features.saga.chat.repository;

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
public final class MessageRepositoryImpl_Factory implements Factory<MessageRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  public MessageRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MessageRepositoryImpl get() {
    return newInstance(databaseProvider.get());
  }

  public static MessageRepositoryImpl_Factory create(Provider<SagaDatabase> databaseProvider) {
    return new MessageRepositoryImpl_Factory(databaseProvider);
  }

  public static MessageRepositoryImpl newInstance(SagaDatabase database) {
    return new MessageRepositoryImpl(database);
  }
}
