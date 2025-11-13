package com.ilustris.sagai.features.newsaga.data.usecase;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository;
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
public final class NewSagaUseCaseImpl_Factory implements Factory<NewSagaUseCaseImpl> {
  private final Provider<SagaRepository> sagaRepositoryProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public NewSagaUseCaseImpl_Factory(Provider<SagaRepository> sagaRepositoryProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    this.sagaRepositoryProvider = sagaRepositoryProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public NewSagaUseCaseImpl get() {
    return newInstance(sagaRepositoryProvider.get(), gemmaClientProvider.get());
  }

  public static NewSagaUseCaseImpl_Factory create(Provider<SagaRepository> sagaRepositoryProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    return new NewSagaUseCaseImpl_Factory(sagaRepositoryProvider, gemmaClientProvider);
  }

  public static NewSagaUseCaseImpl newInstance(SagaRepository sagaRepository,
      GemmaClient gemmaClient) {
    return new NewSagaUseCaseImpl(sagaRepository, gemmaClient);
  }
}
