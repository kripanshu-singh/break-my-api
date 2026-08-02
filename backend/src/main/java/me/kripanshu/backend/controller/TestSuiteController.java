package me.kripanshu.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.kripanshu.backend.dto.ApiResponse;
import me.kripanshu.backend.dto.CreateTestSuiteRequest;
import me.kripanshu.backend.dto.TestSuiteResponse;
import me.kripanshu.backend.dto.UpdateTestSuiteRequest;
import me.kripanshu.backend.service.TestSuiteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/test-suites")
@RequiredArgsConstructor
public class TestSuiteController {

    private final TestSuiteService testSuiteService;

    @PostMapping
    public ResponseEntity<ApiResponse<TestSuiteResponse>> createTestSuite(
            @PathVariable String projectId,
            @Valid @RequestBody CreateTestSuiteRequest request
    ) {
        TestSuiteResponse response = testSuiteService.createTestSuite(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TestSuiteResponse>>> getTestSuitesByProject(
            @PathVariable String projectId
    ) {
        List<TestSuiteResponse> suites = testSuiteService.getTestSuitesByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(suites));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestSuiteResponse>> getTestSuite(
            @PathVariable String projectId,
            @PathVariable String id
    ) {
        TestSuiteResponse response = testSuiteService.getTestSuite(projectId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TestSuiteResponse>> updateTestSuite(
            @PathVariable String projectId,
            @PathVariable String id,
            @Valid @RequestBody UpdateTestSuiteRequest request
    ) {
        TestSuiteResponse response = testSuiteService.updateTestSuite(projectId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestSuite(
            @PathVariable String projectId,
            @PathVariable String id
    ) {
        testSuiteService.deleteTestSuite(projectId, id);
        return ResponseEntity.noContent().build();
    }
}
