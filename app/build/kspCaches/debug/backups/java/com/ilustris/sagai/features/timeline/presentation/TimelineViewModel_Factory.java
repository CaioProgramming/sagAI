package com.ilustris.sagai.features.timeline.presentation;

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
public final class TimelineViewModel_Factory implements Factory<TimelineViewModel> {
  private final Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider;

  public TimelineViewModel_Factory(Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider) {
    this.sagaHistoryUseCaseProvider = sagaHistoryUseCaseProvider;
  }

  @Override
  public TimelineViewModel get() {
    return newInstance(sagaHistoryUseCaseProvider.get());
  }

  public static TimelineViewModel_Factory create(
      Provider<SagaHistoryUseCase> sagaHistoryUseCaseProvider) {
    return new TimelineViewModel_Factory(sagaHistoryUseCaseProvider);
  }

  public static TimelineViewModel newInstance(SagaHistoryUseCase sagaHistoryUseCase) {
    return new TimelineViewModel(sagaHistoryUseCase);
  }
}
