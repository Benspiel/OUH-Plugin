package de.ben.oUH;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Death implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String deathMessage;
        String playerName = event.getEntity().getName();
        String killerName = null;

        if (event.getEntity().getKiller() != null) {
            killerName = event.getEntity().getKiller().getName();
            deathMessage = Messages.getDeathMessage(playerName, Messages.DeathCause.PLAYER_KILL, killerName);
        } else if (event.getEntity().getLastDamageCause() != null) {
            switch (event.getEntity().getLastDamageCause().getCause()) {
                case FALL:
                    deathMessage = Messages.getDeathMessage(playerName, Messages.DeathCause.FALL);
                    break;
                case FIRE:
                    deathMessage = Messages.getDeathMessage(playerName, Messages.DeathCause.FIRE);
                    break;
                case LAVA:
                    deathMessage = Messages.getDeathMessage(playerName, Messages.DeathCause.LAVA);
                    break;
                case DROWNING:
                    deathMessage = Messages.getDeathMessage(playerName, Messages.DeathCause.DROWNING);
                    break;
                case ENTITY_ATTACK:
                    deathMessage = Messages.getDeathMessage(playerName, Messages.DeathCause.ENTITY_ATTACK);
                    break;
                default:
                    deathMessage = Messages.getDeathMessage(playerName, Messages.DeathCause.OTHER);
                    break;
            }
        } else {
            deathMessage = Messages.getDeathMessage(playerName, Messages.DeathCause.OTHER);
        }

        event.setDeathMessage(deathMessage);
    }

    public static class Messages {
        private static final Map<DeathCause, List<String>> deathMessages = Map.of(
                DeathCause.FALL, List.of(
                        "[✞] %player% wollte den Boden küssen.",
                        "[✞] %player% dachte, er kann fliegen – falsch gedacht.",
                        "[✞] %player% hat den Boden mit dem Gesicht begrüßt.",
                        "[✞] %player% hat den Höhenflug nicht überlebt."
                ),
                DeathCause.FIRE, List.of(
                        "[✞] %player% hat Feuer gefangen!",
                        "[✞] %player% wurde flambiert.",
                        "[✞] %player% brannte lichterloh.",
                        "[✞] %player% hat sich ein bisschen zu warm angezogen."
                ),
                DeathCause.LAVA, List.of(
                        "[✞] %player% ging in Lava baden.",
                        "[✞] %player% wurde zu Obsidian... fast.",
                        "[✞] %player% ist geschmolzen wie Butter.",
                        "[✞] %player% hat sich in Lava gesonnt."
                ),
                DeathCause.DROWNING, List.of(
                        "[✞] %player% hat das Atmen unter Wasser nicht gemeistert.",
                        "[✞] %player% ging baden – ohne Rückfahrt.",
                        "[✞] %player% hat vergessen, wie man schwimmt.",
                        "[✞] %player% ist untergegangen wie ein Stein."
                ),
                DeathCause.ENTITY_ATTACK, List.of(
                        "[✞] %player% wurde verprügelt.",
                        "[✞] %player% konnte sich nicht wehren.",
                        "[✞] %player% war dem Mob unterlegen.",
                        "[✞] %player% wurde von einem Mobsquad erledigt."
                ),
                DeathCause.PLAYER_KILL, List.of(
                        "[✞] %player% wurde von %killer% in den Boden gestampft.",
                        "[✞] %killer% hat %player% gezeigt, wo der Hammer hängt.",
                        "[✞] %player% legte sich mit %killer% an... RIP.",
                        "[✞] %killer% hat %player% eiskalt erwischt."
                ),
                DeathCause.OTHER, List.of(
                        "[✞] %player% ist mysteriös gestorben.",
                        "[✞] %player% hat einfach aufgehört zu existieren.",
                        "[✞] %player% hat den Tod gefunden.",
                        "[✞] %player% verabschiedete sich aus der Welt."
                )
        );

        public static String getDeathMessage(String playerName, DeathCause cause) {
            List<String> messages = deathMessages.getOrDefault(cause, deathMessages.get(DeathCause.OTHER));
            String template = messages.get(random.nextInt(messages.size()));
            return ChatColor.RED + template.replace("%player%", playerName);
        }

        public static String getDeathMessage(String playerName, DeathCause cause, String killerName) {
            List<String> messages = deathMessages.getOrDefault(cause, deathMessages.get(DeathCause.OTHER));
            String template = messages.get(random.nextInt(messages.size()));
            return ChatColor.RED + template
                    .replace("%player%", playerName)
                    .replace("%killer%", killerName);
        }

        public enum DeathCause {
            FALL,
            FIRE,
            LAVA,
            DROWNING,
            ENTITY_ATTACK,
            PLAYER_KILL,
            OTHER
        }
    }
}
