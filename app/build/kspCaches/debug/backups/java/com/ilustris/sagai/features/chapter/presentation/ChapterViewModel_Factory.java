package com.ilustris.sagai.features.chapter.presentation;

import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase;
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase;
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
public final class ChapterViewModel_Factory implements Factory<ChapterViewModel> {
  private final Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider;

  private final Provider<ChapterUseCase> chapterUseCaseProvider;

  public ChapterViewModel_Factory(Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider,
      Provider<ChapterUseCase> chapterUseCaseProvider) {
    this.sagaHistoryUseCaseProvider = sagaHistoryUseCaseProvider;
    this.chapterUseCaseProvider = chapterUseCaseProvider;
  }

  @Override
  public ChapterViewModel get() {
    return newInstance(sagaHistoryUseCaseProvider.get(), chapterUseCaseProvider.get());
  }

  public static ChapterViewModel_Factory create(
      Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider,
      Provider<ChapterUseCase> chapterUseCaseProvider) {
    return new ChapterViewModel_Factory(sagaHistoryUseCaseProvider, chapterUseCaseProvider);
  }

  public static ChapterViewModel newInstance(SagaHistoryUseCase sagaHistoryUseCase,
      ChapterUseCase chapterUseCase) {
    return new ChapterViewModel(sagaHistoryUseCase, chapterUseCase);
  }
}
