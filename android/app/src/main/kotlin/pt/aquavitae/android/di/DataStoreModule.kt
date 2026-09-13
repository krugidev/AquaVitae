package pt.aquavitae.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt separado do [pt.aquavitae.android.data.network.NetworkModule] por
 * responsabilidade: aqui só se fornece o DataStore de Preferences usado para
 * persistir localmente a sessão (token JWT), consumido por
 * [pt.aquavitae.android.data.local.TokenDataStore].
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val AUTH_PREFERENCES_NAME = "aquavitae_auth_prefs"

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(AUTH_PREFERENCES_NAME)
        }
}
