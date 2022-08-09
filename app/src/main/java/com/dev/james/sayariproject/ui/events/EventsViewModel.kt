package com.dev.james.sayariproject.ui.events

import android.os.Build
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.models.iss.Agency
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.utilities.Event
import com.dev.james.sayariproject.utilities.NetworkResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: BaseMainRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _sState : MutableStateFlow<Event<Boolean>> = MutableStateFlow(Event(false))
    val sState get() = _sState

    private val _chartDataState : MutableStateFlow<List<Float>> = MutableStateFlow(listOf(0f))
    val chartDataState get() = _chartDataState

    private val _eventCountStateFlow : MutableStateFlow<EventsThisMonthFavAgencies> = MutableStateFlow(
        EventsThisMonthFavAgencies(0 , 0)
    )
    val eventCountStateFlow get() = _eventCountStateFlow

    val uiState : StateFlow<UiState>

    val pagingDataFlow : Flow<PagingData<Events>>

    val accept : (UiAction) -> Unit

    init {

        getEventsForAppBar()

        val initialQuery = savedStateHandle.get<String>(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY

        val actionStateFlow = MutableSharedFlow<UiAction>()

        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Search(query = initialQuery)) }

        pagingDataFlow = searches
            .flatMapLatest {
                searchEvents(queryString = it.query)
            }
            .cachedIn(viewModelScope)

        uiState = searches.map { search ->
            UiState(
                query = search.query
            )
        }.stateIn(
           scope = viewModelScope,
            started =  SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UiState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

    }

    fun updateSearchState(boolean : Boolean) = viewModelScope.launch {
        _sState.value = Event(boolean)
    }

    override fun onCleared() {
        super.onCleared()
        savedStateHandle[LAST_SEARCH_QUERY] = uiState.value.query
    }
    private fun searchEvents(queryString : String?) : Flow<PagingData<Events>> =
        repository.getEvents(queryString).cachedIn(viewModelScope)

    private fun getEventsForAppBar() = viewModelScope.launch {

        when(val response = repository.getEventsTopAppbar()){
            is NetworkResource.Loading -> {
                Log.d("EventsVm", "getEventsHappeningThisMonth: loading... ")
            }
            is NetworkResource.Success -> {
                val events = response.value.results
                //filter through list and get events happening in current month
                getEventsThisMonth(events)
                getEventsChartData(events)
                Log.d("EventsVm", "events: ${events.toString()} ")
            }
            is NetworkResource.Failure -> {
                val error = response.errorBody
                error?.let {
                    Log.d("EventsVm", "getEventsHappeningThisMonth: $error ")
                }
            }
        }

    }

    private fun getEventsChartData(events: List<Events>) {
        val chartData = calculateChartData(events)
        Log.d("EventsVm", "getEventsChartData: chart floats : ${chartData.toString()} from size: ${events.size} ")
        //post value to stateflow
        _chartDataState.value = chartData
    }

    private fun calculateChartData(events: List<Events>): List<Float> {
        val dockingCount = events.filter { it.type.id == EVENT_DOCKING_ID }.size
        val berthingCount = events.filter { it.type.id == EVENT_BERTHING_ID }.size
        val evaCount = events.filter { it.type.id == EVENT_EVA_ID }.size
        val undockingCount = events.filter { it.type.id == EVENT_UNDOCKING_ID }.size
        val landingCount = events.filter { it.type.id == EVENT_SPACECRAFT_LANDING_ID }.size
        val other = events.filter {
            it.type.id != EVENT_SPACECRAFT_LANDING_ID && it.type.id != EVENT_DOCKING_ID
                    &&  it.type.id != EVENT_BERTHING_ID && it.type.id != EVENT_EVA_ID
                    && it.type.id != EVENT_UNDOCKING_ID
        }.size

        return listOf(dockingCount , undockingCount , evaCount , berthingCount ,landingCount ,other).map {
       //    val num =  ((it.toDouble()/events.size)*100).roundToInt()
         //   num.toFloat()
           it.toFloat()
        }
    }

    private suspend fun getEventsThisMonth(events: List<Events>) {
        try{

            val currentMonth = getCurrentMonth().uppercase()

            val fromFavourites = repository.getFavouriteAgencies().map{
                Agency(
                    id = it.id,
                    url = it.url,
                    type = it.type,
                    name = it.name
                )
            }

            Log.d("EventsViewModel", "getEventsThisMonth: saved agencies : $fromFavourites")

            val filteredEvents = events.filter { event ->
                val month = dateFormatter(event.date)
                month == currentMonth
            }

            val filteredEventsForFav = events.filter { event ->
                val month = dateFormatter(event.date)
                month == currentMonth && event.program.isNotEmpty()
            }


          val filteredByFavourites = filteredEventsForFav.filter { event ->
                  event.program[0].agencies.containsAll(fromFavourites) //||
                      //  event.program[1].agencies.containsAll(fromFavourites)
            }


            _eventCountStateFlow.value =
                EventsThisMonthFavAgencies(
                    thisMonth = filteredEvents.size ,
                    favouriteAgencies =  filteredByFavourites.size
                )


        }catch (e : Exception){

            Log.e("EventsViewModel", "getEventsThisMonth: ${e.localizedMessage}" )

        }


    }

    private fun getCurrentMonth(): String {
        val date = Calendar.getInstance().time
        return date.toString("MMMM")
    }

    private fun dateFormatter(date: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // only for OREO and newer versions
            val dateFormat = ZonedDateTime.parse(date)

            val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))

            return createdDateFormatted.month.toString()

        }else {
            val dateFormat: SimpleDateFormat =
                SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss")
            val eDate: Date = dateFormat.parse(date)

            return eDate.month.toString()
        }
    }

    private fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }
}

data class EventsThisMonthFavAgencies(
    val thisMonth : Int ,
    val favouriteAgencies : Int
)

sealed class UiAction {
    data class Search(val query : String) : UiAction()
}

data class UiState(
    val query : String = DEFAULT_QUERY,
    val pagingData : PagingData<Events> = PagingData.empty()
)
private const val LAST_SEARCH_QUERY = "last_search_query"
private const val  DEFAULT_QUERY = ""
private const val EVENT_DOCKING_ID = 2
private const val EVENT_BERTHING_ID = 4
private const val EVENT_EVA_ID = 3
private const val EVENT_UNDOCKING_ID = 8
private const val EVENT_SPACECRAFT_LANDING_ID = 9

