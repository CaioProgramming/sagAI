package com.ilustris.sagai.features.saga.chat.data.manager;

import android.content.Context;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.ilustris.sagai.core.file.BackupService;
import com.ilustris.sagai.core.file.FileCacheService;
import com.ilustris.sagai.features.act.data.usecase.ActUseCase;
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase;
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase;
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase;
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
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SagaContentManagerImpl_Factory implements Factory<SagaContentManagerImpl> {
  private final Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider;

  private final Provider<CharacterUseCase> characterUseCaseProvider;

  private final Provider<ChapterUseCase> chapterUseCaseProvider;

  private final Provider<WikiUseCase> wikiUseCaseProvider;

  private final Provider<TimelineUseCase> timelineUseCaseProvider;

  private final Provider<ActUseCase> actUseCaseProvider;

  private final Provider<EmotionalUseCase> emotionalUseCaseProvider;

  private final Provider<FileCacheService> fileCacheServiceProvider;

  private final Provider<FirebaseRemoteConfig> remoteConfigProvider;

  private final Provider<BackupService> backupServiceProvider;

  private final Provider<Context> contextProvider;

  public SagaContentManagerImpl_Factory(Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider,
      Provider<CharacterUseCase> characterUseCaseProvider,
      Provider<ChapterUseCase> chapterUseCaseProvider, Provider<WikiUseCase> wikiUseCaseProvider,
      Provider<TimelineUseCase> timelineUseCaseProvider, Provider<ActUseCase> actUseCaseProvider,
      Provider<EmotionalUseCase> emotionalUseCaseProvider,
      Provider<FileCacheService> fileCacheServiceProvider,
      Provider<FirebaseRemoteConfig> remoteConfigProvider,
      Provider<BackupService> backupServiceProvider, Provider<Context> contextProvider) {
    this.sagaHistoryUseCaseProvider = sagaHistoryUseCaseProvider;
    this.characterUseCaseProvider = characterUseCaseProvider;
    this.chapterUseCaseProvider = chapterUseCaseProvider;
    this.wikiUseCaseProvider = wikiUseCaseProvider;
    this.timelineUseCaseProvider = timelineUseCaseProvider;
    this.actUseCaseProvider = actUseCaseProvider;
    this.emotionalUseCaseProvider = emotionalUseCaseProvider;
    this.fileCacheServiceProvider = fileCacheServiceProvider;
    this.remoteConfigProvider = remoteConfigProvider;
    this.backupServiceProvider = backupServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SagaContentManagerImpl get() {
    return newInstance(sagaHistoryUseCaseProvider.get(), characterUseCaseProvider.get(), chapterUseCaseProvider.get(), wikiUseCaseProvider.get(), timelineUseCaseProvider.get(), actUseCaseProvider.get(), emotionalUseCaseProvider.get(), fileCacheServiceProvider.get(), remoteConfigProvider.get(), backupServiceProvider.get(), contextProvider.get());
  }

  public static SagaContentManagerImpl_Factory create(
      Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider,
      Provider<CharacterUseCase> characterUseCaseProvider,
      Provider<ChapterUseCase> chapterUseCaseProvider, Provider<WikiUseCase> wikiUseCaseProvider,
      Provider<TimelineUseCase> timelineUseCaseProvider, Provider<ActUseCase> actUseCaseProvider,
      Provider<EmotionalUseCase> emotionalUseCaseProvider,
      Provider<FileCacheService> fileCacheServiceProvider,
      Provider<FirebaseRemoteConfig> remoteConfigProvider,
      Provider<BackupService> backupServiceProvider, Provider<Context> contextProvider) {
    return new SagaContentManagerImpl_Factory(sagaHistoryUseCaseProvider, characterUseCaseProvider, chapterUseCaseProvider, wikiUseCaseProvider, timelineUseCaseProvider, actUseCaseProvider, emotionalUseCaseProvider, fileCacheServiceProvider, remoteConfigProvider, backupServiceProvider, contextProvider);
  }

  public static SagaContentManagerImpl newInstance(SagaHistoryUseCase sagaHistoryUseCase,
      CharacterUseCase characterUseCase, ChapterUseCase chapterUseCase, WikiUseCase wikiUseCase,
      TimelineUseCase timelineUseCase, ActUseCase actUseCase, EmotionalUseCase emotionalUseCase,
      FileCacheService fileCacheService, FirebaseRemoteConfig remoteConfig,
      BackupService backupService, Context context) {
    return new SagaContentManagerImpl(sagaHistoryUseCase, characterUseCase, chapterUseCase, wikiUseCase, timelineUseCase, actUseCase, emotionalUseCase, fileCacheService, remoteConfig, backupService, context);
  }
}
