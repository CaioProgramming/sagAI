package com.ilustris.sagai.core.media.notification;

import android.content.Context;
import com.ilustris.sagai.core.file.FileHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class MediaNotificationManagerImpl_Factory implements Factory<MediaNotificationManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<FileHelper> fileHelperProvider;

  public MediaNotificationManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<FileHelper> fileHelperProvider) {
    this.contextProvider = contextProvider;
    this.fileHelperProvider = fileHelperProvider;
  }

  @Override
  public MediaNotificationManagerImpl get() {
    return newInstance(contextProvider.get(), fileHelperProvider.get());
  }

  public static MediaNotificationManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<FileHelper> fileHelperProvider) {
    return new MediaNotificationManagerImpl_Factory(contextProvider, fileHelperProvider);
  }

  public static MediaNotificationManagerImpl newInstance(Context context, FileHelper fileHelper) {
    return new MediaNotificationManagerImpl(context, fileHelper);
  }
}
