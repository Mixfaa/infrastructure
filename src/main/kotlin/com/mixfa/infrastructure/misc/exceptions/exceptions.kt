package com.mixfa.infrastructure.misc.exceptions

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.ExcifyOptionalOrThrow
import com.mixfa.infrastructure.model.User

@ExcifyCachedException(methodName = "usernameTaken")
val usernameTakenException = ClientError("Username already taken")

@ExcifyCachedException(methodName = "userNotExist")
@ExcifyOptionalOrThrow(
    type = User::class,
    methodName = "orThrow",
    makeForNullable = true
)
val userNotExistException = ClientError("User not exist")

@ExcifyCachedException(methodName = "invalidPassword")
val invalidPasswordException = ClientError("Invalid password")

@ExcifyCachedException(methodName = "notChannelAdmin")
val notChannelAdminException = ClientError("You are not admin of this channel")