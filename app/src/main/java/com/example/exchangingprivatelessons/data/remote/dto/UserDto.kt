/* data/remote/dto/UserDto.kt */
package com.example.exchangingprivatelessons.data.remote.dto

import com.google.firebase.Timestamp

data class UserDto(
    var id:           String     = "",
    var displayName:  String     = "",
    var email:        String     = "",
    var photoUrl:     String     = "",
    var bio:          String     = "",
    var score:        Int        = 0,
    var createdAt:    Timestamp? = null,
    var lastLoginAt:  Timestamp? = null,
    var lastUpdatedAt:  Timestamp? = null      // 👈 עכשיו ‎var‎, ולא ‎val‎
)
