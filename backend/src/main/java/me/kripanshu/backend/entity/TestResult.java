package me.kripanshu.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "test_results")
public class TestResult {

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TestRun testRun;

    @Column(name = "mutation_type", length = 50, nullable = false)
    private String mutationType;

    @Column(name = "request_payload", columnDefinition = "JSONB")
    private String requestPayload;

    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "JSONB")
    private String responseBody;

    @Column(name = "latency_ms", nullable = false)
    private Integer latencyMs;

    @Column(nullable = false)
    private Boolean passed;

    @Column(name = "diff_report", columnDefinition = "JSONB")
    private String diffReport;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Severity severity;
}
