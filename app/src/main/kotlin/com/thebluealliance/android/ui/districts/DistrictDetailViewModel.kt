package com.thebluealliance.android.ui.districts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebluealliance.android.data.repository.DistrictRepository
import com.thebluealliance.android.data.repository.EventRepository
import com.thebluealliance.android.domain.model.District
import com.thebluealliance.android.navigation.Screen
import com.thebluealliance.android.ui.events.buildEventSections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = DistrictDetailViewModel.Factory::class)
class DistrictDetailViewModel @AssistedInject constructor(
    @Assisted val navKey: Screen.DistrictDetail,
    private val districtRepository: DistrictRepository,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val initialDistrictKey: String = navKey.districtKey
    private val initialDistrictAbbreviation: String = initialDistrictKey.drop(4)
    private val initialYear: Int = initialDistrictKey.take(4).toInt()

    private val _selectedDistrictKey = MutableStateFlow(initialDistrictKey)

    val selectedYear: StateFlow<Int> = _selectedDistrictKey
        .map { it.take(4).toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialYear)

    private val _availableDistricts = MutableStateFlow<List<District>>(emptyList())
    val availableYears: StateFlow<List<Int>> = _availableDistricts
        .map { districts -> districts.map { it.year } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<DistrictDetailUiState> = combine(
        _selectedDistrictKey.flatMapLatest { key ->
            districtRepository.observeDistrict(key)
        },
        _selectedDistrictKey.flatMapLatest { key ->
            eventRepository.observeDistrictEvents(key)
        },
        _selectedDistrictKey.flatMapLatest { key ->
            districtRepository.observeDistrictRankings(key)
        },
        _availableDistricts,
        _selectedDistrictKey,
    ) { district, events, rankings, available, key ->
        DistrictDetailUiState(
            district = district ?: available.find { it.key == key },
            eventSections = if (events.isEmpty()) null else buildEventSections(events),
            rankings = rankings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DistrictDetailUiState())

    init {
        fetchAvailableYears()
        refreshAll()
    }

    private fun fetchAvailableYears() {
        viewModelScope.launch {
            try {
                val districts = districtRepository.getDistrictHistory(initialDistrictAbbreviation)
                _availableDistricts.value = districts
            } catch (_: Exception) { }
        }
    }

    fun selectYear(year: Int) {
        val district = _availableDistricts.value.find { it.year == year } ?: return
        if (_selectedDistrictKey.value == district.key) return
        _selectedDistrictKey.value = district.key
        viewModelScope.launch { refreshYearData() }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                refreshYearData()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun refreshYearData() {
        val key = _selectedDistrictKey.value
        val year = key.take(4).toInt()
        coroutineScope {
            launch { try { districtRepository.refreshDistrictsForYear(year) } catch (_: Exception) { } }
            launch { try { eventRepository.refreshDistrictEvents(key) } catch (_: Exception) { } }
            launch { try { districtRepository.refreshDistrictRankings(key) } catch (_: Exception) { } }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: Screen.DistrictDetail): DistrictDetailViewModel
    }
}
