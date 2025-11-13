package com.ilustris.sagai.di;

import com.ilustris.sagai.core.file.ImageCropHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideImageCropHelperFactory implements Factory<ImageCropHelper> {
  @Override
  public ImageCropHelper get() {
    return provideImageCropHelper();
  }

  public static AppModule_ProvideImageCropHelperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ImageCropHelper provideImageCropHelper() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideImageCropHelper());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideImageCropHelperFactory INSTANCE = new AppModule_ProvideImageCropHelperFactory();
  }
}
