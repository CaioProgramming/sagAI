package com.ilustris.sagai.core.database;

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
public final class DatabaseBuilder_Factory implements Factory<DatabaseBuilder> {
  private final Provider<Context> contextProvider;

  public DatabaseBuilder_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DatabaseBuilder get() {
    return newInstance(contextProvider.get());
  }

  public static DatabaseBuilder_Factory create(Provider<Context> contextProvider) {
    return new DatabaseBuilder_Factory(contextProvider);
  }

  public static DatabaseBuilder newInstance(Context context) {
    return new DatabaseBuilder(context);
  }
}
