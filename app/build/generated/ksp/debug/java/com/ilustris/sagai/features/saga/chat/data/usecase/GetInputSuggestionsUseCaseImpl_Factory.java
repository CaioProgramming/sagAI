package com.ilustris.sagai.features.saga.chat.data.usecase;

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
public final class GetInputSuggestionsUseCaseImpl_Factory implements Factory<GetInputSuggestionsUseCaseImpl> {
  private final Provider<GemmaClient> gemmaClientProvider;

  public GetInputSuggestionsUseCaseImpl_Factory(Provider<GemmaClient> gemmaClientProvider) {
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public GetInputSuggestionsUseCaseImpl get() {
    return newInstance(gemmaClientProvider.get());
  }

  public static GetInputSuggestionsUseCaseImpl_Factory create(
      Provider<GemmaClient> gemmaClientProvider) {
    return new GetInputSuggestionsUseCaseImpl_Factory(gemmaClientProvider);
  }

  public static GetInputSuggestionsUseCaseImpl newInstance(GemmaClient gemmaClient) {
    return new GetInputSuggestionsUseCaseImpl(gemmaClient);
  }
}
