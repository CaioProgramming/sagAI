package com.ilustris.sagai.features.timeline.domain;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase;
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository;
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
public final class TimelineUseCaseImpl_Factory implements Factory<TimelineUseCaseImpl> {
  private final Provider<TimelineRepository> timelineRepositoryProvider;

  private final Provider<EmotionalUseCase> emotionalUseCaseProvider;

  private final Provider<WikiUseCase> wikiUseCaseProvider;

  private final Provider<CharacterUseCase> characterUseCaseProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public TimelineUseCaseImpl_Factory(Provider<TimelineRepository> timelineRepositoryProvider,
      Provider<EmotionalUseCase> emotionalUseCaseProvider,
      Provider<WikiUseCase> wikiUseCaseProvider,
      Provider<CharacterUseCase> characterUseCaseProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    this.timelineRepositoryProvider = timelineRepositoryProvider;
    this.emotionalUseCaseProvider = emotionalUseCaseProvider;
    this.wikiUseCaseProvider = wikiUseCaseProvider;
    this.characterUseCaseProvider = characterUseCaseProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public TimelineUseCaseImpl get() {
    return newInstance(timelineRepositoryProvider.get(), emotionalUseCaseProvider.get(), wikiUseCaseProvider.get(), characterUseCaseProvider.get(), gemmaClientProvider.get());
  }

  public static TimelineUseCaseImpl_Factory create(
      Provider<TimelineRepository> timelineRepositoryProvider,
      Provider<EmotionalUseCase> emotionalUseCaseProvider,
      Provider<WikiUseCase> wikiUseCaseProvider,
      Provider<CharacterUseCase> characterUseCaseProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    return new TimelineUseCaseImpl_Factory(timelineRepositoryProvider, emotionalUseCaseProvider, wikiUseCaseProvider, characterUseCaseProvider, gemmaClientProvider);
  }

  public static TimelineUseCaseImpl newInstance(TimelineRepository timelineRepository,
      EmotionalUseCase emotionalUseCase, WikiUseCase wikiUseCase, CharacterUseCase characterUseCase,
      GemmaClient gemmaClient) {
    return new TimelineUseCaseImpl(timelineRepository, emotionalUseCase, wikiUseCase, characterUseCase, gemmaClient);
  }
}
