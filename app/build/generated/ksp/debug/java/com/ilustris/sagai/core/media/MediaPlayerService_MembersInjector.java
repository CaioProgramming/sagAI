package com.ilustris.sagai.core.media;

import com.google.gson.Gson;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.core.media.notification.MediaNotificationManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MediaPlayerService_MembersInjector implements MembersInjector<MediaPlayerService> {
  private final Provider<MediaPlayerManager> mediaPlayerManagerProvider;

  private final Provider<MediaNotificationManager> notificationManagerProvider;

  private final Provider<Gson> gsonProvider;

  private final Provider<FileHelper> fileHelperProvider;

  public MediaPlayerService_MembersInjector(Provider<MediaPlayerManager> mediaPlayerManagerProvider,
      Provider<MediaNotificationManager> notificationManagerProvider, Provider<Gson> gsonProvider,
      Provider<FileHelper> fileHelperProvider) {
    this.mediaPlayerManagerProvider = mediaPlayerManagerProvider;
    this.notificationManagerProvider = notificationManagerProvider;
    this.gsonProvider = gsonProvider;
    this.fileHelperProvider = fileHelperProvider;
  }

  public static MembersInjector<MediaPlayerService> create(
      Provider<MediaPlayerManager> mediaPlayerManagerProvider,
      Provider<MediaNotificationManager> notificationManagerProvider, Provider<Gson> gsonProvider,
      Provider<FileHelper> fileHelperProvider) {
    return new MediaPlayerService_MembersInjector(mediaPlayerManagerProvider, notificationManagerProvider, gsonProvider, fileHelperProvider);
  }

  @Override
  public void injectMembers(MediaPlayerService instance) {
    injectMediaPlayerManager(instance, mediaPlayerManagerProvider.get());
    injectNotificationManager(instance, notificationManagerProvider.get());
    injectGson(instance, gsonProvider.get());
    injectFileHelper(instance, fileHelperProvider.get());
  }

  @InjectedFieldSignature("com.ilustris.sagai.core.media.MediaPlayerService.mediaPlayerManager")
  public static void injectMediaPlayerManager(MediaPlayerService instance,
      MediaPlayerManager mediaPlayerManager) {
    instance.mediaPlayerManager = mediaPlayerManager;
  }

  @InjectedFieldSignature("com.ilustris.sagai.core.media.MediaPlayerService.notificationManager")
  public static void injectNotificationManager(MediaPlayerService instance,
      MediaNotificationManager notificationManager) {
    instance.notificationManager = notificationManager;
  }

  @InjectedFieldSignature("com.ilustris.sagai.core.media.MediaPlayerService.gson")
  public static void injectGson(MediaPlayerService instance, Gson gson) {
    instance.gson = gson;
  }

  @InjectedFieldSignature("com.ilustris.sagai.core.media.MediaPlayerService.fileHelper")
  public static void injectFileHelper(MediaPlayerService instance, FileHelper fileHelper) {
    instance.fileHelper = fileHelper;
  }
}
