package de.ben.oUH.status;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class StatusPlaceholder extends PlaceholderExpansion {

    private final Map<UUID, String> playerStatuses;
    private final Map<String, String> statuses;

    public StatusPlaceholder(Map<UUID, String> playerStatuses, Map<String, String> statuses) {
        this.playerStatuses = playerStatuses;
        this.statuses = statuses;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ouh"; // ergibt %ouh_status%
    }

    @Override
    public @NotNull String getAuthor() {
        return "de.ben";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.equalsIgnoreCase("status")) {
            String key = playerStatuses.get(player.getUniqueId());
            return key != null ? statuses.getOrDefault(key, "") : "";
        }
        return null;
    }
}
