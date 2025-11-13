package com.ilustris.sagai;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import coil3.ImageLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.ai.ImagenClient;
import com.ilustris.sagai.core.ai.TextGenClient;
import com.ilustris.sagai.core.database.DatabaseBuilder;
import com.ilustris.sagai.core.database.SagaDatabase;
import com.ilustris.sagai.core.datastore.DataStorePreferences;
import com.ilustris.sagai.core.file.BackupService;
import com.ilustris.sagai.core.file.FileCacheService;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.core.file.FileManager;
import com.ilustris.sagai.core.file.GenreReferenceHelper;
import com.ilustris.sagai.core.file.ImageCropHelper;
import com.ilustris.sagai.core.file.backup.ui.BackupViewModel;
import com.ilustris.sagai.core.file.backup.ui.BackupViewModel_HiltModules;
import com.ilustris.sagai.core.file.backup.ui.BackupViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.core.file.backup.ui.BackupViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.core.lifecycle.AppLifecycleManagerImpl;
import com.ilustris.sagai.core.media.MediaPlayerManager;
import com.ilustris.sagai.core.media.MediaPlayerService;
import com.ilustris.sagai.core.media.MediaPlayerService_MembersInjector;
import com.ilustris.sagai.core.media.notification.MediaNotificationManagerImpl;
import com.ilustris.sagai.core.permissions.PermissionService;
import com.ilustris.sagai.core.services.BillingService;
import com.ilustris.sagai.core.services.RemoteConfigService;
import com.ilustris.sagai.di.AppModule_BindsFileCacheServiceFactory;
import com.ilustris.sagai.di.AppModule_BindsFileHelperFactory;
import com.ilustris.sagai.di.AppModule_ProvideBillingServiceFactory;
import com.ilustris.sagai.di.AppModule_ProvideDataStorePreferencesFactory;
import com.ilustris.sagai.di.AppModule_ProvideFirebaseRemoteConfigFactory;
import com.ilustris.sagai.di.AppModule_ProvideGsonFactory;
import com.ilustris.sagai.di.AppModule_ProvideImageCropHelperFactory;
import com.ilustris.sagai.di.AppModule_ProvideImageLoaderFactory;
import com.ilustris.sagai.di.AppModule_ProvideImagenClientFactory;
import com.ilustris.sagai.di.AppModule_ProvideMediaPlayerManagerFactory;
import com.ilustris.sagai.di.AppModule_ProvideSagaDatabaseFactory;
import com.ilustris.sagai.di.AppModule_ProvidesBackupServiceFactory;
import com.ilustris.sagai.di.AppModule_ProvidesFileManagerFactory;
import com.ilustris.sagai.di.AppModule_ProvidesPermissionServiceFactory;
import com.ilustris.sagai.di.AppModule_ProvidesReferenceHelperFactory;
import com.ilustris.sagai.di.AppModule_ProvidesRemoteConfigServiceFactory;
import com.ilustris.sagai.di.AppModule_ProvidesSummarizationClientFactory;
import com.ilustris.sagai.di.AppModule_ProvidesTextGenClientFactory;
import com.ilustris.sagai.features.act.data.repository.ActRepositoryImpl;
import com.ilustris.sagai.features.act.data.usecase.ActUseCaseImpl;
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepositoryImpl;
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCaseImpl;
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel;
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel_HiltModules;
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCaseImpl;
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepositoryImpl;
import com.ilustris.sagai.features.characters.presentation.CharacterViewModel;
import com.ilustris.sagai.features.characters.presentation.CharacterViewModel_HiltModules;
import com.ilustris.sagai.features.characters.presentation.CharacterViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.characters.presentation.CharacterViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.characters.relations.data.repository.CharacterRelationRepositoryImpl;
import com.ilustris.sagai.features.characters.relations.data.usecase.CharacterRelationUseCaseImpl;
import com.ilustris.sagai.features.characters.repository.CharacterRepositoryImpl;
import com.ilustris.sagai.features.characters.ui.CharacterDetailsViewModel;
import com.ilustris.sagai.features.characters.ui.CharacterDetailsViewModel_HiltModules;
import com.ilustris.sagai.features.characters.ui.CharacterDetailsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.characters.ui.CharacterDetailsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.home.data.usecase.HomeUseCaseImpl;
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCaseImpl;
import com.ilustris.sagai.features.home.ui.HomeViewModel;
import com.ilustris.sagai.features.home.ui.HomeViewModel_HiltModules;
import com.ilustris.sagai.features.home.ui.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.home.ui.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCaseImpl;
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel;
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel_HiltModules;
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.premium.PremiumViewModel;
import com.ilustris.sagai.features.premium.PremiumViewModel_HiltModules;
import com.ilustris.sagai.features.premium.PremiumViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.premium.PremiumViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManagerImpl;
import com.ilustris.sagai.features.saga.chat.data.usecase.GetInputSuggestionsUseCaseImpl;
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCaseImpl;
import com.ilustris.sagai.features.saga.chat.domain.manager.ChatNotificationManagerImpl;
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel;
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel_HiltModules;
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.saga.chat.repository.MessageRepositoryImpl;
import com.ilustris.sagai.features.saga.chat.repository.SagaBackupServiceImpl;
import com.ilustris.sagai.features.saga.chat.repository.SagaRepositoryImpl;
import com.ilustris.sagai.features.saga.datasource.ReactionRepositoryImpl;
import com.ilustris.sagai.features.saga.detail.data.usecase.SagaDetailUseCaseImpl;
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel;
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel_HiltModules;
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.settings.domain.SettingsUseCaseImpl;
import com.ilustris.sagai.features.settings.ui.SettingsViewModel;
import com.ilustris.sagai.features.settings.ui.SettingsViewModel_HiltModules;
import com.ilustris.sagai.features.settings.ui.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.settings.ui.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.share.domain.SharePlayUseCaseImpl;
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel;
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel_HiltModules;
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepositoryImpl;
import com.ilustris.sagai.features.timeline.domain.TimelineUseCaseImpl;
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel;
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel_HiltModules;
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.ilustris.sagai.features.wiki.data.repository.WikiRepositoryImpl;
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCaseImpl;
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCaseImpl;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerSagaApp_HiltComponents_SingletonC {
  private DaggerSagaApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public SagaApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements SagaApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public SagaApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements SagaApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public SagaApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements SagaApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public SagaApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements SagaApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SagaApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements SagaApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SagaApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements SagaApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public SagaApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements SagaApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public SagaApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends SagaApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends SagaApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends SagaApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends SagaApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(12).put(BackupViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, BackupViewModel_HiltModules.KeyModule.provide()).put(ChapterViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChapterViewModel_HiltModules.KeyModule.provide()).put(CharacterDetailsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CharacterDetailsViewModel_HiltModules.KeyModule.provide()).put(CharacterViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CharacterViewModel_HiltModules.KeyModule.provide()).put(ChatViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChatViewModel_HiltModules.KeyModule.provide()).put(CreateSagaViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CreateSagaViewModel_HiltModules.KeyModule.provide()).put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide()).put(PremiumViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PremiumViewModel_HiltModules.KeyModule.provide()).put(SagaDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SagaDetailViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(SharePlayViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SharePlayViewModel_HiltModules.KeyModule.provide()).put(TimelineViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TimelineViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends SagaApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<BackupViewModel> backupViewModelProvider;

    Provider<ChapterViewModel> chapterViewModelProvider;

    Provider<CharacterDetailsViewModel> characterDetailsViewModelProvider;

    Provider<CharacterViewModel> characterViewModelProvider;

    Provider<ChatViewModel> chatViewModelProvider;

    Provider<CreateSagaViewModel> createSagaViewModelProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    Provider<PremiumViewModel> premiumViewModelProvider;

    Provider<SagaDetailViewModel> sagaDetailViewModelProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    Provider<SharePlayViewModel> sharePlayViewModelProvider;

    Provider<TimelineViewModel> timelineViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    SagaRepositoryImpl sagaRepositoryImpl() {
      return new SagaRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get(), singletonCImpl.providesReferenceHelperProvider.get(), singletonCImpl.providesSummarizationClientProvider.get(), singletonCImpl.provideImageCropHelperProvider.get(), singletonCImpl.bindsFileHelperProvider.get(), singletonCImpl.provideImagenClientProvider.get(), singletonCImpl.providesBackupServiceProvider.get());
    }

    CharacterRepositoryImpl characterRepositoryImpl() {
      return new CharacterRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    ActRepositoryImpl actRepositoryImpl() {
      return new ActRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    ChapterRepositoryImpl chapterRepositoryImpl() {
      return new ChapterRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    TimelineRepositoryImpl timelineRepositoryImpl() {
      return new TimelineRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    WikiRepositoryImpl wikiRepositoryImpl() {
      return new WikiRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    CharacterRelationRepositoryImpl characterRelationRepositoryImpl() {
      return new CharacterRelationRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    CharacterEventRepositoryImpl characterEventRepositoryImpl() {
      return new CharacterEventRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    MessageRepositoryImpl messageRepositoryImpl() {
      return new MessageRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    ReactionRepositoryImpl reactionRepositoryImpl() {
      return new ReactionRepositoryImpl(singletonCImpl.provideSagaDatabaseProvider.get());
    }

    SagaBackupServiceImpl sagaBackupServiceImpl() {
      return new SagaBackupServiceImpl(sagaRepositoryImpl(), characterRepositoryImpl(), actRepositoryImpl(), chapterRepositoryImpl(), timelineRepositoryImpl(), wikiRepositoryImpl(), characterRelationRepositoryImpl(), characterEventRepositoryImpl(), messageRepositoryImpl(), reactionRepositoryImpl(), singletonCImpl.providesBackupServiceProvider.get(), singletonCImpl.bindsFileHelperProvider.get());
    }

    SagaHistoryUseCaseImpl sagaHistoryUseCaseImpl() {
      return new SagaHistoryUseCaseImpl(sagaRepositoryImpl(), singletonCImpl.providesTextGenClientProvider.get(), singletonCImpl.providesSummarizationClientProvider.get());
    }

    WikiUseCaseImpl wikiUseCaseImpl() {
      return new WikiUseCaseImpl(wikiRepositoryImpl(), singletonCImpl.providesSummarizationClientProvider.get());
    }

    ChapterUseCaseImpl chapterUseCaseImpl() {
      return new ChapterUseCaseImpl(chapterRepositoryImpl(), timelineRepositoryImpl(), wikiUseCaseImpl(), singletonCImpl.providesSummarizationClientProvider.get(), singletonCImpl.provideImagenClientProvider.get(), singletonCImpl.bindsFileHelperProvider.get(), singletonCImpl.providesReferenceHelperProvider.get());
    }

    CharacterRelationUseCaseImpl characterRelationUseCaseImpl() {
      return new CharacterRelationUseCaseImpl(singletonCImpl.providesSummarizationClientProvider.get(), characterRelationRepositoryImpl());
    }

    CharacterUseCaseImpl characterUseCaseImpl() {
      return new CharacterUseCaseImpl(characterRepositoryImpl(), characterEventRepositoryImpl(), characterRelationUseCaseImpl(), singletonCImpl.provideImagenClientProvider.get(), singletonCImpl.providesTextGenClientProvider.get(), singletonCImpl.providesSummarizationClientProvider.get(), singletonCImpl.bindsFileHelperProvider.get(), singletonCImpl.provideImageCropHelperProvider.get(), singletonCImpl.providesReferenceHelperProvider.get(), singletonCImpl.provideBillingServiceProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));
    }

    MessageUseCaseImpl messageUseCaseImpl() {
      return new MessageUseCaseImpl(messageRepositoryImpl(), reactionRepositoryImpl(), singletonCImpl.providesTextGenClientProvider.get(), singletonCImpl.providesSummarizationClientProvider.get());
    }

    EmotionalUseCaseImpl emotionalUseCaseImpl() {
      return new EmotionalUseCaseImpl(singletonCImpl.providesSummarizationClientProvider.get());
    }

    TimelineUseCaseImpl timelineUseCaseImpl() {
      return new TimelineUseCaseImpl(timelineRepositoryImpl(), emotionalUseCaseImpl(), wikiUseCaseImpl(), characterUseCaseImpl(), singletonCImpl.providesSummarizationClientProvider.get());
    }

    ActUseCaseImpl actUseCaseImpl() {
      return new ActUseCaseImpl(actRepositoryImpl(), singletonCImpl.providesSummarizationClientProvider.get());
    }

    SagaContentManagerImpl sagaContentManagerImpl() {
      return new SagaContentManagerImpl(sagaHistoryUseCaseImpl(), characterUseCaseImpl(), chapterUseCaseImpl(), wikiUseCaseImpl(), timelineUseCaseImpl(), actUseCaseImpl(), emotionalUseCaseImpl(), singletonCImpl.bindsFileCacheServiceProvider.get(), singletonCImpl.provideFirebaseRemoteConfigProvider.get(), singletonCImpl.providesBackupServiceProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));
    }

    GetInputSuggestionsUseCaseImpl getInputSuggestionsUseCaseImpl() {
      return new GetInputSuggestionsUseCaseImpl(singletonCImpl.providesSummarizationClientProvider.get());
    }

    ChatNotificationManagerImpl chatNotificationManagerImpl() {
      return new ChatNotificationManagerImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.bindsFileHelperProvider.get(), singletonCImpl.appLifecycleManagerImplProvider.get());
    }

    SettingsUseCaseImpl settingsUseCaseImpl() {
      return new SettingsUseCaseImpl(singletonCImpl.provideDataStorePreferencesProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), sagaRepositoryImpl(), singletonCImpl.provideBillingServiceProvider.get(), singletonCImpl.bindsFileHelperProvider.get(), singletonCImpl.providesPermissionServiceProvider.get(), singletonCImpl.providesBackupServiceProvider.get(), singletonCImpl.providesFileManagerProvider.get());
    }

    NewSagaUseCaseImpl newSagaUseCaseImpl() {
      return new NewSagaUseCaseImpl(sagaRepositoryImpl(), singletonCImpl.providesSummarizationClientProvider.get());
    }

    HomeUseCaseImpl homeUseCaseImpl() {
      return new HomeUseCaseImpl(sagaRepositoryImpl(), singletonCImpl.providesSummarizationClientProvider.get(), singletonCImpl.providesBackupServiceProvider.get(), sagaBackupServiceImpl(), singletonCImpl.provideFirebaseRemoteConfigProvider.get(), singletonCImpl.provideBillingServiceProvider.get());
    }

    SagaDetailUseCaseImpl sagaDetailUseCaseImpl() {
      return new SagaDetailUseCaseImpl(sagaRepositoryImpl(), singletonCImpl.bindsFileHelperProvider.get(), singletonCImpl.providesTextGenClientProvider.get(), timelineUseCaseImpl(), emotionalUseCaseImpl(), wikiUseCaseImpl());
    }

    SharePlayUseCaseImpl sharePlayUseCaseImpl() {
      return new SharePlayUseCaseImpl(singletonCImpl.bindsFileCacheServiceProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.providesSummarizationClientProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.backupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.chapterViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.characterDetailsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.characterViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.chatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.createSagaViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.premiumViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.sagaDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.sharePlayViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.timelineViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(12).put(BackupViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) backupViewModelProvider)).put(ChapterViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) chapterViewModelProvider)).put(CharacterDetailsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) characterDetailsViewModelProvider)).put(CharacterViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) characterViewModelProvider)).put(ChatViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) chatViewModelProvider)).put(CreateSagaViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) createSagaViewModelProvider)).put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) homeViewModelProvider)).put(PremiumViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) premiumViewModelProvider)).put(SagaDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) sagaDetailViewModelProvider)).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) settingsViewModelProvider)).put(SharePlayViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) sharePlayViewModelProvider)).put(TimelineViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) timelineViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.ilustris.sagai.core.file.backup.ui.BackupViewModel
          return (T) new BackupViewModel(singletonCImpl.providesBackupServiceProvider.get(), viewModelCImpl.sagaBackupServiceImpl(), viewModelCImpl.sagaRepositoryImpl());

          case 1: // com.ilustris.sagai.features.chapter.presentation.ChapterViewModel
          return (T) new ChapterViewModel(viewModelCImpl.sagaHistoryUseCaseImpl(), viewModelCImpl.chapterUseCaseImpl());

          case 2: // com.ilustris.sagai.features.characters.ui.CharacterDetailsViewModel
          return (T) new CharacterDetailsViewModel(viewModelCImpl.sagaHistoryUseCaseImpl(), viewModelCImpl.characterUseCaseImpl(), singletonCImpl.provideBillingServiceProvider.get());

          case 3: // com.ilustris.sagai.features.characters.presentation.CharacterViewModel
          return (T) new CharacterViewModel(viewModelCImpl.characterUseCaseImpl(), viewModelCImpl.sagaHistoryUseCaseImpl());

          case 4: // com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel
          return (T) new ChatViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), viewModelCImpl.messageUseCaseImpl(), viewModelCImpl.sagaContentManagerImpl(), viewModelCImpl.getInputSuggestionsUseCaseImpl(), viewModelCImpl.chatNotificationManagerImpl(), singletonCImpl.provideMediaPlayerManagerProvider.get(), viewModelCImpl.settingsUseCaseImpl());

          case 5: // com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel
          return (T) new CreateSagaViewModel(viewModelCImpl.newSagaUseCaseImpl(), viewModelCImpl.characterUseCaseImpl());

          case 6: // com.ilustris.sagai.features.home.ui.HomeViewModel
          return (T) new HomeViewModel(viewModelCImpl.homeUseCaseImpl(), singletonCImpl.providesBackupServiceProvider.get());

          case 7: // com.ilustris.sagai.features.premium.PremiumViewModel
          return (T) new PremiumViewModel(singletonCImpl.provideBillingServiceProvider.get());

          case 8: // com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel
          return (T) new SagaDetailViewModel(viewModelCImpl.sagaDetailUseCaseImpl(), singletonCImpl.provideFirebaseRemoteConfigProvider.get(), singletonCImpl.provideBillingServiceProvider.get());

          case 9: // com.ilustris.sagai.features.settings.ui.SettingsViewModel
          return (T) new SettingsViewModel(viewModelCImpl.settingsUseCaseImpl());

          case 10: // com.ilustris.sagai.features.share.presentation.SharePlayViewModel
          return (T) new SharePlayViewModel(viewModelCImpl.sharePlayUseCaseImpl());

          case 11: // com.ilustris.sagai.features.timeline.presentation.TimelineViewModel
          return (T) new TimelineViewModel(viewModelCImpl.sagaHistoryUseCaseImpl());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends SagaApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends SagaApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectMediaPlayerService(MediaPlayerService mediaPlayerService) {
      injectMediaPlayerService2(mediaPlayerService);
    }

    @CanIgnoreReturnValue
    private MediaPlayerService injectMediaPlayerService2(MediaPlayerService instance) {
      MediaPlayerService_MembersInjector.injectMediaPlayerManager(instance, singletonCImpl.provideMediaPlayerManagerProvider.get());
      MediaPlayerService_MembersInjector.injectNotificationManager(instance, singletonCImpl.mediaNotificationManagerImplProvider.get());
      MediaPlayerService_MembersInjector.injectGson(instance, singletonCImpl.provideGsonProvider.get());
      MediaPlayerService_MembersInjector.injectFileHelper(instance, singletonCImpl.bindsFileHelperProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends SagaApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<BillingService> provideBillingServiceProvider;

    Provider<DataStorePreferences> provideDataStorePreferencesProvider;

    Provider<FileHelper> bindsFileHelperProvider;

    Provider<BackupService> providesBackupServiceProvider;

    Provider<SagaDatabase> provideSagaDatabaseProvider;

    Provider<FirebaseRemoteConfig> provideFirebaseRemoteConfigProvider;

    Provider<ImageLoader> provideImageLoaderProvider;

    Provider<GenreReferenceHelper> providesReferenceHelperProvider;

    Provider<RemoteConfigService> providesRemoteConfigServiceProvider;

    Provider<GemmaClient> providesSummarizationClientProvider;

    Provider<ImageCropHelper> provideImageCropHelperProvider;

    Provider<ImagenClient> provideImagenClientProvider;

    Provider<TextGenClient> providesTextGenClientProvider;

    Provider<FileCacheService> bindsFileCacheServiceProvider;

    Provider<AppLifecycleManagerImpl> appLifecycleManagerImplProvider;

    Provider<MediaPlayerManager> provideMediaPlayerManagerProvider;

    Provider<PermissionService> providesPermissionServiceProvider;

    Provider<FileManager> providesFileManagerProvider;

    Provider<MediaNotificationManagerImpl> mediaNotificationManagerImplProvider;

    Provider<Gson> provideGsonProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    DatabaseBuilder databaseBuilder() {
      return new DatabaseBuilder(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideBillingServiceProvider = DoubleCheck.provider(new SwitchingProvider<BillingService>(singletonCImpl, 0));
      this.provideDataStorePreferencesProvider = DoubleCheck.provider(new SwitchingProvider<DataStorePreferences>(singletonCImpl, 2));
      this.bindsFileHelperProvider = DoubleCheck.provider(new SwitchingProvider<FileHelper>(singletonCImpl, 3));
      this.providesBackupServiceProvider = DoubleCheck.provider(new SwitchingProvider<BackupService>(singletonCImpl, 1));
      this.provideSagaDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<SagaDatabase>(singletonCImpl, 4));
      this.provideFirebaseRemoteConfigProvider = DoubleCheck.provider(new SwitchingProvider<FirebaseRemoteConfig>(singletonCImpl, 6));
      this.provideImageLoaderProvider = DoubleCheck.provider(new SwitchingProvider<ImageLoader>(singletonCImpl, 7));
      this.providesReferenceHelperProvider = DoubleCheck.provider(new SwitchingProvider<GenreReferenceHelper>(singletonCImpl, 5));
      this.providesRemoteConfigServiceProvider = DoubleCheck.provider(new SwitchingProvider<RemoteConfigService>(singletonCImpl, 9));
      this.providesSummarizationClientProvider = DoubleCheck.provider(new SwitchingProvider<GemmaClient>(singletonCImpl, 8));
      this.provideImageCropHelperProvider = DoubleCheck.provider(new SwitchingProvider<ImageCropHelper>(singletonCImpl, 10));
      this.provideImagenClientProvider = DoubleCheck.provider(new SwitchingProvider<ImagenClient>(singletonCImpl, 11));
      this.providesTextGenClientProvider = DoubleCheck.provider(new SwitchingProvider<TextGenClient>(singletonCImpl, 12));
      this.bindsFileCacheServiceProvider = DoubleCheck.provider(new SwitchingProvider<FileCacheService>(singletonCImpl, 13));
      this.appLifecycleManagerImplProvider = DoubleCheck.provider(new SwitchingProvider<AppLifecycleManagerImpl>(singletonCImpl, 14));
      this.provideMediaPlayerManagerProvider = DoubleCheck.provider(new SwitchingProvider<MediaPlayerManager>(singletonCImpl, 15));
      this.providesPermissionServiceProvider = DoubleCheck.provider(new SwitchingProvider<PermissionService>(singletonCImpl, 16));
      this.providesFileManagerProvider = DoubleCheck.provider(new SwitchingProvider<FileManager>(singletonCImpl, 17));
      this.mediaNotificationManagerImplProvider = DoubleCheck.provider(new SwitchingProvider<MediaNotificationManagerImpl>(singletonCImpl, 18));
      this.provideGsonProvider = DoubleCheck.provider(new SwitchingProvider<Gson>(singletonCImpl, 19));
    }

    @Override
    public void injectSagaApp(SagaApp sagaApp) {
      injectSagaApp2(sagaApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private SagaApp injectSagaApp2(SagaApp instance) {
      SagaApp_MembersInjector.injectBillingService(instance, provideBillingServiceProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.ilustris.sagai.core.services.BillingService
          return (T) AppModule_ProvideBillingServiceFactory.provideBillingService(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.ilustris.sagai.core.file.BackupService
          return (T) AppModule_ProvidesBackupServiceFactory.providesBackupService(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideDataStorePreferencesProvider.get(), singletonCImpl.bindsFileHelperProvider.get());

          case 2: // com.ilustris.sagai.core.datastore.DataStorePreferences
          return (T) AppModule_ProvideDataStorePreferencesFactory.provideDataStorePreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.ilustris.sagai.core.file.FileHelper
          return (T) AppModule_BindsFileHelperFactory.bindsFileHelper(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.ilustris.sagai.core.database.SagaDatabase
          return (T) AppModule_ProvideSagaDatabaseFactory.provideSagaDatabase(singletonCImpl.databaseBuilder());

          case 5: // com.ilustris.sagai.core.file.GenreReferenceHelper
          return (T) AppModule_ProvidesReferenceHelperFactory.providesReferenceHelper(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideFirebaseRemoteConfigProvider.get(), singletonCImpl.provideImageLoaderProvider.get());

          case 6: // com.google.firebase.remoteconfig.FirebaseRemoteConfig
          return (T) AppModule_ProvideFirebaseRemoteConfigFactory.provideFirebaseRemoteConfig();

          case 7: // coil3.ImageLoader
          return (T) AppModule_ProvideImageLoaderFactory.provideImageLoader(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.ilustris.sagai.core.ai.GemmaClient
          return (T) AppModule_ProvidesSummarizationClientFactory.providesSummarizationClient(singletonCImpl.providesRemoteConfigServiceProvider.get());

          case 9: // com.ilustris.sagai.core.services.RemoteConfigService
          return (T) AppModule_ProvidesRemoteConfigServiceFactory.providesRemoteConfigService();

          case 10: // com.ilustris.sagai.core.file.ImageCropHelper
          return (T) AppModule_ProvideImageCropHelperFactory.provideImageCropHelper();

          case 11: // com.ilustris.sagai.core.ai.ImagenClient
          return (T) AppModule_ProvideImagenClientFactory.provideImagenClient(singletonCImpl.providesRemoteConfigServiceProvider.get(), singletonCImpl.provideBillingServiceProvider.get(), singletonCImpl.providesSummarizationClientProvider.get());

          case 12: // com.ilustris.sagai.core.ai.TextGenClient
          return (T) AppModule_ProvidesTextGenClientFactory.providesTextGenClient(singletonCImpl.providesRemoteConfigServiceProvider.get());

          case 13: // com.ilustris.sagai.core.file.FileCacheService
          return (T) AppModule_BindsFileCacheServiceFactory.bindsFileCacheService(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 14: // com.ilustris.sagai.core.lifecycle.AppLifecycleManagerImpl
          return (T) new AppLifecycleManagerImpl();

          case 15: // com.ilustris.sagai.core.media.MediaPlayerManager
          return (T) AppModule_ProvideMediaPlayerManagerFactory.provideMediaPlayerManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 16: // com.ilustris.sagai.core.permissions.PermissionService
          return (T) AppModule_ProvidesPermissionServiceFactory.providesPermissionService(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 17: // com.ilustris.sagai.core.file.FileManager
          return (T) AppModule_ProvidesFileManagerFactory.providesFileManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.bindsFileHelperProvider.get());

          case 18: // com.ilustris.sagai.core.media.notification.MediaNotificationManagerImpl
          return (T) new MediaNotificationManagerImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.bindsFileHelperProvider.get());

          case 19: // com.google.gson.Gson
          return (T) AppModule_ProvideGsonFactory.provideGson();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
