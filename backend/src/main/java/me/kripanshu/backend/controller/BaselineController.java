package me.kripanshu.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.kripanshu.backend.dto.ApiResponse;
import me.kripanshu.backend.dto.BaselineResponse;
import me.kripanshu.backend.dto.CreateBaselineRequest;
import me.kripanshu.backend.dto.ProbeResult;
import me.kripanshu.backend.service.BaselineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/endpoints/{endpointId}/baseline")
@RequiredArgsConstructor
public class BaselineController {

    private final BaselineService baselineService;

    @PostMapping
    public ResponseEntity<ApiResponse<BaselineResponse>> createOrUpdateBaseline(
            @PathVariable String endpointId,
            @Valid @RequestBody CreateBaselineRequest request
    ) {
        BaselineResponse response = baselineService.createOrUpdateBaseline(endpointId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/probe")
    public ResponseEntity<ApiResponse<ProbeResult>> probeAndSetBaseline(
            @PathVariable String endpointId
    ) {
        ProbeResult result = baselineService.probeAndSetBaseline(endpointId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BaselineResponse>> getBaselineByEndpoint(
            @PathVariable String endpointId
    ) {
        BaselineResponse response = baselineService.getBaselineByEndpoint(endpointId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBaseline(@PathVariable String endpointId) {
        baselineService.deleteBaseline(endpointId);
        return ResponseEntity.noContent().build();
    }
}
