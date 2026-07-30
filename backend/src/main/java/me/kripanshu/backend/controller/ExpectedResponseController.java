package me.kripanshu.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.kripanshu.backend.dto.ApiResponse;
import me.kripanshu.backend.dto.ExpectedResponseResponse;
import me.kripanshu.backend.dto.CreateExpectedResponseRequest;
import me.kripanshu.backend.dto.UpdateExpectedResponseRequest;
import me.kripanshu.backend.service.ExpectedResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expected-responses")
@RequiredArgsConstructor
public class ExpectedResponseController {
    private final ExpectedResponseService expectedResponseService;

    @PostMapping("/{endpointId}")
    public ResponseEntity<ApiResponse<ExpectedResponseResponse>> createExpectedResponse(
            @PathVariable String endpointId,
            @Valid @RequestBody CreateExpectedResponseRequest request
    ) {
        ExpectedResponseResponse response = expectedResponseService.createExpectedResponse(endpointId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpectedResponseResponse>> getExpectedResponse(@PathVariable String id) {
        ExpectedResponseResponse response = expectedResponseService.getExpectedResponse(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-endpoint/{endpointId}")
    public ResponseEntity<ApiResponse<ExpectedResponseResponse>> getExpectedResponseByEndpoint(@PathVariable String endpointId) {
        ExpectedResponseResponse response = expectedResponseService.getExpectedResponseByEndpointId(endpointId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpectedResponseResponse>> updateExpectedResponse(
            @PathVariable String id,
            @Valid @RequestBody UpdateExpectedResponseRequest request
    ) {
        ExpectedResponseResponse response = expectedResponseService.updateExpectedResponse(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpectedResponse(@PathVariable String id) {
        expectedResponseService.deleteExpectedResponse(id);
        return ResponseEntity.noContent().build();
    }
}
