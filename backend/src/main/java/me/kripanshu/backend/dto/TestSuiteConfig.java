package me.kripanshu.backend.dto;

import me.kripanshu.backend.entity.MutationType;

import java.util.EnumSet;
import java.util.Set;

public record TestSuiteConfig(
        Integer maxConcurrency,
        Integer perRequestTimeoutMs,
        Integer maxResponseSizeBytes,
        Integer runDurationLimitSeconds,
        Boolean judgeSuspiciousOnly,
        Set<MutationType> mutators
) {

    public static TestSuiteConfig defaults() {
        return new TestSuiteConfig(
                5,
                15_000,
                65_536,
                120,
                true,
                EnumSet.allOf(MutationType.class)
        );
    }

    public TestSuiteConfig merge() {
        TestSuiteConfig defaultConfig = defaults();
        return new TestSuiteConfig(
                maxConcurrency != null ? maxConcurrency : defaultConfig.maxConcurrency,
                perRequestTimeoutMs != null ? perRequestTimeoutMs : defaultConfig.perRequestTimeoutMs,
                maxResponseSizeBytes != null ? maxResponseSizeBytes : defaultConfig.maxResponseSizeBytes,
                runDurationLimitSeconds != null ? runDurationLimitSeconds : defaultConfig.runDurationLimitSeconds,
                judgeSuspiciousOnly != null ? judgeSuspiciousOnly : defaultConfig.judgeSuspiciousOnly,
                mutators != null ? mutators : defaultConfig.mutators
        );
    }
}
