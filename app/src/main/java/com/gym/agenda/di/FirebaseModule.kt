package com.gym.agenda.di

import com.gym.agenda.data.firebase.AuthService
import com.gym.agenda.data.firebase.FirestoreService
import com.gym.agenda.data.repository.AppointmentRepository
import com.gym.agenda.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            firestoreSettings = firestoreSettings {
                isPersistenceEnabled = true
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthService {
        return AuthService(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreService(
        firestore: FirebaseFirestore
    ): FirestoreService {
        return FirestoreService(firestore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authService: AuthService,
        firestoreService: FirestoreService
    ): AuthRepository {
        return AuthRepository(authService, firestoreService)
    }

    @Provides
    @Singleton
    fun provideAppointmentRepository(
        firestoreService: FirestoreService
    ): AppointmentRepository {
        return AppointmentRepository(firestoreService)
    }
}