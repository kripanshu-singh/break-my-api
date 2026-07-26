package me.kripanshu.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.kripanshu.backend.entity.TestResult;

public interface TestResultRepository extends JpaRepository<TestResult, String> {
    List<TestResult> findByTestRunId(String testRunId);
    List<TestResult> findByTestRunIdAndPassed(String testRunId, Boolean passed);
}
