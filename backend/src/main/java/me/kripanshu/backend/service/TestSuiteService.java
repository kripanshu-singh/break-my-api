package me.kripanshu.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kripanshu.backend.dto.CreateTestSuiteRequest;
import me.kripanshu.backend.dto.TestSuiteConfig;
import me.kripanshu.backend.dto.TestSuiteResponse;
import me.kripanshu.backend.dto.UpdateTestSuiteRequest;
import me.kripanshu.backend.entity.Project;
import me.kripanshu.backend.entity.TestSuite;
import me.kripanshu.backend.exception.BadRequestException;
import me.kripanshu.backend.exception.ResourceInUseException;
import me.kripanshu.backend.exception.ResourceNotFoundException;
import me.kripanshu.backend.repository.ProjectRepository;
import me.kripanshu.backend.repository.TestRunRepository;
import me.kripanshu.backend.repository.TestSuiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TestSuiteService {

    public static final String DEFAULT_SUITE_NAME = "Default Fuzzing Profile";

    private final TestSuiteRepository testSuiteRepository;
    private final ProjectRepository projectRepository;
    private final TestRunRepository testRunRepository;
    private final ObjectMapper objectMapper;

    public TestSuiteResponse createTestSuite(String projectId, CreateTestSuiteRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        TestSuiteConfig config = resolveConfig(request.configuration());
        validateConfig(config);

        TestSuite suite = new TestSuite();
        suite.setProject(project);
        suite.setName(request.name());
        suite.setConfiguration(writeConfig(config));

        suite = testSuiteRepository.save(suite);
        log.info("Created test suite: id={}, name={}, projectId={}",
                suite.getId(), suite.getName(), projectId);
        return toResponse(suite);
    }

    @Transactional(readOnly = true)
    public TestSuiteResponse getTestSuite(String projectId, String id) {
        return toResponse(findSuiteInProject(projectId, id));
    }

    @Transactional(readOnly = true)
    public List<TestSuiteResponse> getTestSuitesByProjectId(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return testSuiteRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TestSuiteResponse updateTestSuite(String projectId, String id, UpdateTestSuiteRequest request) {
        TestSuite suite = findSuiteInProject(projectId, id);

        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new BadRequestException("Suite name must not be blank");
            }
            suite.setName(request.name());
        }
        if (request.configuration() != null) {
            TestSuiteConfig config = resolveConfig(request.configuration());
            validateConfig(config);
            suite.setConfiguration(writeConfig(config));
        }

        suite = testSuiteRepository.save(suite);
        log.info("Updated test suite: id={}, projectId={}", suite.getId(), projectId);
        return toResponse(suite);
    }

    public void deleteTestSuite(String projectId, String id) {
        TestSuite suite = findSuiteInProject(projectId, id);
        if (testRunRepository.existsBySuiteId(id)) {
            throw new ResourceInUseException(
                    "Test suite '" + id + "' cannot be deleted while test runs exist; run history is retained");
        }
        testSuiteRepository.delete(suite);
        log.info("Deleted test suite: id={}, projectId={}", id, projectId);
    }

    public void createDefaultSuite(Project project) {
        TestSuite suite = new TestSuite();
        suite.setProject(project);
        suite.setName(DEFAULT_SUITE_NAME);
        suite.setConfiguration(writeConfig(TestSuiteConfig.defaults()));
        suite = testSuiteRepository.save(suite);
        log.info("Created default test suite: id={}, projectId={}", suite.getId(), project.getId());
        toResponse(suite);
    }

    private TestSuite findSuiteInProject(String projectId, String id) {
        TestSuite suite = testSuiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestSuite", id));
        if (!suite.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("TestSuite", id);
        }
        return suite;
    }

    private TestSuiteConfig resolveConfig(TestSuiteConfig config) {
        return config != null ? config.merge() : TestSuiteConfig.defaults();
    }

    private static void validateConfig(TestSuiteConfig config) {
        if (config.maxConcurrency() < 1 || config.maxConcurrency() > 50) {
            throw new BadRequestException("maxConcurrency must be between 1 and 50");
        }
        if (config.perRequestTimeoutMs() < 1_000 || config.perRequestTimeoutMs() > 60_000) {
            throw new BadRequestException("perRequestTimeoutMs must be between 1000 and 60000");
        }
        if (config.maxResponseSizeBytes() < 1 || config.maxResponseSizeBytes() > 1_048_576) {
            throw new BadRequestException("maxResponseSizeBytes must be between 1 and 1048576");
        }
        if (config.runDurationLimitSeconds() < 1 || config.runDurationLimitSeconds() > 3_600) {
            throw new BadRequestException("runDurationLimitSeconds must be between 1 and 3600");
        }
        if (config.mutators() == null || config.mutators().isEmpty()) {
            throw new BadRequestException("At least one mutator must be enabled");
        }
    }

    private String writeConfig(TestSuiteConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize test suite configuration", e);
        }
    }

    private TestSuiteConfig readConfig(String configuration) {
        if (configuration == null || configuration.isBlank()) {
            return TestSuiteConfig.defaults();
        }
        try {
            return objectMapper.readValue(configuration, TestSuiteConfig.class);
        } catch (Exception e) {
            log.warn("Failed to parse stored test suite configuration, using defaults: {}", e.getMessage());
            return TestSuiteConfig.defaults();
        }
    }

    private TestSuiteResponse toResponse(TestSuite suite) {
        return new TestSuiteResponse(
                suite.getId(),
                suite.getProject().getId(),
                suite.getName(),
                readConfig(suite.getConfiguration()),
                suite.getCreatedAt(),
                suite.getUpdatedAt()
        );
    }
}
