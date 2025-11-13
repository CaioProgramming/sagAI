package com.ilustris.sagai.features.act.data.usecase;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.features.act.data.repository.ActRepository;
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
public final class ActUseCaseImpl_Factory implements Factory<ActUseCaseImpl> {
  private final Provider<ActRepository> actRepositoryProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public ActUseCaseImpl_Factory(Provider<ActRepository> actRepositoryProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    this.actRepositoryProvider = actRepositoryProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public ActUseCaseImpl get() {
    return newInstance(actRepositoryProvider.get(), gemmaClientProvider.get());
  }

  public static ActUseCaseImpl_Factory create(Provider<ActRepository> actRepositoryProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    return new ActUseCaseImpl_Factory(actRepositoryProvider, gemmaClientProvider);
  }

  public static ActUseCaseImpl newInstance(ActRepository actRepository, GemmaClient gemmaClient) {
    return new ActUseCaseImpl(actRepository, gemmaClient);
  }
}
