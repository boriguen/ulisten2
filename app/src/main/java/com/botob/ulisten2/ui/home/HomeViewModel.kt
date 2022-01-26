package com.botob.ulisten2.ui.home

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.botob.ulisten2.media.Media
import com.botob.ulisten2.preferences.SettingsManager

// TODO: remove the AndroidViewModel dependency by either using a ViewModelFactory or dependency injection.
class HomeViewModel(application: Application) : AndroidViewModel(application),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val settingsManager = SettingsManager(application)

    private val _checked = MutableLiveData<Boolean>().apply {
        value = settingsManager.playServiceEnabled
    }
    val checked: LiveData<Boolean> = _checked

    val medias = MutableLiveData<MutableList<Media>>(mutableListOf())

    init {
        settingsManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCleared() {
        settingsManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onCleared()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SettingsManager.PLAY_SERVICE_ENABLED) {
            _checked.value = settingsManager.playServiceEnabled
        }
    }

    /**
     * Adds the given media to the store if not already added.
     *
     * @param media the media to add to the list.
     */
    fun addMedia(media: Media) {
        medias.value?.let {
            if (!it.contains(media)) {
                it.add(0, media)
            }
            medias.postValue(it)
        }
    }
}