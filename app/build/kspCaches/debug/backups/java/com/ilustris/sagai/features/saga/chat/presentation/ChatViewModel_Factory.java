package com.ilustris.sagai.features.saga.chat.presentation;

import android.content.Context;
import com.ilustris.sagai.core.media.MediaPlayerManager;
import com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManager;
import com.ilustris.sagai.features.saga.chat.data.usecase.GetInputSuggestionsUseCase;
import com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase;
import com.ilustris.sagai.features.saga.chat.domain.manager.ChatNotificationManager;
import com.ilustris.sagai.features.settings.domain.SettingsUseCase;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<MessageUseCase> messageUseCaseProvider;

  private final Provider<SagaContentManager> sagaContentManagerProvider;

  private final Provider<GetInputSuggestionsUseCase> suggestionUseCaseProvider;

  private final Provider<ChatNotificationManager> notificationManagerProvider;

  private final Provider<MediaPlayerManager> mediaPlayerManagerProvider;

  private final Provider<SettingsUseCase> settingsUseCaseProvider;

  public ChatViewModel_Factory(Provider<Context> contextProvider,
      Provider<MessageUseCase> messageUseCaseProvider,
      Provider<SagaContentManager> sagaContentManagerProvider,
      Provider<GetInputSuggestionsUseCase> suggestionUseCaseProvider,
      Provider<ChatNotificationManager> notificationManagerProvider,
      Provider<MediaPlayerManager> mediaPlayerManagerProvider,
      Provider<SettingsUseCase> settingsUseCaseProvider) {
    this.contextProvider = contextProvider;
    this.messageUseCaseProvider = messageUseCaseProvider;
    this.sagaContentManagerProvider = sagaContentManagerProvider;
    this.suggestionUseCaseProvider = suggestionUseCaseProvider;
    this.notificationManagerProvider = notificationManagerProvider;
    this.mediaPlayerManagerProvider = mediaPlayerManagerProvider;
    this.settingsUseCaseProvider = settingsUseCaseProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(contextProvider.get(), messageUseCaseProvider.get(), sagaContentManagerProvider.get(), suggestionUseCaseProvider.get(), notificationManagerProvider.get(), mediaPlayerManagerProvider.get(), settingsUseCaseProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<Context> contextProvider,
      Provider<MessageUseCase> messageUseCaseProvider,
      Provider<SagaContentManager> sagaContentManagerProvider,
      Provider<GetInputSuggestionsUseCase> suggestionUseCaseProvider,
      Provider<ChatNotificationManager> notificationManagerProvider,
      Provider<MediaPlayerManager> mediaPlayerManagerProvider,
      Provider<SettingsUseCase> settingsUseCaseProvider) {
    return new ChatViewModel_Factory(contextProvider, messageUseCaseProvider, sagaContentManagerProvider, suggestionUseCaseProvider, notificationManagerProvider, mediaPlayerManagerProvider, settingsUseCaseProvider);
  }

  public static ChatViewModel newInstance(Context context, MessageUseCase messageUseCase,
      SagaContentManager sagaContentManager, GetInputSuggestionsUseCase suggestionUseCase,
      ChatNotificationManager notificationManager, MediaPlayerManager mediaPlayerManager,
      SettingsUseCase settingsUseCase) {
    return new ChatViewModel(context, messageUseCase, sagaContentManager, suggestionUseCase, notificationManager, mediaPlayerManager, settingsUseCase);
  }
}
