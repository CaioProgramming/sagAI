package com.ilustris.sagai.features.saga.chat.domain.manager;

import android.content.Context;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.core.lifecycle.AppLifecycleManager;
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
public final class ChatNotificationManagerImpl_Factory implements Factory<ChatNotificationManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<FileHelper> fileHelperProvider;

  private final Provider<AppLifecycleManager> appLifecycleManagerProvider;

  public ChatNotificationManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<FileHelper> fileHelperProvider,
      Provider<AppLifecycleManager> appLifecycleManagerProvider) {
    this.contextProvider = contextProvider;
    this.fileHelperProvider = fileHelperProvider;
    this.appLifecycleManagerProvider = appLifecycleManagerProvider;
  }

  @Override
  public ChatNotificationManagerImpl get() {
    return newInstance(contextProvider.get(), fileHelperProvider.get(), appLifecycleManagerProvider.get());
  }

  public static ChatNotificationManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<FileHelper> fileHelperProvider,
      Provider<AppLifecycleManager> appLifecycleManagerProvider) {
    return new ChatNotificationManagerImpl_Factory(contextProvider, fileHelperProvider, appLifecycleManagerProvider);
  }

  public static ChatNotificationManagerImpl newInstance(Context context, FileHelper fileHelper,
      AppLifecycleManager appLifecycleManager) {
    return new ChatNotificationManagerImpl(context, fileHelper, appLifecycleManager);
  }
}
