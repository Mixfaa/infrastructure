package com.mixfa.infrastructure.service.repo

import com.mixfa.infrastructure.model.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepo : CoroutineCrudRepository<User, String>