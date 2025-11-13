package com.ilustris.sagai.features.wiki.data.usecase;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.features.wiki.data.repository.WikiRepository;
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
public final class WikiUseCaseImpl_Factory implements Factory<WikiUseCaseImpl> {
  private final Provider<WikiRepository> wikiRepositoryProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public WikiUseCaseImpl_Factory(Provider<WikiRepository> wikiRepositoryProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    this.wikiRepositoryProvider = wikiRepositoryProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public WikiUseCaseImpl get() {
    return newInstance(wikiRepositoryProvider.get(), gemmaClientProvider.get());
  }

  public static WikiUseCaseImpl_Factory create(Provider<WikiRepository> wikiRepositoryProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    return new WikiUseCaseImpl_Factory(wikiRepositoryProvider, gemmaClientProvider);
  }

  public static WikiUseCaseImpl newInstance(WikiRepository wikiRepository,
      GemmaClient gemmaClient) {
    return new WikiUseCaseImpl(wikiRepository, gemmaClient);
  }
}
