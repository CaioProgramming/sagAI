package com.ilustris.sagai.core.media;

import android.content.Context;
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
public final class MediaPlayerManagerImpl_Factory implements Factory<MediaPlayerManagerImpl> {
  private final Provider<Context> contextProvider;

  public MediaPlayerManagerImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MediaPlayerManagerImpl get() {
    return newInstance(contextProvider.get());
  }

  public static MediaPlayerManagerImpl_Factory create(Provider<Context> contextProvider) {
    return new MediaPlayerManagerImpl_Factory(contextProvider);
  }

  public static MediaPlayerManagerImpl newInstance(Context context) {
    return new MediaPlayerManagerImpl(context);
  }
}
