package vyacheslav.pogudin.intervaltimer.di

import androidx.lifecycle.ViewModelProvider
import vyacheslav.pogudin.intervaltimer.ui.load.LoadViewModel

/**
 * Factory для создания LoadViewModel с инъекцией зависимостей
 */
class LoadViewModelFactory : ViewModelProvider.Factory {
    
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoadViewModel(
                loadTimerUseCase = AppContainer.getLoadTimerUseCase()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
