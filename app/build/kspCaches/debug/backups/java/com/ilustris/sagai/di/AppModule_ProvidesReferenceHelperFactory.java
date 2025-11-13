package com.ilustris.sagai.di;

import android.content.Context;
import coil3.ImageLoader;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.ilustris.sagai.core.file.GenreReferenceHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvidesReferenceHelperFactory implements Factory<GenreReferenceHelper> {
  private final Provider<Context> contextProvider;

  private final Provider<FirebaseRemoteConfig> firebaseRemoteConfigProvider;

  private final Provider<ImageLoader> imageLoaderProvider;

  public AppModule_ProvidesReferenceHelperFactory(Provider<Context> contextProvider,
      Provider<FirebaseRemoteConfig> firebaseRemoteConfigProvider,
      Provider<ImageLoader> imageLoaderProvider) {
    this.contextProvider = contextProvider;
    this.firebaseRemoteConfigProvider = firebaseRemoteConfigProvider;
    this.imageLoaderProvider = imageLoaderProvider;
  }

  @Override
  public GenreReferenceHelper get() {
    return providesReferenceHelper(contextProvider.get(), firebaseRemoteConfigProvider.get(), imageLoaderProvider.get());
  }

  public static AppModule_ProvidesReferenceHelperFactory create(Provider<Context> contextProvider,
      Provider<FirebaseRemoteConfig> firebaseRemoteConfigProvider,
      Provider<ImageLoader> imageLoaderProvider) {
    return new AppModule_ProvidesReferenceHelperFactory(contextProvider, firebaseRemoteConfigProvider, imageLoaderProvider);
  }

  public static GenreReferenceHelper providesReferenceHelper(Context context,
      FirebaseRemoteConfig firebaseRemoteConfig, ImageLoader imageLoader) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providesReferenceHelper(context, firebaseRemoteConfig, imageLoader));
  }
}
