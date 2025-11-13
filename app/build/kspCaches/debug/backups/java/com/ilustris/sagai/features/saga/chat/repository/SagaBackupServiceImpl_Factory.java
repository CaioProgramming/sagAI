package com.ilustris.sagai.features.saga.chat.repository;

import com.ilustris.sagai.core.file.BackupService;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.features.act.data.repository.ActRepository;
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository;
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepository;
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepository;
import com.ilustris.sagai.features.characters.repository.CharacterRepository;
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository;
import com.ilustris.sagai.features.wiki.data.repository.WikiRepository;
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
public final class SagaBackupServiceImpl_Factory implements Factory<SagaBackupServiceImpl> {
  private final Provider<SagaRepository> sagaRepositoryProvider;

  private final Provider<CharacterRepository> characterRepositoryProvider;

  private final Provider<ActRepository> actRepositoryProvider;

  private final Provider<ChapterRepository> chapterRepositoryProvider;

  private final Provider<TimelineRepository> timelineRepositoryProvider;

  private final Provider<WikiRepository> wikiRepositoryProvider;

  private final Provider<CharacterRelationRepository> relationRepositoryProvider;

  private final Provider<CharacterEventRepository> characterEventRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<ReactionRepository> reactionRepositoryProvider;

  private final Provider<BackupService> backupServiceProvider;

  private final Provider<FileHelper> fileHelperProvider;

  public SagaBackupServiceImpl_Factory(Provider<SagaRepository> sagaRepositoryProvider,
      Provider<CharacterRepository> characterRepositoryProvider,
      Provider<ActRepository> actRepositoryProvider,
      Provider<ChapterRepository> chapterRepositoryProvider,
      Provider<TimelineRepository> timelineRepositoryProvider,
      Provider<WikiRepository> wikiRepositoryProvider,
      Provider<CharacterRelationRepository> relationRepositoryProvider,
      Provider<CharacterEventRepository> characterEventRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<ReactionRepository> reactionRepositoryProvider,
      Provider<BackupService> backupServiceProvider, Provider<FileHelper> fileHelperProvider) {
    this.sagaRepositoryProvider = sagaRepositoryProvider;
    this.characterRepositoryProvider = characterRepositoryProvider;
    this.actRepositoryProvider = actRepositoryProvider;
    this.chapterRepositoryProvider = chapterRepositoryProvider;
    this.timelineRepositoryProvider = timelineRepositoryProvider;
    this.wikiRepositoryProvider = wikiRepositoryProvider;
    this.relationRepositoryProvider = relationRepositoryProvider;
    this.characterEventRepositoryProvider = characterEventRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.reactionRepositoryProvider = reactionRepositoryProvider;
    this.backupServiceProvider = backupServiceProvider;
    this.fileHelperProvider = fileHelperProvider;
  }

  @Override
  public SagaBackupServiceImpl get() {
    return newInstance(sagaRepositoryProvider.get(), characterRepositoryProvider.get(), actRepositoryProvider.get(), chapterRepositoryProvider.get(), timelineRepositoryProvider.get(), wikiRepositoryProvider.get(), relationRepositoryProvider.get(), characterEventRepositoryProvider.get(), messageRepositoryProvider.get(), reactionRepositoryProvider.get(), backupServiceProvider.get(), fileHelperProvider.get());
  }

  public static SagaBackupServiceImpl_Factory create(
      Provider<SagaRepository> sagaRepositoryProvider,
      Provider<CharacterRepository> characterRepositoryProvider,
      Provider<ActRepository> actRepositoryProvider,
      Provider<ChapterRepository> chapterRepositoryProvider,
      Provider<TimelineRepository> timelineRepositoryProvider,
      Provider<WikiRepository> wikiRepositoryProvider,
      Provider<CharacterRelationRepository> relationRepositoryProvider,
      Provider<CharacterEventRepository> characterEventRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<ReactionRepository> reactionRepositoryProvider,
      Provider<BackupService> backupServiceProvider, Provider<FileHelper> fileHelperProvider) {
    return new SagaBackupServiceImpl_Factory(sagaRepositoryProvider, characterRepositoryProvider, actRepositoryProvider, chapterRepositoryProvider, timelineRepositoryProvider, wikiRepositoryProvider, relationRepositoryProvider, characterEventRepositoryProvider, messageRepositoryProvider, reactionRepositoryProvider, backupServiceProvider, fileHelperProvider);
  }

  public static SagaBackupServiceImpl newInstance(SagaRepository sagaRepository,
      CharacterRepository characterRepository, ActRepository actRepository,
      ChapterRepository chapterRepository, TimelineRepository timelineRepository,
      WikiRepository wikiRepository, CharacterRelationRepository relationRepository,
      CharacterEventRepository characterEventRepository, MessageRepository messageRepository,
      ReactionRepository reactionRepository, BackupService backupService, FileHelper fileHelper) {
    return new SagaBackupServiceImpl(sagaRepository, characterRepository, actRepository, chapterRepository, timelineRepository, wikiRepository, relationRepository, characterEventRepository, messageRepository, reactionRepository, backupService, fileHelper);
  }
}
