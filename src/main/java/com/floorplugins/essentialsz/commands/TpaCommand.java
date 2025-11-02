package com.floorplugins.essentialsz.commands;

import com.floorplugins.essentialsz.utils.MessageUtils;
import com.floorplugins.essentialsz.utils.TpaRequest;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TpaCommand implements BasicCommand {
    public final JavaPlugin plugin;
    public final MessageUtils messageUtils;
    public final List<String> subCommands = List.of("accept", "deny");
    public final Map<String, List<String>> requests = new HashMap<>();
    public final Map<String, TpaRequest> activeRequests = new HashMap<>();
    public final MiniMessage mm = MiniMessage.miniMessage();

    public TpaCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(this.plugin);
    }

    @Override
    public void execute(CommandSourceStack source, String @NotNull [] args) {
        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        CommandSender sender = source.getSender();
        FileConfiguration config = plugin.getConfig();
        int timeout = config.getInt("tpa.timeout-seconds", 120);

        if (!(sender instanceof Player player)) {
            send(sender, "error", "only-players", null);
            return;
        }

        if (args.length < 1) {
            send(player, "error", "specify-player", Map.of("subcommands", String.join("/", subCommands)));
            return;
        }

        if (args[0].equals(player.getName())) {
            send(player, "error", "teleport-self", null);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "accept":
                if (args.length < 2) {
                    send(player, "error", "specify-player", Map.of("subcommands", String.join("/", subCommands)));
                    return;
                }

                if (!requests.containsKey(player.getName()) || requests.get(player.getName()).isEmpty()) {
                    requests.put(player.getName(), new ArrayList<>());
                    send(player, "error", "no-requests", Map.of("action", "accept"));
                    return;
                }

                if (!players.contains(args[1])) {
                    send(player, "error", "offline-player", null);
                    return;
                }

                Player requestFromA = Bukkit.getPlayer(args[1]);

                if (requestFromA == null) {
                    send(player, "error", "offline-player", null);
                    return;
                }

                requests.get(player.getName()).remove(requestFromA.getName());
                send(requestFromA, "success", "request-accepted-by", Map.of("player", player.getName()));
                send(player, "success", "request-accepted", Map.of("player", requestFromA.getName()));
                requestFromA.teleport(player);
                return;
            case "deny":
                if (args.length < 2) {
                    send(player, "error", "specify-player", Map.of("subcommands", String.join("/", subCommands)));
                    return;
                }

                if (!requests.containsKey(player.getName()) || requests.get(player.getName()).isEmpty()) {
                    requests.put(player.getName(), new ArrayList<>());
                    send(player, "error", "no-requests", Map.of("action", "deny"));
                    return;
                }

                if (!players.contains(args[1])) {
                    send(player, "error", "offline-player", null);
                    return;
                }

                Player requestFromB = Bukkit.getPlayer(args[1]);

                if (requestFromB == null) {
                    send(player, "error", "offline-player", null);
                    return;
                }

                requests.get(player.getName()).remove(requestFromB.getName());
                send(requestFromB, "error", "request-denied-by", Map.of("player", player.getName()));
                send(player, "success", "request-denied", Map.of("player", requestFromB.getName()));
                return;
        }

        if (!players.contains(args[0])) {
            send(player, "error", "offline-player", null);
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            send(player, "error", "offline-player", null);
            return;
        }

        if (requests.containsKey(targetName)) {
            List<String> existing = new ArrayList<>(requests.get(targetName));

            for (String requester : existing) {
                if (requester.equalsIgnoreCase(player.getName())) {
                    requests.get(targetName).remove(requester);
                    Player old = Bukkit.getPlayer(requester);

                    if (old != null)
                        send(old, "error", "request-canceled-old", Map.of("player", targetName));
                }
            }
        } else {
            requests.put(targetName, new ArrayList<>());
        }

        TpaRequest tpaRequest = new TpaRequest(player, target, System.currentTimeMillis() + timeout * 1000L);
        activeRequests.put(targetName + ":" + player.getName(), tpaRequest);
        requests.get(targetName).add(player.getName());
        send(player, "success", "request-sent", Map.of(
                "player", targetName,
                "timeout", timeout + " seconds"
        ));
        send(target, "success", "request-received", Map.of(
                "player", player.getName(),
                "timeout", timeout + " seconds"
        ));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TpaRequest req = activeRequests.get(targetName + ":" + player.getName());

            if (req != null) {
                activeRequests.remove(targetName + ":" + player.getName());
                requests.getOrDefault(targetName, new ArrayList<>()).remove(player.getName());
                send(player, "error", "request-denied-by", Map.of("player", targetName));
                send(target, "error", "request-denied", Map.of("player", player.getName()));
            }
        }, timeout * 20L);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, String @NotNull [] args) {
        if (!(source.getSender() instanceof Player player))
            return Collections.emptyList();

        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();

        if (args.length == 0) {
            List<String> suggestions = new ArrayList<>(players);
            suggestions.addAll(subCommands);
            return suggestions;
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(players);
            suggestions.addAll(subCommands);
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();

            if (subCommands.contains(sub)) {
                return requests.getOrDefault(player.getName(), Collections.emptyList());
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String permission() {
        return "essentialsz.commands.tpa";
    }

    private void send(CommandSender sender, String type, String path, Map<String, String> replace) {
        sender.sendMessage(mm.deserialize(messageUtils.withFormat(type, messageUtils.get("tpa", path, replace))));
    }
}
