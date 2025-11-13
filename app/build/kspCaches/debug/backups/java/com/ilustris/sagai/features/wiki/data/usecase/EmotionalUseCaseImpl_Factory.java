package com.ilustris.sagai.features.wiki.data.usecase;

import com.ilustris.sagai.core.ai.GemmaClient;
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
public final class EmotionalUseCaseImpl_Factory implements Factory<EmotionalUseCaseImpl> {
  private final Provider<GemmaClient> gemmaClientProvider;

  public EmotionalUseCaseImpl_Factory(Provider<GemmaClient> gemmaClientProvider) {
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public EmotionalUseCaseImpl get() {
    return newInstance(gemmaClientProvider.get());
  }

  public static EmotionalUseCaseImpl_Factory create(Provider<GemmaClient> gemmaClientProvider) {
    return new EmotionalUseCaseImpl_Factory(gemmaClientProvider);
  }

  public static EmotionalUseCaseImpl newInstance(GemmaClient gemmaClient) {
    return new EmotionalUseCaseImpl(gemmaClient);
  }
}
