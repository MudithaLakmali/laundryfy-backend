package com.laundrify.server.repository;

import com.laundrify.server.model.Laundry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LaundryRepository extends MongoRepository<Laundry, String> {
    Optional<Laundry> findByUserId(String userId);
    boolean existsByUserId(String userId);
    
    // Find laundries by exact location match (City > District > Province hierarchy)
    List<Laundry> findByCityAndDistrictAndProvince(String city, String district, String province);
    
    // Find laundries by city
    List<Laundry> findByCity(String city);
    
    // Find laundries by city and district
    List<Laundry> findByCityAndDistrict(String city, String district);
    
    // Find laundries by province
    List<Laundry> findByProvince(String province);
}
