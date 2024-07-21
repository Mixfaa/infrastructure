package com.mixfa.infrastructure.misc.exceptions

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.ExcifyOptionalOrThrow
import com.mixfa.infrastructure.model.User

class UserServiceExceptions(msg: String) : ClientError(msg) {
    companion object
}

@ExcifyCachedException(methodName = "usernameTaken")
val usernameTakenException = UserServiceExceptions("Username already taken")

@ExcifyCachedException(methodName = "userNotExist")
@ExcifyOptionalOrThrow(
    type = User::class,
    methodName = "orThrow",
    makeForNullable = true
)
val userNotExistException = UserServiceExceptions("User not exist")

@ExcifyCachedException(methodName = "invalidPassword")
val invalidPasswordException = UserServiceExceptions("Invalid password")