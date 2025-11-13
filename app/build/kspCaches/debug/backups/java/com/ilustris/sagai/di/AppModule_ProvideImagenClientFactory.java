package com.ilustris.sagai.di;

import com.ilustris.sagai.core.ai.GemmaClient;
import com.ilustris.sagai.core.ai.ImagenClient;
import com.ilustris.sagai.core.services.BillingService;
import com.ilustris.sagai.core.services.RemoteConfigService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
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
public final class AppModule_ProvideImagenClientFactory implements Factory<ImagenClient> {
  private final Provider<RemoteConfigService> remoteConfigServiceProvider;

  private final Provider<BillingService> billingServiceProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public AppModule_ProvideImagenClientFactory(
      Provider<RemoteConfigService> remoteConfigServiceProvider,
      Provider<BillingService> billingServiceProvider, Provider<GemmaClient> gemmaClientProvider) {
    this.remoteConfigServiceProvider = remoteConfigServiceProvider;
    this.billingServiceProvider = billingServiceProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public ImagenClient get() {
    return provideImagenClient(remoteConfigServiceProvider.get(), billingServiceProvider.get(), gemmaClientProvider.get());
  }

  public static AppModule_ProvideImagenClientFactory create(
      Provider<RemoteConfigService> remoteConfigServiceProvider,
      Provider<BillingService> billingServiceProvider, Provider<GemmaClient> gemmaClientProvider) {
    return new AppModule_ProvideImagenClientFactory(remoteConfigServiceProvider, billingServiceProvider, gemmaClientProvider);
  }

  public static ImagenClient provideImagenClient(RemoteConfigService remoteConfigService,
      BillingService billingService, GemmaClient gemmaClient) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideImagenClient(remoteConfigService, billingService, gemmaClient));
  }
}
