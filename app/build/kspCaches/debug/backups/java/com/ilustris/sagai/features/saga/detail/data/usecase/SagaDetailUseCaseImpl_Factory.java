package com.ilustris.sagai.features.saga.detail.data.usecase;

import com.ilustris.sagai.core.ai.TextGenClient;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository;
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase;
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase;
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase;
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
public final class SagaDetailUseCaseImpl_Factory implements Factory<SagaDetailUseCaseImpl> {
  private final Provider<SagaRepository> sagaRepositoryProvider;

  private final Provider<FileHelper> fileHelperProvider;

  private final Provider<TextGenClient> textGenClientProvider;

  private final Provider<TimelineUseCase> timelineUseCaseProvider;

  private final Provider<EmotionalUseCase> emotionalUseCaseProvider;

  private final Provider<WikiUseCase> wikiUseCaseProvider;

  public SagaDetailUseCaseImpl_Factory(Provider<SagaRepository> sagaRepositoryProvider,
      Provider<FileHelper> fileHelperProvider, Provider<TextGenClient> textGenClientProvider,
      Provider<TimelineUseCase> timelineUseCaseProvider,
      Provider<EmotionalUseCase> emotionalUseCaseProvider,
      Provider<WikiUseCase> wikiUseCaseProvider) {
    this.sagaRepositoryProvider = sagaRepositoryProvider;
    this.fileHelperProvider = fileHelperProvider;
    this.textGenClientProvider = textGenClientProvider;
    this.timelineUseCaseProvider = timelineUseCaseProvider;
    this.emotionalUseCaseProvider = emotionalUseCaseProvider;
    this.wikiUseCaseProvider = wikiUseCaseProvider;
  }

  @Override
  public SagaDetailUseCaseImpl get() {
    return newInstance(sagaRepositoryProvider.get(), fileHelperProvider.get(), textGenClientProvider.get(), timelineUseCaseProvider.get(), emotionalUseCaseProvider.get(), wikiUseCaseProvider.get());
  }

  public static SagaDetailUseCaseImpl_Factory create(
      Provider<SagaRepository> sagaRepositoryProvider, Provider<FileHelper> fileHelperProvider,
      Provider<TextGenClient> textGenClientProvider,
      Provider<TimelineUseCase> timelineUseCaseProvider,
      Provider<EmotionalUseCase> emotionalUseCaseProvider,
      Provider<WikiUseCase> wikiUseCaseProvider) {
    return new SagaDetailUseCaseImpl_Factory(sagaRepositoryProvider, fileHelperProvider, textGenClientProvider, timelineUseCaseProvider, emotionalUseCaseProvider, wikiUseCaseProvider);
  }

  public static SagaDetailUseCaseImpl newInstance(SagaRepository sagaRepository,
      FileHelper fileHelper, TextGenClient textGenClient, TimelineUseCase timelineUseCase,
      EmotionalUseCase emotionalUseCase, WikiUseCase wikiUseCase) {
    return new SagaDetailUseCaseImpl(sagaRepository, fileHelper, textGenClient, timelineUseCase, emotionalUseCase, wikiUseCase);
  }
}
