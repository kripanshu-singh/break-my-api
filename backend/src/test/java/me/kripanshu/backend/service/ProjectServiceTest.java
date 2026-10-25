package me.kripanshu.backend.service;

import me.kripanshu.backend.dto.CreateProjectRequest;
import me.kripanshu.backend.dto.ProjectResponse;
import me.kripanshu.backend.dto.UpdateProjectRequest;
import me.kripanshu.backend.entity.Project;
import me.kripanshu.backend.exception.DuplicateResourceException;
import me.kripanshu.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TestSuiteService testSuiteService;

    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(projectRepository, testSuiteService);
    }

    @Test
    void createProject_normalizesBaseUrl() {
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = service.createProject(
                new CreateProjectRequest("Payments", "Payments API", " https://api.example.com/ ", null));

        assertThat(response.baseUrl()).isEqualTo("https://api.example.com");
    }

    @Test
    void createProject_rejectsDuplicateBaseUrl() {
        when(projectRepository.existsByBaseUrl("https://api.example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createProject(
                new CreateProjectRequest("Payments", "Payments API", "https://api.example.com/", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void updateProject_rejectsDuplicateBaseUrl() {
        Project project = new Project();
        project.setId("project-1");
        project.setName("Payments");
        project.setBaseUrl("https://old.example.com");
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));
        when(projectRepository.existsByBaseUrlAndIdNot("https://api.example.com", "project-1")).thenReturn(true);

        assertThatThrownBy(() -> service.updateProject(
                "project-1", new UpdateProjectRequest(null, null, "https://api.example.com/", null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("baseUrl");
    }
}
