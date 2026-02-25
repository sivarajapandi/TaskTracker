package com.siva.taskTracker.repository;

import com.siva.taskTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{
    //what is the use of optional here?
    // Optional is a container object which may or may not contain a non-null value.
    // It provides methods to check if a value is present, retrieve the value, or provide a default value if the value is absent. This helps to avoid null pointer exceptions and makes the code more readable and safer when dealing with potentially null values.
    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

}