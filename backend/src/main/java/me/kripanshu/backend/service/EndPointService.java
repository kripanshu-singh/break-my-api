package me.kripanshu.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kripanshu.backend.dto.CreateEndPointRequest;
import me.kripanshu.backend.dto.EndPointResponse;
import me.kripanshu.backend.dto.UpdateEndPointRequest;
import me.kripanshu.backend.entity.Endpoint;
import me.kripanshu.backend.entity.Project;
import me.kripanshu.backend.exception.BadRequestException;
import me.kripanshu.backend.exception.ResourceNotFoundException;
import me.kripanshu.backend.repository.EndPointRepository;
import me.kripanshu.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EndPointService {

    private static final Set<String> VALID_HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "QUERY");

    private final EndPointRepository endPointRepository;
    private final ProjectRepository projectRepository;

    public EndPointResponse createEndPoint(String projectId, CreateEndPointRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        validateMethod(request.method());
        validatePath(request.path());

        Endpoint endpoint = new Endpoint();
        endpoint.setProject(project);
        endpoint.setPath(request.path());
        endpoint.setMethod(request.method().toUpperCase());
        endpoint.setHeaders(request.headers());
        endpoint.setQueryParams(request.queryParams());
        endpoint.setBodySchema(request.bodySchema());
        endpoint.setSamplePayload(request.samplePayload());

        endpoint = endPointRepository.save(endpoint);
        log.info("Created endpoint: id={}, path={}, method={}, projectId={}",
                endpoint.getId(), endpoint.getPath(), endpoint.getMethod(), projectId);
        return toResponse(endpoint);
    }

    @Transactional(readOnly = true)
    public EndPointResponse getEndpoint(String id) {
        return endPointRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint", id));
    }

    @Transactional(readOnly = true)
    public List<EndPointResponse> getEndpointsByProjectId(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return endPointRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    public EndPointResponse updateEndpoint(String id, UpdateEndPointRequest request) {
        Endpoint endpoint = endPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint", id));

        if (request.path() != null) {
            validatePath(request.path());
            endpoint.setPath(request.path());
        }
        if (request.method() != null) {
            validateMethod(request.method());
            endpoint.setMethod(request.method().toUpperCase());
        }
        if (request.headers() != null) {
            endpoint.setHeaders(request.headers());
        }
        if (request.queryParams() != null) {
            endpoint.setQueryParams(request.queryParams());
        }
        if (request.bodySchema() != null) {
            endpoint.setBodySchema(request.bodySchema());
        }
        if (request.samplePayload() != null) {
            endpoint.setSamplePayload(request.samplePayload());
        }

        endpoint = endPointRepository.save(endpoint);
        log.info("Updated endpoint: id={}", endpoint.getId());
        return toResponse(endpoint);
    }

    public void deleteEndpoint(String id) {
        if (!endPointRepository.existsById(id)) {
            throw new ResourceNotFoundException("Endpoint", id);
        }
        endPointRepository.deleteById(id);
        log.info("Deleted endpoint: id={}", id);
    }

    private static void validateMethod(String method) {
        if (method == null || !VALID_HTTP_METHODS.contains(method.toUpperCase())) {
            throw new BadRequestException(
                    "Invalid HTTP method: " + method + ". Allowed: " + VALID_HTTP_METHODS);
        }
    }

    private static void validatePath(String path) {
        if (path == null || !path.startsWith("/")) {
            throw new BadRequestException("Path must start with /");
        }
    }

    private EndPointResponse toResponse(Endpoint endpoint) {
        return new EndPointResponse(
                endpoint.getId(),
                endpoint.getProject().getId(),
                endpoint.getPath(),
                endpoint.getMethod(),
                endpoint.getHeaders(),
                endpoint.getQueryParams(),
                endpoint.getBodySchema(),
                endpoint.getSamplePayload(),
                endpoint.getCreatedAt(),
                endpoint.getUpdatedAt()
        );
    }
}