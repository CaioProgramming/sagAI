package com.ilustris.sagai.features.characters.data.usecase;

import android.content.Context;
import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.ai.ImagenClient;
import com.ilustris.sagai.core.ai.TextGenClient;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.core.file.GenreReferenceHelper;
import com.ilustris.sagai.core.file.ImageCropHelper;
import com.ilustris.sagai.core.services.BillingService;
import com.ilustris.sagai.features.characters.events.data.repository.CharacterEventRepository;
import com.ilustris.sagai.features.characters.relations.data.usecase.CharacterRelationUseCase;
import com.ilustris.sagai.features.characters.repository.CharacterRepository;
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
public final class CharacterUseCaseImpl_Factory implements Factory<CharacterUseCaseImpl> {
  private final Provider<CharacterRepository> repositoryProvider;

  private final Provider<CharacterEventRepository> eventsRepositoryProvider;

  private final Provider<CharacterRelationUseCase> characterRelationUseCaseProvider;

  private final Provider<ImagenClient> imagenClientProvider;

  private final Provider<TextGenClient> textGenClientProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  private final Provider<FileHelper> fileHelperProvider;

  private final Provider<ImageCropHelper> imageCropHelperProvider;

  private final Provider<GenreReferenceHelper> genreReferenceHelperProvider;

  private final Provider<BillingService> billingServiceProvider;

  private final Provider<Context> contextProvider;

  public CharacterUseCaseImpl_Factory(Provider<CharacterRepository> repositoryProvider,
      Provider<CharacterEventRepository> eventsRepositoryProvider,
      Provider<CharacterRelationUseCase> characterRelationUseCaseProvider,
      Provider<ImagenClient> imagenClientProvider, Provider<TextGenClient> textGenClientProvider,
      Provider<GemmaClient> gemmaClientProvider, Provider<FileHelper> fileHelperProvider,
      Provider<ImageCropHelper> imageCropHelperProvider,
      Provider<GenreReferenceHelper> genreReferenceHelperProvider,
      Provider<BillingService> billingServiceProvider, Provider<Context> contextProvider) {
    this.repositoryProvider = repositoryProvider;
    this.eventsRepositoryProvider = eventsRepositoryProvider;
    this.characterRelationUseCaseProvider = characterRelationUseCaseProvider;
    this.imagenClientProvider = imagenClientProvider;
    this.textGenClientProvider = textGenClientProvider;
    this.gemmaClientProvider = gemmaClientProvider;
    this.fileHelperProvider = fileHelperProvider;
    this.imageCropHelperProvider = imageCropHelperProvider;
    this.genreReferenceHelperProvider = genreReferenceHelperProvider;
    this.billingServiceProvider = billingServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public CharacterUseCaseImpl get() {
    return newInstance(repositoryProvider.get(), eventsRepositoryProvider.get(), characterRelationUseCaseProvider.get(), imagenClientProvider.get(), textGenClientProvider.get(), gemmaClientProvider.get(), fileHelperProvider.get(), imageCropHelperProvider.get(), genreReferenceHelperProvider.get(), billingServiceProvider.get(), contextProvider.get());
  }

  public static CharacterUseCaseImpl_Factory create(
      Provider<CharacterRepository> repositoryProvider,
      Provider<CharacterEventRepository> eventsRepositoryProvider,
      Provider<CharacterRelationUseCase> characterRelationUseCaseProvider,
      Provider<ImagenClient> imagenClientProvider, Provider<TextGenClient> textGenClientProvider,
      Provider<GemmaClient> gemmaClientProvider, Provider<FileHelper> fileHelperProvider,
      Provider<ImageCropHelper> imageCropHelperProvider,
      Provider<GenreReferenceHelper> genreReferenceHelperProvider,
      Provider<BillingService> billingServiceProvider, Provider<Context> contextProvider) {
    return new CharacterUseCaseImpl_Factory(repositoryProvider, eventsRepositoryProvider, characterRelationUseCaseProvider, imagenClientProvider, textGenClientProvider, gemmaClientProvider, fileHelperProvider, imageCropHelperProvider, genreReferenceHelperProvider, billingServiceProvider, contextProvider);
  }

  public static CharacterUseCaseImpl newInstance(CharacterRepository repository,
      CharacterEventRepository eventsRepository, CharacterRelationUseCase characterRelationUseCase,
      ImagenClient imagenClient, TextGenClient textGenClient, GemmaClient gemmaClient,
      FileHelper fileHelper, ImageCropHelper imageCropHelper,
      GenreReferenceHelper genreReferenceHelper, BillingService billingService, Context context) {
    return new CharacterUseCaseImpl(repository, eventsRepository, characterRelationUseCase, imagenClient, textGenClient, gemmaClient, fileHelper, imageCropHelper, genreReferenceHelper, billingService, context);
  }
}
