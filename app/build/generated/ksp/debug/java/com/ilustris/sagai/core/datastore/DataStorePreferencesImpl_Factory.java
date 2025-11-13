package com.ilustris.sagai.core.datastore;

import android.content.Context;
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
public final class DataStorePreferencesImpl_Factory implements Factory<DataStorePreferencesImpl> {
  private final Provider<Context> contextProvider;

  public DataStorePreferencesImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DataStorePreferencesImpl get() {
    return newInstance(contextProvider.get());
  }

  public static DataStorePreferencesImpl_Factory create(Provider<Context> contextProvider) {
    return new DataStorePreferencesImpl_Factory(contextProvider);
  }

  public static DataStorePreferencesImpl newInstance(Context context) {
    return new DataStorePreferencesImpl(context);
  }
}
