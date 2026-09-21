package com.ethobservatory.repository;

import com.ethobservatory.model.AddressActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressActivityRepository extends JpaRepository<AddressActivity, Long> {

    Optional<AddressActivity> findByAddress(String address);
}
