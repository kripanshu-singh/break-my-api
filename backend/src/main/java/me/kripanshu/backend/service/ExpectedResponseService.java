package me.kripanshu.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kripanshu.backend.dto.ExpectedResponseResponse;
import me.kripanshu.backend.dto.CreateExpectedResponseRequest;
import me.kripanshu.backend.dto.UpdateExpectedResponseRequest;
import me.kripanshu.backend.entity.ExpectedResponse;
import me.kripanshu.backend.entity.Endpoint;
import me.kripanshu.backend.exception.BadRequestException;
import me.kripanshu.backend.exception.DuplicateResourceException;
import me.kripanshu.backend.exception.ResourceNotFoundException;
import me.kripanshu.backend.repository.ExpectedResponseRepository;
import me.kripanshu.backend.repository.EndPointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExpectedResponseService {

    private final ExpectedResponseRepository expectedResponseRepository;
    private final EndPointRepository endPointRepository;

    public ExpectedResponseResponse createExpectedResponse(String endpointId, CreateExpectedResponseRequest request) {
        Endpoint endpoint = endPointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint", endpointId));

        if (expectedResponseRepository.findByEndpointId(endpointId).isPresent()) {
            throw new DuplicateResourceException("ExpectedResponse", "endpointId", endpointId);
        }

        ExpectedResponse expectedResponse = new ExpectedResponse();
        expectedResponse.setEndpoint(endpoint);
        expectedResponse.setExpectedStatus(request.expectedStatus());
        expectedResponse.setExpectedSchema(request.expectedSchema());
        expectedResponse.setSampleResponse(request.sampleResponse());
        expectedResponse.setMaxLatencyMs(request.maxLatencyMs());

        expectedResponse = expectedResponseRepository.save(expectedResponse);
        log.info("Created expected response: id={}, endpointId={}, expectedStatus={}",
                expectedResponse.getId(), endpointId, expectedResponse.getExpectedStatus());
        return toResponse(expectedResponse);
    }

    @Transactional(readOnly = true)
    public ExpectedResponseResponse getExpectedResponse(String id) {
        return expectedResponseRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ExpectedResponse", id));
    }

    @Transactional(readOnly = true)
    public ExpectedResponseResponse getExpectedResponseByEndpointId(String endpointId) {
        if (!endPointRepository.existsById(endpointId)) {
            throw new ResourceNotFoundException("Endpoint", endpointId);
        }
        return expectedResponseRepository.findByEndpointId(endpointId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ExpectedResponse for endpoint", endpointId));
    }

    public ExpectedResponseResponse updateExpectedResponse(String id, UpdateExpectedResponseRequest request) {
        ExpectedResponse expectedResponse = expectedResponseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExpectedResponse", id));

        if (request.expectedStatus() != null) {
            int status = request.expectedStatus();
            if (status < 100 || status > 599) {
                throw new BadRequestException("expectedStatus must be between 100 and 599");
            }
            expectedResponse.setExpectedStatus(status);
        }
        if (request.expectedSchema() != null) {
            expectedResponse.setExpectedSchema(request.expectedSchema());
        }
        if (request.sampleResponse() != null) {
            expectedResponse.setSampleResponse(request.sampleResponse());
        }
        if (request.maxLatencyMs() != null) {
            if (request.maxLatencyMs() < 0) {
                throw new BadRequestException("maxLatencyMs must be non-negative");
            }
            expectedResponse.setMaxLatencyMs(request.maxLatencyMs());
        }

        expectedResponse = expectedResponseRepository.save(expectedResponse);
        log.info("Updated expected response: id={}", expectedResponse.getId());
        return toResponse(expectedResponse);
    }

    public void deleteExpectedResponse(String id) {
        if (!expectedResponseRepository.existsById(id)) {
            throw new ResourceNotFoundException("ExpectedResponse", id);
        }
        expectedResponseRepository.deleteById(id);
        log.info("Deleted expected response: id={}", id);
    }

    private ExpectedResponseResponse toResponse(ExpectedResponse expectedResponse) {
        return new ExpectedResponseResponse(
                expectedResponse.getId(),
                expectedResponse.getEndpoint().getId(),
                expectedResponse.getExpectedStatus(),
                expectedResponse.getExpectedSchema(),
                expectedResponse.getSampleResponse(),
                expectedResponse.getMaxLatencyMs(),
                expectedResponse.getCreatedAt(),
                expectedResponse.getUpdatedAt()
        );
    }
}
