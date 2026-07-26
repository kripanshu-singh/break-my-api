package me.kripanshu.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.kripanshu.backend.entity.Baseline;

public interface BaselineRepository extends JpaRepository<Baseline, String> {
    Optional<Baseline> findByEndpointId(String endpointId);
}
