package me.kripanshu.backend.service;

import lombok.extern.slf4j.Slf4j;
import me.kripanshu.backend.dto.CreateProjectRequest;
import me.kripanshu.backend.dto.ProjectResponse;
import me.kripanshu.backend.dto.UpdateProjectRequest;
import me.kripanshu.backend.entity.Project;
import me.kripanshu.backend.exception.DuplicateResourceException;
import me.kripanshu.backend.exception.ResourceNotFoundException;
import me.kripanshu.backend.repository.ProjectRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public ProjectResponse createProject(@NotNull CreateProjectRequest request) {
        if (projectRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Project", "name", request.name());
        }

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setBaseUrl(request.baseUrl());
        project.setVersion(request.version());

        project = projectRepository.save(project);
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
            if (!request.name().equals(project.getName()) && projectRepository.existsByName(request.name())) {
                throw new DuplicateResourceException("Project", "name", request.name());
            }
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.baseUrl() != null) {
            project.setBaseUrl(request.baseUrl());
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
        projectRepository.deleteById(id);
        log.info("Deleted project: id={}", id);
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
}
