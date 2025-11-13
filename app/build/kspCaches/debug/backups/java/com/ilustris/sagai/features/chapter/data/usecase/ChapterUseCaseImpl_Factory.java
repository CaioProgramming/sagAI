package com.ilustris.sagai.features.chapter.data.usecase;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.ai.ImagenClient;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.core.file.GenreReferenceHelper;
import com.ilustris.sagai.features.chapter.data.repository.ChapterRepository;
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository;
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase;
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
public final class ChapterUseCaseImpl_Factory implements Factory<ChapterUseCaseImpl> {
  private final Provider<ChapterRepository> chapterRepositoryProvider;

  private final Provider<TimelineRepository> timelineRepositoryProvider;

  private final Provider<WikiUseCase> wikiUseCaseProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  private final Provider<ImagenClient> imagenClientProvider;

  private final Provider<FileHelper> fileHelperProvider;

  private final Provider<GenreReferenceHelper> genreReferenceHelperProvider;

  public ChapterUseCaseImpl_Factory(Provider<ChapterRepository> chapterRepositoryProvider,
      Provider<TimelineRepository> timelineRepositoryProvider,
      Provider<WikiUseCase> wikiUseCaseProvider, Provider<GemmaClient> gemmaClientProvider,
      Provider<ImagenClient> imagenClientProvider, Provider<FileHelper> fileHelperProvider,
      Provider<GenreReferenceHelper> genreReferenceHelperProvider) {
    this.chapterRepositoryProvider = chapterRepositoryProvider;
    this.timelineRepositoryProvider = timelineRepositoryProvider;
    this.wikiUseCaseProvider = wikiUseCaseProvider;
    this.gemmaClientProvider = gemmaClientProvider;
    this.imagenClientProvider = imagenClientProvider;
    this.fileHelperProvider = fileHelperProvider;
    this.genreReferenceHelperProvider = genreReferenceHelperProvider;
  }

  @Override
  public ChapterUseCaseImpl get() {
    return newInstance(chapterRepositoryProvider.get(), timelineRepositoryProvider.get(), wikiUseCaseProvider.get(), gemmaClientProvider.get(), imagenClientProvider.get(), fileHelperProvider.get(), genreReferenceHelperProvider.get());
  }

  public static ChapterUseCaseImpl_Factory create(
      Provider<ChapterRepository> chapterRepositoryProvider,
      Provider<TimelineRepository> timelineRepositoryProvider,
      Provider<WikiUseCase> wikiUseCaseProvider, Provider<GemmaClient> gemmaClientProvider,
      Provider<ImagenClient> imagenClientProvider, Provider<FileHelper> fileHelperProvider,
      Provider<GenreReferenceHelper> genreReferenceHelperProvider) {
    return new ChapterUseCaseImpl_Factory(chapterRepositoryProvider, timelineRepositoryProvider, wikiUseCaseProvider, gemmaClientProvider, imagenClientProvider, fileHelperProvider, genreReferenceHelperProvider);
  }

  public static ChapterUseCaseImpl newInstance(ChapterRepository chapterRepository,
      TimelineRepository timelineRepository, WikiUseCase wikiUseCase, GemmaClient gemmaClient,
      ImagenClient imagenClient, FileHelper fileHelper, GenreReferenceHelper genreReferenceHelper) {
    return new ChapterUseCaseImpl(chapterRepository, timelineRepository, wikiUseCase, gemmaClient, imagenClient, fileHelper, genreReferenceHelper);
  }
}
