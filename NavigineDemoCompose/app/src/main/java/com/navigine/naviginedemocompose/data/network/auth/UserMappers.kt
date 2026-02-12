package com.navigine.naviginedemocompose.data.network.auth

import com.navigine.naviginedemocompose.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    name = name.orEmpty(),
    companyName = companyName,
    email = email,
    hash = hash,
    role = role,
    active = (active ?: false),
    lastEntryIso = lastEntry,
    language = language,
    phone = phoneNumber,
    registeredAtIso = registeredAt,
    position = position,
    linkedin = linkedin,
    avatarUrl = avatarUrl
)