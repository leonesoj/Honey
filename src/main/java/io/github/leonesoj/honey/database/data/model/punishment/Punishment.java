package io.github.leonesoj.honey.database.data.model.punishment;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record Punishment(UUID id, UUID issuer, UUID offender, Set<String> serverScope,
                         String reason, Instant startTime, Instant endTime) {
}
