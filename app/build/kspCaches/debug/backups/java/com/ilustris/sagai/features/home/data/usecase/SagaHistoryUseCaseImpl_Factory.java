package com.ilustris.sagai.features.home.data.usecase;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.ai.TextGenClient;
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
public final class SagaHistoryUseCaseImpl_Factory implements Factory<SagaHistoryUseCaseImpl> {
  private final Provider<SagaRepository> sagaRepositoryProvider;

  private final Provider<TextGenClient> textGenClientProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public SagaHistoryUseCaseImpl_Factory(Provider<SagaRepository> sagaRepositoryProvider,
      Provider<TextGenClient> textGenClientProvider, Provider<GemmaClient> gemmaClientProvider) {
    this.sagaRepositoryProvider = sagaRepositoryProvider;
    this.textGenClientProvider = textGenClientProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public SagaHistoryUseCaseImpl get() {
    return newInstance(sagaRepositoryProvider.get(), textGenClientProvider.get(), gemmaClientProvider.get());
  }

  public static SagaHistoryUseCaseImpl_Factory create(
      Provider<SagaRepository> sagaRepositoryProvider,
      Provider<TextGenClient> textGenClientProvider, Provider<GemmaClient> gemmaClientProvider) {
    return new SagaHistoryUseCaseImpl_Factory(sagaRepositoryProvider, textGenClientProvider, gemmaClientProvider);
  }

  public static SagaHistoryUseCaseImpl newInstance(SagaRepository sagaRepository,
      TextGenClient textGenClient, GemmaClient gemmaClient) {
    return new SagaHistoryUseCaseImpl(sagaRepository, textGenClient, gemmaClient);
  }
}
