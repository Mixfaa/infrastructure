package com.mixfa.infrastructure.service.logic

import com.mixfa.infrastructure.misc.ClientContext
import com.mixfa.infrastructure.misc.toByteBuffer
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
        if (exists) {
            client.send(USER_EXIST_MSG_BUFFER)
            return
        }

        val user = User(username, passwordEncoder.encode(password))
        client.user = user

        userRepo.save(user)
    }

    override suspend fun authenticate(username: String, password: String) {
        val client = clientContext.get()

        val user = userRepo.findById(username)

        if (user == null) {
            client.send(USER_NOT_EXIST_MSG_BUFFER)
            return
        }

        if (!passwordEncoder.matches(password, user.password)) {
            client.send(INVALID_PASSWORD_MSG_BUFFER)
            return
        }

        client.user = user
    }

    companion object {
        private val USER_EXIST_MSG_BUFFER = "Username taken".toByteBuffer()
        private val USER_NOT_EXIST_MSG_BUFFER = "User not exist".toByteBuffer()
        private val INVALID_PASSWORD_MSG_BUFFER = "Invalid password".toByteBuffer()
    }
}