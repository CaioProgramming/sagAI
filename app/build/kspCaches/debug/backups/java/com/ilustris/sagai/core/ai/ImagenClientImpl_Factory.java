package com.ilustris.sagai.core.ai;

import com.ilustris.sagai.core.services.BillingService;
import com.ilustris.sagai.core.services.RemoteConfigService;
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
public final class ImagenClientImpl_Factory implements Factory<ImagenClientImpl> {
  private final Provider<BillingService> billingServiceProvider;

  private final Provider<RemoteConfigService> remoteConfigServiceProvider;

  private final Provider<GemmaClient> gemmaClientProvider;

  public ImagenClientImpl_Factory(Provider<BillingService> billingServiceProvider,
      Provider<RemoteConfigService> remoteConfigServiceProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    this.billingServiceProvider = billingServiceProvider;
    this.remoteConfigServiceProvider = remoteConfigServiceProvider;
    this.gemmaClientProvider = gemmaClientProvider;
  }

  @Override
  public ImagenClientImpl get() {
    return newInstance(billingServiceProvider.get(), remoteConfigServiceProvider.get(), gemmaClientProvider.get());
  }

  public static ImagenClientImpl_Factory create(Provider<BillingService> billingServiceProvider,
      Provider<RemoteConfigService> remoteConfigServiceProvider,
      Provider<GemmaClient> gemmaClientProvider) {
    return new ImagenClientImpl_Factory(billingServiceProvider, remoteConfigServiceProvider, gemmaClientProvider);
  }

  public static ImagenClientImpl newInstance(BillingService billingService,
      RemoteConfigService remoteConfigService, GemmaClient gemmaClient) {
    return new ImagenClientImpl(billingService, remoteConfigService, gemmaClient);
  }
}
