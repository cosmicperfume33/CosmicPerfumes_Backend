package com.example.ForgeX.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ForgeX.model.Address;
import com.example.ForgeX.model.User;


@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {
    Address findByUser(User userdb);
}
