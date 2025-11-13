package com.ilustris.sagai.features.share.presentation;

import com.ilustris.sagai.features.share.domain.SharePlayUseCase;
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
public final class SharePlayViewModel_Factory implements Factory<SharePlayViewModel> {
  private final Provider<SharePlayUseCase> sharePlayUseCaseProvider;

  public SharePlayViewModel_Factory(Provider<SharePlayUseCase> sharePlayUseCaseProvider) {
    this.sharePlayUseCaseProvider = sharePlayUseCaseProvider;
  }

  @Override
  public SharePlayViewModel get() {
    return newInstance(sharePlayUseCaseProvider.get());
  }

  public static SharePlayViewModel_Factory create(
      Provider<SharePlayUseCase> sharePlayUseCaseProvider) {
    return new SharePlayViewModel_Factory(sharePlayUseCaseProvider);
  }

  public static SharePlayViewModel newInstance(SharePlayUseCase sharePlayUseCase) {
    return new SharePlayViewModel(sharePlayUseCase);
  }
}
