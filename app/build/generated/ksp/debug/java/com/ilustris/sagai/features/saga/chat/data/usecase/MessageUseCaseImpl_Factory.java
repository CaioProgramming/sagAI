package com.ilustris.sagai.features.saga.chat.data.usecase;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.ai.TextGenClient;
import com.ilustris.sagai.features.saga.chat.repository.MessageRepository;
import com.ilustris.sagai.features.saga.chat.repository.ReactionRepository;
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
public final class MessageUseCaseImpl_Factory implements Factory<MessageUseCaseImpl> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<ReactionRepository> reactionRepositoryProvider;

  private final Provider<TextGenClient> textGenClientProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public MessageUseCaseImpl_Factory(Provider<MessageRepository> messageRepositoryProvider,
      Provider<ReactionRepository> reactionRepositoryProvider,
      Provider<TextGenClient> textGenClientProvider, Provider<GemmaClient> gemmaClientProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.reactionRepositoryProvider = reactionRepositoryProvider;
    this.textGenClientProvider = textGenClientProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public MessageUseCaseImpl get() {
    return newInstance(messageRepositoryProvider.get(), reactionRepositoryProvider.get(), textGenClientProvider.get(), gemmaClientProvider.get());
  }

  public static MessageUseCaseImpl_Factory create(
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<ReactionRepository> reactionRepositoryProvider,
      Provider<TextGenClient> textGenClientProvider, Provider<GemmaClient> gemmaClientProvider) {
    return new MessageUseCaseImpl_Factory(messageRepositoryProvider, reactionRepositoryProvider, textGenClientProvider, gemmaClientProvider);
  }

  public static MessageUseCaseImpl newInstance(MessageRepository messageRepository,
      ReactionRepository reactionRepository, TextGenClient textGenClient, GemmaClient gemmaClient) {
    return new MessageUseCaseImpl(messageRepository, reactionRepository, textGenClient, gemmaClient);
  }
}
