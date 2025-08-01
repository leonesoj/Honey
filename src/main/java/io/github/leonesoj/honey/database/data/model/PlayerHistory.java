package io.github.leonesoj.honey.database.data.model;

import java.util.Set;
import java.util.UUID;

public record PlayerHistory(UUID uuid, Set<UUID> banHistory, Set<UUID> kickHistory) {

}
