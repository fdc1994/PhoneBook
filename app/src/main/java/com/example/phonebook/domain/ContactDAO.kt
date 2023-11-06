package com.example.phonebook.domain

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert

@Dao
interface ContactDAO {

    @Upsert
    suspend fun upsertContact(contact: Contact) {

    }

    @Delete
    suspend fun deleteContact(contact: Contact)
}