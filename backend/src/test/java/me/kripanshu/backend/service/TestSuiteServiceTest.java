package me.kripanshu.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kripanshu.backend.dto.CreateTestSuiteRequest;
import me.kripanshu.backend.dto.TestSuiteConfig;
import me.kripanshu.backend.dto.TestSuiteResponse;
import me.kripanshu.backend.entity.MutationType;
import me.kripanshu.backend.entity.Project;
import me.kripanshu.backend.entity.TestSuite;
import me.kripanshu.backend.exception.BadRequestException;
import me.kripanshu.backend.exception.ResourceInUseException;
import me.kripanshu.backend.exception.ResourceNotFoundException;
import me.kripanshu.backend.repository.ProjectRepository;
import me.kripanshu.backend.repository.TestRunRepository;
import me.kripanshu.backend.repository.TestSuiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestSuiteServiceTest {

    @Mock
    private TestSuiteRepository testSuiteRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TestRunRepository testRunRepository;

    private TestSuiteService service;

    private final Project project = new Project();

    @BeforeEach
    void setUp() {
        service = new TestSuiteService(testSuiteRepository, projectRepository, testRunRepository, new ObjectMapper());
        project.setId("proj-1");
    }

    @Test
    void createTestSuite_mergesDefaultsWhenConfigNull() {
        when(projectRepository.findById("proj-1")).thenReturn(Optional.of(project));
        when(testSuiteRepository.save(any(TestSuite.class))).thenAnswer(inv -> {
            TestSuite suite = inv.getArgument(0);
            suite.setId("suite-1");
            return suite;
        });

        TestSuiteResponse response = service.createTestSuite("proj-1", new CreateTestSuiteRequest("Strict", null));

        assertThat(response.id()).isEqualTo("suite-1");
        assertThat(response.configuration().maxConcurrency()).isEqualTo(5);
        assertThat(response.configuration().perRequestTimeoutMs()).isEqualTo(15_000);
        assertThat(response.configuration().judgeSuspiciousOnly()).isTrue();
        assertThat(response.configuration().mutators()).hasSize(7);
    }

    @Test
    void createTestSuite_mergesPartialConfigOverDefaults() {
        when(projectRepository.findById("proj-1")).thenReturn(Optional.of(project));
        when(testSuiteRepository.save(any(TestSuite.class))).thenAnswer(inv -> {
            TestSuite suite = inv.getArgument(0);
            suite.setId("suite-2");
            return suite;
        });

        TestSuiteConfig partial = new TestSuiteConfig(10, null, null, null, null,
                Set.of(MutationType.BOUNDARY, MutationType.INJECTION));
        TestSuiteResponse response = service.createTestSuite("proj-1", new CreateTestSuiteRequest("Focused", partial));

        assertThat(response.configuration().maxConcurrency()).isEqualTo(10);
        assertThat(response.configuration().perRequestTimeoutMs()).isEqualTo(15_000);
        assertThat(response.configuration().mutators()).containsExactlyInAnyOrder(MutationType.BOUNDARY, MutationType.INJECTION);
    }

    @Test
    void createTestSuite_invalidConcurrency_throws() {
        when(projectRepository.findById("proj-1")).thenReturn(Optional.of(project));

        TestSuiteConfig invalid = new TestSuiteConfig(500, null, null, null, null, null);

        assertThatThrownBy(() -> service.createTestSuite("proj-1", new CreateTestSuiteRequest("Bad", invalid)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("maxConcurrency");
        verify(testSuiteRepository, never()).save(any());
    }

    @Test
    void createTestSuite_projectMissing_throws() {
        when(projectRepository.findById("proj-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTestSuite("proj-1", new CreateTestSuiteRequest("Orphan", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project");
    }

    @Test
    void getTestSuite_suiteFromDifferentProject_throws() {
        TestSuite suite = new TestSuite();
        suite.setId("suite-1");
        suite.setProject(project);
        when(testSuiteRepository.findById("suite-1")).thenReturn(Optional.of(suite));

        assertThatThrownBy(() -> service.getTestSuite("other-project", "suite-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTestSuite_withExistingRuns_throwsResourceInUse() {
        TestSuite suite = new TestSuite();
        suite.setId("suite-1");
        suite.setProject(project);
        when(testSuiteRepository.findById("suite-1")).thenReturn(Optional.of(suite));
        when(testRunRepository.existsBySuiteId("suite-1")).thenReturn(true);

        assertThatThrownBy(() -> service.deleteTestSuite("proj-1", "suite-1"))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("run history is retained");
        verify(testSuiteRepository, never()).delete(any());
    }

    @Test
    void deleteTestSuite_withoutRuns_deletes() {
        TestSuite suite = new TestSuite();
        suite.setId("suite-1");
        suite.setProject(project);
        when(testSuiteRepository.findById("suite-1")).thenReturn(Optional.of(suite));
        when(testRunRepository.existsBySuiteId("suite-1")).thenReturn(false);

        service.deleteTestSuite("proj-1", "suite-1");

        verify(testSuiteRepository).delete(suite);
    }
}
