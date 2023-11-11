package com.demo.repository;

import com.demo.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {

    User findByUserName(String userName);

    boolean existsByUserName(String username);
}
