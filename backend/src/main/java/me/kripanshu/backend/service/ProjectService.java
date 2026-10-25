package me.kripanshu.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kripanshu.backend.dto.CreateProjectRequest;
import me.kripanshu.backend.dto.ProjectResponse;
import me.kripanshu.backend.dto.UpdateProjectRequest;
import me.kripanshu.backend.entity.Project;
import me.kripanshu.backend.exception.DuplicateResourceException;
import me.kripanshu.backend.exception.ResourceInUseException;
import me.kripanshu.backend.exception.ResourceNotFoundException;
import me.kripanshu.backend.repository.BaselineRepository;
import me.kripanshu.backend.repository.EndPointRepository;
import me.kripanshu.backend.repository.ProjectRepository;
import me.kripanshu.backend.repository.TestRunRepository;
import me.kripanshu.backend.repository.TestSuiteRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TestSuiteService testSuiteService;
    private final EndPointRepository endPointRepository;
    private final BaselineRepository baselineRepository;
    private final TestSuiteRepository testSuiteRepository;
    private final TestRunRepository testRunRepository;

    public ProjectResponse createProject(@NotNull CreateProjectRequest request) {
        if (projectRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Project", "name", request.name());
        }
        String normalizedBaseUrl = normalizeBaseUrl(request.baseUrl());
        if (projectRepository.existsByBaseUrl(normalizedBaseUrl)) {
            throw new DuplicateResourceException("Project", "baseUrl", normalizedBaseUrl);
        }

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setBaseUrl(normalizedBaseUrl);
        project.setVersion(request.version());

        project = projectRepository.save(project);
        testSuiteService.createDefaultSuite(project);
        log.info("Created project: id={}, name={}", project.getId(), project.getName());
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(String id) {
        return projectRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse updateProject(String id, @NotNull UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        if (request.name() != null) {
            if (!request.name().equals(project.getName()) && projectRepository.existsByNameAndIdNot(request.name(), id)) {
                throw new DuplicateResourceException("Project", "name", request.name());
            }
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.baseUrl() != null) {
            String normalizedBaseUrl = normalizeBaseUrl(request.baseUrl());
            if (!normalizedBaseUrl.equals(project.getBaseUrl())
                    && projectRepository.existsByBaseUrlAndIdNot(normalizedBaseUrl, id)) {
                throw new DuplicateResourceException("Project", "baseUrl", normalizedBaseUrl);
            }
            project.setBaseUrl(normalizedBaseUrl);
        }
        if (request.version() != null) {
            project.setVersion(request.version());
        }

        project = projectRepository.save(project);
        log.info("Updated project: id={}", project.getId());
        return toResponse(project);
    }

    public void deleteProject(String id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", id);
        }

        if (testRunRepository.existsBySuiteProjectId(id)) {
            throw new ResourceInUseException(
                    "Project '" + id + "' cannot be deleted while test runs exist; run history is retained");
        }

        endPointRepository.findByProjectId(id).forEach(endpoint -> {
            baselineRepository.deleteByEndpointId(endpoint.getId());
            endPointRepository.delete(endpoint);
        });
        testSuiteRepository.deleteByProjectId(id);
        projectRepository.deleteById(id);
        log.info("Deleting project: id={}", id);
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getBaseUrl(),
                project.getVersion(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
