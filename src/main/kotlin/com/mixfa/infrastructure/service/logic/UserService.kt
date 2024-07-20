package com.mixfa.infrastructure.service.logic

import com.mixfa.infrastructure.misc.ClientContext
import com.mixfa.infrastructure.misc.toByteBuffer
import com.mixfa.infrastructure.model.User
import com.mixfa.infrastructure.service.repo.UserRepo
import kotlinx.coroutines.launch
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

interface UserServiceOps {
    fun register(username: String, password: String)
    fun authenticate(username: String, password: String)
}

@Service
class UserService(
    private val userRepo: UserRepo,
    private val passwordEncoder: PasswordEncoder,
    private val clientContext: ClientContext
) : UserServiceOps {
    override fun register(username: String, password: String) {
        val (client, scope) = clientContext.get()

        scope.launch {
            val exists = userRepo.existsById(username)
            if (exists) {
                client.send(USER_EXIST_MSG_BUFFER)
                return@launch
            }

            val user = User(username, passwordEncoder.encode(password))
            client.user = user

            userRepo.save(user)
        }
    }

    override fun authenticate(username: String, password: String) {
        val (client, scope) = clientContext.get()

        scope.launch {
            val userOpt = userRepo.findById(username)

            if (userOpt.isEmpty) {
                client.send(USER_NOT_EXIST_MSG_BUFFER)
                return@launch
            }

            val user = userOpt.get()

            if (!passwordEncoder.matches(user.password, password)) {
                client.send(INVALID_PASSWORD_MSG_BUFFER)
                return@launch
            }

            client.user = user
        }
    }

    companion object {
        private val USER_EXIST_MSG_BUFFER = "Username taken".toByteBuffer()
        private val USER_NOT_EXIST_MSG_BUFFER = "User not exist".toByteBuffer()
        private val INVALID_PASSWORD_MSG_BUFFER = "Invalid password".toByteBuffer()
    }
}