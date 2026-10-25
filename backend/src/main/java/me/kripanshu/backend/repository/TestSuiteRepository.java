package me.kripanshu.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.kripanshu.backend.entity.TestSuite;

public interface TestSuiteRepository extends JpaRepository<TestSuite, String> {
    List<TestSuite> findByProjectId(String projectId);
    void deleteByProjectId(String projectId);
}
