package com.ilustris.sagai.di

import android.content.Context
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.request.crossfade
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.ImagenClientImpl
import com.ilustris.sagai.core.ai.TextGenClient
import com.ilustris.sagai.core.analytics.AnalyticsService
import com.ilustris.sagai.core.database.DatabaseBuilder
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.datastore.DataStorePreferencesImpl
import com.ilustris.sagai.core.file.BackupService
import com.ilustris.sagai.core.file.FileCacheService
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.FileManager
import com.ilustris.sagai.core.file.GenreReferenceHelper
import com.ilustris.sagai.core.file.ImageCropHelper
import com.ilustris.sagai.core.lifecycle.AppLifecycleManager
import com.ilustris.sagai.core.lifecycle.AppLifecycleManagerImpl
import com.ilustris.sagai.core.media.MediaPlayerManager
import com.ilustris.sagai.core.media.MediaPlayerManagerImpl
import com.ilustris.sagai.core.media.notification.MediaNotificationManager
import com.ilustris.sagai.core.media.notification.MediaNotificationManagerImpl
import com.ilustris.sagai.core.notifications.ScheduledNotificationService
import com.ilustris.sagai.core.notifications.ScheduledNotificationServiceImpl
import com.ilustris.sagai.core.notifications.WorkManagerScheduler
import com.ilustris.sagai.core.notifications.WorkManagerSchedulerImpl
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.segmentation.ImageSegmentationHelper
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.FirebaseInstallationService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.act.data.repository.ActRepository
import com.ilustris.sagai.features.act.data.repository.ActRepositoryImpl
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.act.data.usecase.ActUseCaseImpl
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepositoryImpl
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCaseImpl
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCaseImpl
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepository
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepositoryImpl
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepository
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepositoryImpl
import com.ilustris.sagai.features.characters.relations.data.usecase.CharacterRelationUseCase
import com.ilustris.sagai.features.characters.relations.data.usecase.CharacterRelationUseCaseImpl
import com.ilustris.sagai.features.characters.repository.CharacterRepository
import com.ilustris.sagai.features.characters.repository.CharacterRepositoryImpl
import com.ilustris.sagai.features.home.data.usecase.HomeUseCase
import com.ilustris.sagai.features.home.data.usecase.HomeUseCaseImpl
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCaseImpl
import com.ilustris.sagai.features.playthrough.PlaythroughUseCase
import com.ilustris.sagai.features.playthrough.PlaythroughUseCaseImpl
import com.ilustris.sagai.features.saga.chat.data.manager.ChatNotificationManager
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManagerImpl
import com.ilustris.sagai.features.saga.chat.data.usecase.GetInputSuggestionsUseCase
import com.ilustris.sagai.features.saga.chat.data.usecase.GetInputSuggestionsUseCaseImpl
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCaseImpl
import com.ilustris.sagai.features.saga.chat.domain.manager.ChatNotificationManagerImpl
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository
import com.ilustris.sagai.features.saga.chat.repository.MessageRepositoryImpl
import com.ilustris.sagai.features.saga.chat.repository.ReactionRepository
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupService
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupServiceImpl
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import com.ilustris.sagai.features.saga.chat.repository.SagaRepositoryImpl
import com.ilustris.sagai.features.saga.datasource.ReactionRepositoryImpl
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCase
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCaseImpl
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import com.ilustris.sagai.features.settings.domain.SettingsUseCaseImpl
import com.ilustris.sagai.features.share.domain.SharePlayUseCase
import com.ilustris.sagai.features.share.domain.SharePlayUseCaseImpl
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepositoryImpl
import com.ilustris.sagai.features.timeline.domain.TimelineUseCase
import com.ilustris.sagai.features.timeline.domain.TimelineUseCaseImpl
import com.ilustris.sagai.features.wiki.data.repository.WikiRepository
import com.ilustris.sagai.features.wiki.data.repository.WikiRepositoryImpl
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCaseImpl
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun providesAppLifecycleManager(): AppLifecycleManager = AppLifecycleManagerImpl()

    @Provides
    @Singleton
    fun providesFileManager(
        @ApplicationContext context: Context,
        fileHelper: FileHelper,
    ) = FileManager(
        fileHelper,
        context,
    )

    @Provides
    @Singleton
    fun providesPermissionService(
        @ApplicationContext context: Context,
    ) = PermissionService(context)

    @Provides
    @Singleton
    fun providesBackupService(
        @ApplicationContext context: Context,
        preferences: DataStorePreferences,
        fileHelper: FileHelper,
    ) = BackupService(context, preferences, fileHelper)

    @Provides
    @Singleton
    fun providesImageSegmentationHelper(
        @ApplicationContext context: Context,
    ) = ImageSegmentationHelper(
        context,
    )

    @Provides
    @Singleton
    fun provideImageCropHelper(): ImageCropHelper = ImageCropHelper()

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader =
        ImageLoader
            .Builder(context)
            .crossfade(true)
            .build()

    @Provides
    @Singleton
    fun providesReferenceHelper(
        @ApplicationContext context: Context,
        firebaseRemoteConfig: RemoteConfigService,
        imageLoader: ImageLoader,
    ) = GenreReferenceHelper(
        context,
        firebaseRemoteConfig,
        imageLoader,
    )

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideSagaDatabase(databaseBuilder: DatabaseBuilder): SagaDatabase = databaseBuilder.buildDataBase()

    @Provides
    @Singleton
    fun providesTextGenClient(remoteConfigService: RemoteConfigService): TextGenClient = TextGenClient(remoteConfigService)

    @Provides
    @Singleton
    fun providesSummarizationClient(remoteConfigService: RemoteConfigService): GemmaClient = GemmaClient(remoteConfigService)

    @Provides
    @Singleton
    fun bindsFileHelper(
        @ApplicationContext context: Context,
    ) = FileHelper(context)

    @Provides
    @Singleton
    fun bindsFileCacheService(
        @ApplicationContext context: Context,
    ) = FileCacheService(context)

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        val configSettings =
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 60
            }
        remoteConfig.setConfigSettingsAsync(configSettings)
        return remoteConfig
    }

    @Provides
    @Singleton
    fun providesRemoteConfigService(): RemoteConfigService = RemoteConfigService()

    @Provides
    @Singleton
    fun provideAnalyticsService(
        @ApplicationContext context: Context,
    ): AnalyticsService = AnalyticsService(context)

    @Provides
    @Singleton
    fun provideImagenClient(
        remoteConfigService: RemoteConfigService,
        billingService: BillingService,
        analyticsService: AnalyticsService,
        gemmaClient: GemmaClient,
    ): ImagenClient = ImagenClientImpl(billingService, remoteConfigService, gemmaClient, analyticsService)

    @Provides
    @Singleton
    fun provideMediaPlayerManager(
        @ApplicationContext context: Context,
    ): MediaPlayerManager = MediaPlayerManagerImpl(context)

    @Provides
    @Singleton
    fun provideBillingService(
        @ApplicationContext context: Context,
        remoteConfigService: RemoteConfigService,
        firebaseInstallationService: FirebaseInstallationService,
    ): BillingService = BillingService(context, remoteConfigService, firebaseInstallationService)

    @Provides
    @Singleton
    fun provideDataStorePreferences(
        @ApplicationContext context: Context,
    ): DataStorePreferences = DataStorePreferencesImpl(context)

    @Provides
    @Singleton
    fun provideFirebaseInstallationService(): FirebaseInstallationService = FirebaseInstallationService()

    @Provides
    fun providesNotificationManager(
        @ApplicationContext context: Context,
        fileHelper: FileHelper,
        lifecycleManager: AppLifecycleManager,
    ): ChatNotificationManager =
        ChatNotificationManagerImpl(
            context,
            fileHelper,
            lifecycleManager,
        )

    @Provides
    fun providesScheduleNotificationService(
        @ApplicationContext context: Context,
        workManagerScheduler: WorkManagerScheduler,
        preferences: DataStorePreferences,
    ): ScheduledNotificationService =
        ScheduledNotificationServiceImpl(
            context,
            context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager,
            workManagerScheduler,
            preferences,
        )

    @Provides
    @Singleton
    fun providesWorkManagerScheduler(
        @ApplicationContext context: Context,
    ): WorkManagerScheduler = WorkManagerSchedulerImpl(context)
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class UseCaseModule {
    @Binds
    abstract fun providesSaveShare(sharePlayUseCaseImpl: SharePlayUseCaseImpl): SharePlayUseCase

    @Binds
    abstract fun providesEmotionalUseCase(emotionalUseCaseImpl: EmotionalUseCaseImpl): EmotionalUseCase

    @Binds
    abstract fun providesSagaHistoryUseCase(sagaHistoryUseCaseImpl: SagaHistoryUseCaseImpl): SagaHistoryUseCase

    @Binds
    abstract fun providesHomeUseCase(homeUseCaseImpl: HomeUseCaseImpl): HomeUseCase

    @Binds
    abstract fun providesMessageUseCase(messageUseCaseImpl: MessageUseCaseImpl): MessageUseCase

    @Binds
    abstract fun providesChapterUseCase(chapterUseCaseImpl: ChapterUseCaseImpl): ChapterUseCase

    @Binds
    abstract fun providesCharacterUseCase(characterUseCaseImpl: CharacterUseCaseImpl): CharacterUseCase

    @Binds
    abstract fun providesCharacterRelationUseCase(characterRelationUseCaseImpl: CharacterRelationUseCaseImpl): CharacterRelationUseCase

    @Binds
    abstract fun providesSagaDetailUseCase(sagaDetailUseCaseImpl: SagaDetailUseCaseImpl): SagaDetailUseCase

    @Binds
    abstract fun providesWikiUseCase(wikiUseCaseImpl: WikiUseCaseImpl): WikiUseCase

    @Binds
    abstract fun providesSagaContentManager(sagaContentManagerImpl: SagaContentManagerImpl): SagaContentManager

    @Binds
    abstract fun proviesTimelineUseCase(timelineUseCaseImpl: TimelineUseCaseImpl): TimelineUseCase

    @Binds
    abstract fun providesActUseCase(actUseCaseImpl: ActUseCaseImpl): ActUseCase

    @Binds
    abstract fun providesGetInputSuggestionsUseCase(
        getInputSuggestionsUseCaseImpl: GetInputSuggestionsUseCaseImpl,
    ): GetInputSuggestionsUseCase

    @Binds
    abstract fun provideSettingsUseCase(getSettingsUseCaseImpl: SettingsUseCaseImpl): SettingsUseCase

    @Binds
    abstract fun providesPlaythroughUseCase(playthroughUseCaseImpl: PlaythroughUseCaseImpl): PlaythroughUseCase

    @Binds
    abstract fun providesSagaBackupService(sagaBackupServiceImpl: SagaBackupServiceImpl): SagaBackupService
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindsSagaRepository(sagaRepositoryImpl: SagaRepositoryImpl): SagaRepository

    @Binds
    abstract fun bindsCharacterRelationRepository(
        characterRelationRepositoryImpl: CharacterRelationRepositoryImpl,
    ): CharacterRelationRepository

    @Binds
    abstract fun bindsMessageRepository(messageRepositoryImpl: MessageRepositoryImpl): MessageRepository

    @Binds
    abstract fun bindsChapterRepository(chapterRepositoryImpl: ChapterRepositoryImpl): ChapterRepository

    @Binds
    abstract fun bindsCharacterRepository(characterRepositoryImpl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    abstract fun bindsWikiRepository(wikiRepositoryImpl: WikiRepositoryImpl): WikiRepository

    @Binds
    abstract fun bindsTimelineRepository(timelineRepositoryImpl: TimelineRepositoryImpl): TimelineRepository

    @Binds
    abstract fun bindsCharacterEventRepository(characterEventRepositoryImpl: CharacterEventRepositoryImpl): CharacterEventRepository

    @Binds
    abstract fun bindsActRepository(actRepositoryImpl: ActRepositoryImpl): ActRepository

    @Binds
    abstract fun bindsReactionRepository(reactionRepositoryImpl: ReactionRepositoryImpl): ReactionRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindMediaNotificationManager(mediaNotificationManagerImpl: MediaNotificationManagerImpl): MediaNotificationManager
}
