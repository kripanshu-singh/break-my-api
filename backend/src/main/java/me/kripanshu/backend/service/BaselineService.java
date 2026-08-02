package me.kripanshu.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kripanshu.backend.dto.BaselineResponse;
import me.kripanshu.backend.dto.CreateBaselineRequest;
import me.kripanshu.backend.dto.ProbeResult;
import me.kripanshu.backend.entity.Baseline;
import me.kripanshu.backend.entity.Endpoint;
import me.kripanshu.backend.entity.Project;
import me.kripanshu.backend.exception.BadRequestException;
import me.kripanshu.backend.exception.ResourceNotFoundException;
import me.kripanshu.backend.repository.BaselineRepository;
import me.kripanshu.backend.repository.EndPointRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BaselineService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final BaselineRepository baselineRepository;
    private final EndPointRepository endPointRepository;
    private final RestClient restClient;
    private final JsonSchemaGenerator jsonSchemaGenerator;

    public BaselineResponse createOrUpdateBaseline(String endpointId, CreateBaselineRequest request) {
        Endpoint endpoint = endPointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint", endpointId));

        Baseline baseline = baselineRepository.findByEndpointId(endpointId)
                .orElseGet(Baseline::new);

        baseline.setEndpoint(endpoint);
        baseline.setExpectedStatus(request.expectedStatus());
        baseline.setExpectedSchema(request.expectedSchema());
        baseline.setSampleResponse(request.sampleResponse());
        baseline.setMaxLatencyMs(request.maxLatencyMs());

        baseline = baselineRepository.save(baseline);

        String action = baseline.getCreatedAt().equals(baseline.getUpdatedAt()) ? "Created" : "Updated";
        log.info("{} baseline: id={}, endpointId={}, expectedStatus={}",
                action, baseline.getId(), endpointId, baseline.getExpectedStatus());

        return toResponse(baseline);
    }

    @Transactional(readOnly = true)
    public BaselineResponse getBaselineByEndpoint(String endpointId) {
        if (!endPointRepository.existsById(endpointId)) {
            throw new ResourceNotFoundException("Endpoint", endpointId);
        }
        return baselineRepository.findByEndpointId(endpointId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Baseline for endpoint", endpointId));
    }

    public void deleteBaseline(String endpointId) {
        Baseline baseline = baselineRepository.findByEndpointId(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Baseline for endpoint", endpointId));
        baselineRepository.delete(baseline);
        log.info("Deleted baseline: id={}, endpointId={}", baseline.getId(), endpointId);
    }

    public ProbeResult probeAndSetBaseline(String endpointId) {
        Endpoint endpoint = endPointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint", endpointId));

        Project project = endpoint.getProject();
        HttpMethod httpMethod = HttpMethod.valueOf(endpoint.getMethod().toUpperCase());

        String url = buildUrl(project.getBaseUrl(), endpoint.getPath(), endpoint.getQueryParams());

        Instant start = Instant.now();
        String responseBody;
        int statusCode;
        try {
            var request = restClient
                    .method(httpMethod)
                    .uri(url);

            applyHeaders(request, endpoint.getHeaders());

            if (endpoint.getSamplePayload() != null) {
                request.body(endpoint.getSamplePayload());
                if (!requestHasContentType(endpoint.getHeaders())) {
                    request.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                }
            }

            var response = request
                    .retrieve()
                    .toEntity(String.class);
            statusCode = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (Exception e) {
            log.error("Probe request failed for endpointId={}, url={}: {}", endpointId, url, e.getMessage());
            throw new BadRequestException("Probe request failed: " + e.getMessage());
        }
        long latencyMs = Instant.now().toEpochMilli() - start.toEpochMilli();

        String derivedSchema = jsonSchemaGenerator.generate(responseBody != null ? responseBody : "{}");

        Baseline baseline = baselineRepository.findByEndpointId(endpointId)
                .orElseGet(Baseline::new);
        baseline.setEndpoint(endpoint);
        baseline.setExpectedStatus(statusCode);
        baseline.setSampleResponse(responseBody);
        baseline.setExpectedSchema(derivedSchema);
        baseline.setMaxLatencyMs(latencyMs);
        baseline = baselineRepository.save(baseline);

        String action = baseline.getCreatedAt().equals(baseline.getUpdatedAt()) ? "Created" : "Updated";
        log.info("{} baseline via probe: id={}, endpointId={}, status={}, latency={}ms",
                action, baseline.getId(), endpointId, statusCode, latencyMs);

        return new ProbeResult(
                baseline.getId(),
                endpointId,
                statusCode,
                latencyMs,
                responseBody,
                derivedSchema
        );
    }

    private String buildUrl(String baseUrl, String path, String queryParamsJson) {
        String url = baseUrl + path;
        if (queryParamsJson == null || queryParamsJson.isBlank()) {
            return url;
        }
        try {
            Map<String, String> params = MAPPER.readValue(queryParamsJson, new TypeReference<>() {});
            var builder = UriComponentsBuilder.fromUriString(url);
            params.forEach(builder::queryParam);
            return builder.build().toUriString();
        } catch (Exception e) {
            log.warn("Failed to parse query params, skipping: {}", e.getMessage());
            return url;
        }
    }

    private void applyHeaders(RestClient.RequestBodySpec request, String headersJson) {
        if (headersJson == null || headersJson.isBlank()) return;
        try {
            Map<String, String> headers = MAPPER.readValue(headersJson, new TypeReference<>() {});
            headers.forEach(request::header);
        } catch (Exception e) {
            log.warn("Failed to parse headers, skipping: {}", e.getMessage());
        }
    }

    private boolean requestHasContentType(String headersJson) {
        if (headersJson == null || headersJson.isBlank()) return false;
        try {
            Map<String, String> headers = MAPPER.readValue(headersJson, new TypeReference<>() {});
            return headers.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE));
        } catch (Exception e) {
            return false;
        }
    }

    private BaselineResponse toResponse(Baseline baseline) {
        return new BaselineResponse(
                baseline.getId(),
                baseline.getEndpoint().getId(),
                baseline.getExpectedStatus(),
                baseline.getExpectedSchema(),
                baseline.getSampleResponse(),
                baseline.getMaxLatencyMs(),
                baseline.getCreatedAt(),
                baseline.getUpdatedAt()
        );
    }
}
