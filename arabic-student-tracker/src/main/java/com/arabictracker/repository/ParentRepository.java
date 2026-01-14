package com.arabictracker.repository;

import com.arabictracker.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByPhone(String phone);
    boolean existsByPhone(String phone);
}