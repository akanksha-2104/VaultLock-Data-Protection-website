package com.dataprotection.dataprotection.repository;

import com.dataprotection.dataprotection.entity.LoginLocation;
import com.dataprotection.dataprotection.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginLocationRepository extends JpaRepository<LoginLocation, Long> {
    Optional<LoginLocation> findTopByUserOrderByLoginTimeDesc(User user);

    List<LoginLocation> findAllByUserOrderByLoginTimeDesc(User user);
}
