/* ui/main/MainViewModel.kt */
package com.example.exchangingprivatelessons.ui.main

import androidx.lifecycle.ViewModel
import com.example.exchangingprivatelessons.common.di.ApplicationScope
import com.example.exchangingprivatelessons.common.util.toResultLiveData
import com.example.exchangingprivatelessons.domain.usecase.user.ObserveUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    observeUser: ObserveUser,
    @ApplicationScope appScope: CoroutineScope          // scope גלובלי
) : ViewModel() {

    init {
        /* המרה ל‑LiveData + observeForever ➜ מאזין חי לאורך כל חיי‑האפליקציה */
        observeUser()                                   // Flow<Result<User>>
            .toResultLiveData(appScope.coroutineContext)
            .observeForever { /* no‑op */ }
    }
}
