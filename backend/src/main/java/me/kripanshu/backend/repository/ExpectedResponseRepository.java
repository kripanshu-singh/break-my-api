package me.kripanshu.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.kripanshu.backend.entity.ExpectedResponse;

public interface ExpectedResponseRepository extends JpaRepository<ExpectedResponse, String> {
    Optional<ExpectedResponse> findByEndpointId(String endpointId);
}
