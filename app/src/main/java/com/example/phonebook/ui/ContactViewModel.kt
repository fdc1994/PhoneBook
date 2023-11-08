package com.example.phonebook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phonebook.data.ContactDAO
import com.example.phonebook.domain.Contact
import com.example.phonebook.domain.ContactEvent
import com.example.phonebook.domain.ContactState
import com.example.phonebook.domain.SortType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactViewModel(
    private val dao: ContactDAO
) : ViewModel() {

    private val _sortType = MutableStateFlow(SortType.FIRST_NAME)
    private val _contacts = _sortType.flatMapLatest {
        when(it) {
            SortType.FIRST_NAME -> dao.getContactsOrderedByFirstName()
            SortType.LAST_NAME -> dao.getContactsOrderedByLastName()
            SortType.PHONE_NUMBER -> dao.getContactsOrderedByPhoneNumber()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _state = MutableStateFlow(ContactState())
    val state = combine(_state, _sortType, _contacts) { state, sortType, contacts ->
        state.copy(
            contacts = contacts,
            sortType = sortType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactState())


    fun onEvent(event: ContactEvent) {
        when (event) {
            is ContactEvent.DeleteContact -> {
                viewModelScope.launch {
                    dao.deleteContact(event.contact)
                }

            }

            ContactEvent.SaveContact -> {
                val firstName = state.value.firstName
                val lastName = state.value.lastName
                val number = state.value.phoneNumber

                if(firstName.isBlank() || lastName.isBlank() || number.isBlank()) {
                    return
                }

                val contact = Contact(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = number
                )

                viewModelScope.launch {
                    dao.upsertContact(contact)
                }
                _state.update { it.copy(
                    isAddingContact = false,
                    firstName = "",
                    lastName = "",
                    phoneNumber = ""
                ) }
            }

            ContactEvent.HideDialog -> _state.update {
                it.copy(
                    isAddingContact = false
                )
            }

            ContactEvent.ShowDialog -> {
                _state.update {
                    it.copy(
                        isAddingContact = true
                    )
                }
            }

            is ContactEvent.SortContacts -> {
                _sortType.value = event.sortType
            }

            is ContactEvent.SetFirstName -> _state.update {
                it.copy(
                    firstName = event.firstName
                )
            }

            is ContactEvent.SetLastName -> _state.update {
                it.copy(
                    lastName = event.lastName
                )
            }

            is ContactEvent.SetPhoneNumber -> _state.update {
                it.copy(
                    phoneNumber = event.phoneNumber
                )
            }
        }
    }
}