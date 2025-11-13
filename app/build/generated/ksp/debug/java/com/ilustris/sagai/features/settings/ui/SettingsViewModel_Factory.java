package com.ilustris.sagai.features.settings.ui;

import com.ilustris.sagai.features.settings.domain.SettingsUseCase;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsUseCase> settingsUseCaseProvider;

  public SettingsViewModel_Factory(Provider<SettingsUseCase> settingsUseCaseProvider) {
    this.settingsUseCaseProvider = settingsUseCaseProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsUseCaseProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsUseCase> settingsUseCaseProvider) {
    return new SettingsViewModel_Factory(settingsUseCaseProvider);
  }

  public static SettingsViewModel newInstance(SettingsUseCase settingsUseCase) {
    return new SettingsViewModel(settingsUseCase);
  }
}
