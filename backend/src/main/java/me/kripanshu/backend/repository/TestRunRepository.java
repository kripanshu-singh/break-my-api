package me.kripanshu.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.kripanshu.backend.entity.TestRun;

public interface TestRunRepository extends JpaRepository<TestRun, String> {
    List<TestRun> findBySuiteId(String suiteId);
    boolean existsBySuiteId(String suiteId);
}
