package com.ilustris.sagai.features.saga.chat.repository;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.ai.ImagenClient;
import com.ilustris.sagai.core.database.SagaDatabase;
import com.ilustris.sagai.core.file.BackupService;
import com.ilustris.sagai.core.file.FileHelper;
import com.ilustris.sagai.core.file.GenreReferenceHelper;
import com.ilustris.sagai.core.file.ImageCropHelper;
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
public final class SagaRepositoryImpl_Factory implements Factory<SagaRepositoryImpl> {
  private final Provider<SagaDatabase> databaseProvider;

  private final Provider<GenreReferenceHelper> genreReferenceHelperProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  private final Provider<ImageCropHelper> imageCropHelperProvider;

  private final Provider<FileHelper> fileHelperProvider;

  private final Provider<ImagenClient> imagenClientProvider;

  private final Provider<BackupService> backupServiceProvider;

  public SagaRepositoryImpl_Factory(Provider<SagaDatabase> databaseProvider,
      Provider<GenreReferenceHelper> genreReferenceHelperProvider,
      Provider<GemmaClient> gemmaClientProvider, Provider<ImageCropHelper> imageCropHelperProvider,
      Provider<FileHelper> fileHelperProvider, Provider<ImagenClient> imagenClientProvider,
      Provider<BackupService> backupServiceProvider) {
    this.databaseProvider = databaseProvider;
    this.genreReferenceHelperProvider = genreReferenceHelperProvider;
    this.gemmaClientProvider = gemmaClientProvider;
    this.imageCropHelperProvider = imageCropHelperProvider;
    this.fileHelperProvider = fileHelperProvider;
    this.imagenClientProvider = imagenClientProvider;
    this.backupServiceProvider = backupServiceProvider;
  }

  @Override
  public SagaRepositoryImpl get() {
    return newInstance(databaseProvider.get(), genreReferenceHelperProvider.get(), gemmaClientProvider.get(), imageCropHelperProvider.get(), fileHelperProvider.get(), imagenClientProvider.get(), backupServiceProvider.get());
  }

  public static SagaRepositoryImpl_Factory create(Provider<SagaDatabase> databaseProvider,
      Provider<GenreReferenceHelper> genreReferenceHelperProvider,
      Provider<GemmaClient> gemmaClientProvider, Provider<ImageCropHelper> imageCropHelperProvider,
      Provider<FileHelper> fileHelperProvider, Provider<ImagenClient> imagenClientProvider,
      Provider<BackupService> backupServiceProvider) {
    return new SagaRepositoryImpl_Factory(databaseProvider, genreReferenceHelperProvider, gemmaClientProvider, imageCropHelperProvider, fileHelperProvider, imagenClientProvider, backupServiceProvider);
  }

  public static SagaRepositoryImpl newInstance(SagaDatabase database,
      GenreReferenceHelper genreReferenceHelper, GemmaClient gemmaClient,
      ImageCropHelper imageCropHelper, FileHelper fileHelper, ImagenClient imagenClient,
      BackupService backupService) {
    return new SagaRepositoryImpl(database, genreReferenceHelper, gemmaClient, imageCropHelper, fileHelper, imagenClient, backupService);
  }
}
