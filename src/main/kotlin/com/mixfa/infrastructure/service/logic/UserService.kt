package com.mixfa.infrastructure.service.logic

import com.mixfa.infrastructure.misc.ClientContext
import com.mixfa.infrastructure.misc.exception.ClientError
import com.mixfa.infrastructure.misc.exception.invalidPassword
import com.mixfa.infrastructure.misc.exception.orThrow
import com.mixfa.infrastructure.misc.exception.usernameTaken
import com.mixfa.infrastructure.model.User
import com.mixfa.infrastructure.service.repo.UserRepo
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

interface UserServiceOps {
    suspend fun register(username: String, password: String)
    suspend fun authenticate(username: String, password: String)
}

@Service
class UserService(
    private val userRepo: UserRepo,
    private val passwordEncoder: PasswordEncoder,
    private val clientContext: ClientContext
) : UserServiceOps {
    override suspend fun register(username: String, password: String) {
        val client = clientContext.get()

        val exists = userRepo.existsById(username)
        if (exists) throw ClientError.usernameTaken()

        val user = User(username, passwordEncoder.encode(password))
        client.user = user

        userRepo.save(user)
    }

    override suspend fun authenticate(username: String, password: String) {
        val client = clientContext.get()

        val user = userRepo.findById(username).orThrow()

        if (!passwordEncoder.matches(password, user.password))
            throw ClientError.invalidPassword()

        client.user = user
    }
}