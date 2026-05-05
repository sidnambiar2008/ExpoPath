package org.communityday.navigation.events.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searcher: ConferenceSearcher,
    private val repository: EventRepository // 1. Inject the repo
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    // This is the "Master List" from Algolia
    private var rawResults = emptyList<Conference>()

    // This is the "Safe List" for the UI
    var results by mutableStateOf<List<Conference>>(emptyList())
        private set

    var isSearching by mutableStateOf(false)
        private set

    // 2. Convert the Flow of hidden IDs into a State object the ViewModel can use
    private val hiddenIds = repository.getHiddenIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private var searchJob: Job? = null

    init {
        // 1. Trigger the initial search
       // onQueryChange("")

        viewModelScope.launch {
            Firebase.auth.authStateChanged.collect { user ->
                // When account switches, clear everything and re-fetch
                clearResults()
                refresh()
            }
        }

        // 2. Watch for changes in the "Hidden" list
        viewModelScope.launch {
            hiddenIds.collect { currentHiddenSet ->
                // Every time the user hides something OR switches accounts,
                // this triggers and re-filters the current rawResults.
                applyFilter(currentHiddenSet)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            if (newQuery.isNotEmpty()) delay(300)
            isSearching = true
            try {
                // Get fresh results from the searcher
                rawResults = searcher.search(newQuery)
                // Filter them immediately against our blacklist
                applyFilter(hiddenIds.value)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                results = emptyList()
            } finally {
                isSearching = false
            }
        }
    }

    // 4. A function to add a conference to the blacklist
    fun hideConference(confId: String) {
        viewModelScope.launch {
            repository.hideConference(confId)
            // The 'collect' in init will automatically call applyFilter when this finishes
        }
    }

    // The logic that keeps "Nonsense" out of view
    private fun applyFilter(blockedIds: Set<String>) {
        results = rawResults.filter { it.joinCode !in blockedIds }.sortedBy { it.name }
    }

    fun refresh() {
        // Re-run the search with the current query to force a fresh pull from Algolia
        onQueryChange(query)
    }
    fun clearResults() {
        rawResults = emptyList()
        results = emptyList()
        query = ""
    }
}