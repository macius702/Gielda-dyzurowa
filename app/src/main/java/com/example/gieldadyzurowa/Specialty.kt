package com.example.gieldadyzurowa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gieldadyzurowa.network.RetrofitClient
import com.example.gieldadyzurowa.types.Specialty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import android.util.Log

class SpecialtyViewModel : ViewModel() {
    private val _specialties = MutableStateFlow<List<Specialty>>(emptyList())
    val specialties: StateFlow<List<Specialty>> = _specialties

    init {
        fetchSpecialties()
    }

    private fun fetchSpecialties() {
        viewModelScope.launch {
            try {
                Log.d("SpecialtyViewModel", "Fetching specialties...")
                val specialties = RetrofitClient.apiService.getSpecialties(RetrofitClient.BASE_CANISTER)
                Log.d("SpecialtyViewModel", "Specialties: ${specialties.body()}")
                _specialties.value = specialties.body() ?: emptyList()
                Log.d("SpecialtyViewModel", "Specialties: ${specialties.body()}")
            } catch (e: Exception) {
                Log.e("SpecialtyViewModel", "Error fetching specialties", e)
            }
        }
    }
}