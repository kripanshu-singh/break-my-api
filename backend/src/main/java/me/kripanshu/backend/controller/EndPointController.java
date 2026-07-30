package me.kripanshu.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.kripanshu.backend.dto.ApiResponse;
import me.kripanshu.backend.dto.CreateEndPointRequest;
import me.kripanshu.backend.dto.EndPointResponse;
import me.kripanshu.backend.dto.UpdateEndPointRequest;
import me.kripanshu.backend.service.EndPointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndPointController {
    private final EndPointService endPointService;

    @PostMapping("/{projectId}")
    public ResponseEntity<ApiResponse<EndPointResponse>> createEndPoint(
            @PathVariable String projectId,
            @Valid @RequestBody CreateEndPointRequest request
    ) {
        EndPointResponse response = endPointService.createEndPoint(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EndPointResponse>> getEndPoint(@PathVariable String id) {
        EndPointResponse response = endPointService.getEndpoint(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<EndPointResponse>>> getAllEndPoints(@PathVariable String projectId) {
        List<EndPointResponse> endpoints = endPointService.getEndpointsByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(endpoints));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EndPointResponse>> updateEndPoint(
            @PathVariable String id,
            @Valid @RequestBody UpdateEndPointRequest request
    ) {
        EndPointResponse response = endPointService.updateEndpoint(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndPoint(@PathVariable String id) {
        endPointService.deleteEndpoint(id);
        return ResponseEntity.noContent().build();
    }
}
