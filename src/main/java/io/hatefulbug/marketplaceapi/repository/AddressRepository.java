package io.hatefulbug.marketplaceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.hatefulbug.marketplaceapi.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
}
