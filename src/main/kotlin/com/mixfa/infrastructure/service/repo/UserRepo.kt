package com.mixfa.infrastructure.service.repo

import com.mixfa.infrastructure.model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepo : MongoRepository<User, String> {
}