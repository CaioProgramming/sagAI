package com.ilustris.sagai.features.milestone.di

import com.ilustris.sagai.features.milestone.domain.MilestoneUseCase
import com.ilustris.sagai.features.milestone.domain.MilestoneUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class MilestoneModule {
    @Binds
    abstract fun bindsMilestoneUseCase(milestoneUseCaseImpl: MilestoneUseCaseImpl): MilestoneUseCase
}
