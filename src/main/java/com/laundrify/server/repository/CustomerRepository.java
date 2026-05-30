package com.laundrify.server.repository;

import com.laundrify.server.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    Optional<Customer> findByUserId(String userId);
    Optional<Customer> findByEmail(String email);
}
