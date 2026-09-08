package br.com.fiap.susagenda.support;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class AuthEnabledTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "sus-agenda.auth.enabled", "true",
                "sus-agenda.persistence", "memory",
                "sus-agenda.notification.sender", "log");
    }
}
