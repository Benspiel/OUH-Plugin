package de.ben.oUH.status;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StatusTabCompleter implements TabCompleter {

    private final Map<String, String> statuses;

    public StatusTabCompleter(Map<String, String> statuses) {
        this.statuses = statuses;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String status : statuses.keySet()) {
                if (status.startsWith(input)) {
                    completions.add(status);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
