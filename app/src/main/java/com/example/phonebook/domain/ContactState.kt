package com.example.phonebook.domain

data class ContactState(
    val contacts : List<Contact> = listOf(),
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber : String = "",
    var isAddingContact : Boolean = false,
    val sortType: SortType = SortType.FIRST_NAME
    )
