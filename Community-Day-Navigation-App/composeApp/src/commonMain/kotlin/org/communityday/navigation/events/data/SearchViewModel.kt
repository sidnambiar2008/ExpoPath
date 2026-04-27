package org.communityday.navigation.events.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Inherit from ViewModel for better lifecycle management
class SearchViewModel(private val searcher: ConferenceSearcher) : ViewModel() {

    var query by mutableStateOf("")
        private set // Allow reading but only allow changing via onQueryChange

    var results by mutableStateOf<List<Conference>>(emptyList())
        private set

    var isSearching by mutableStateOf(false)
        private set

    private var searchJob: Job? = null
    init {
        // Call the search function with an empty string to get everything
        onQueryChange("")
    }

    //fun onQueryChange(newQuery: String) {
     //   query = newQuery
     //   searchJob?.cancel()

      //  if (newQuery.length < 3) {
       //     results = emptyList()
        //    isSearching = false
        //    return
        //}

        // Use viewModelScope so search is tied to the VM lifecycle
        //searchJob = viewModelScope.launch {
            // Add a small delay (300ms) to wait for the user to stop typing
         //   delay(300)

            //isSearching = true
            //try {
             //   results = searcher.search(newQuery)
            //} catch (e: Exception) {
                // Log the error for debugging
              //  println("Algolia Search Error: ${e.message}")
               // results = emptyList()
            //} finally {
              //  isSearching = false
            //}
     //   }
    //}
    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            // Only delay if the user is actively typing a specific search
            if (newQuery.isNotEmpty()) {
                delay(300)
            }

            isSearching = true
            try {
                // An empty string query returns all (or top) results in Algolia
                results = searcher.search(newQuery).sortedBy { it.name }
            } catch (e: Exception) {
                results = emptyList()
            } finally {
                isSearching = false
            }
        }
    }
}