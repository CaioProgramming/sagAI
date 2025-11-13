package com.ilustris.sagai.features.share.domain;

import android.content.Context;
import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.file.FileCacheService;
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
public final class SharePlayUseCaseImpl_Factory implements Factory<SharePlayUseCaseImpl> {
  private final Provider<FileCacheService> fileHelperProvider;

  private final Provider<Context> contextProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public SharePlayUseCaseImpl_Factory(Provider<FileCacheService> fileHelperProvider,
      Provider<Context> contextProvider, Provider<GemmaClient> gemmaClientProvider) {
    this.fileHelperProvider = fileHelperProvider;
    this.contextProvider = contextProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public SharePlayUseCaseImpl get() {
    return newInstance(fileHelperProvider.get(), contextProvider.get(), gemmaClientProvider.get());
  }

  public static SharePlayUseCaseImpl_Factory create(Provider<FileCacheService> fileHelperProvider,
      Provider<Context> contextProvider, Provider<GemmaClient> gemmaClientProvider) {
    return new SharePlayUseCaseImpl_Factory(fileHelperProvider, contextProvider, gemmaClientProvider);
  }

  public static SharePlayUseCaseImpl newInstance(FileCacheService fileHelper, Context context,
      GemmaClient gemmaClient) {
    return new SharePlayUseCaseImpl(fileHelper, context, gemmaClient);
  }
}
