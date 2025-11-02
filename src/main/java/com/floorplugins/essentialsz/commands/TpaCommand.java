package com.floorplugins.essentialsz.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TpaCommand implements BasicCommand {
    public static List<String> subCommands = new ArrayList<>();

    static {
        subCommands.add("accept");
        subCommands.add("deny");
    }

    public Map<String, List<String>> requests = new HashMap<>();

    @Override
    public void execute(CommandSourceStack source, String @NotNull [] args) {
        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        CommandSender sender = source.getSender();

        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>Only players can teleport to others</red>");
            return;
        }

        if (args.length < 1) {
            player.sendRichMessage("<red>Please specify a player's name to teleport to, or " + String.join("/", subCommands) + "</red>");
            return;
        }

        if (args[0].equals(player.getName())) {
            sender.sendRichMessage("<red>You cannot teleport to yourself</red>");
            return;
        }

        switch (args[0]) {
            case "accept":
                if (args.length < 2) {
                    player.sendRichMessage("<red>Please specify the teleport request's player name to accept it");
                    return;
                }

                if (!requests.containsKey(player.getName()) || requests.get(player.getName()).isEmpty()) {
                    requests.put(player.getName(), new ArrayList<>());
                    sender.sendRichMessage("<red>You have no teleport requests to accept</red>");
                    return;
                }

                if (!players.contains(args[1])) {
                    sender.sendRichMessage("<red>That player is either offline or invalid</red>");
                    return;
                }

                Player requestFromA = Bukkit.getPlayer(args[1]);

                if (requestFromA == null) {
                    sender.sendRichMessage("<red>That player is either offline or invalid</red>");
                    return;
                }

                requests.get(player.getName()).remove(requestFromA.getName());
                requestFromA.sendRichMessage("<green>Your teleport request to " + player.getName() + " was accepted! You will be teleported shortly</green>");
                player.sendRichMessage("<green>Accepted teleport request from " + requestFromA.getName() + "</green>");
                requestFromA.teleport(player);
                return;
            case "deny":
                if (args.length < 2) {
                    player.sendRichMessage("<red>Please specify the teleport request's player name to deny it");
                    return;
                }

                if (!requests.containsKey(player.getName()) || requests.get(player.getName()).isEmpty()) {
                    requests.put(player.getName(), new ArrayList<>());
                    sender.sendRichMessage("<red>You have no teleport requests to deny</red>");
                    return;
                }

                if (!players.contains(args[1])) {
                    sender.sendRichMessage("<red>That player is either offline or invalid</red>");
                    return;
                }

                Player requestFromB = Bukkit.getPlayer(args[1]);

                if (requestFromB == null) {
                    sender.sendRichMessage("<red>That player is either offline or invalid</red>");
                    return;
                }

                requests.get(player.getName()).remove(requestFromB.getName());
                requestFromB.sendRichMessage("<red>Your teleport request to " + player.getName() + " was denied</red>");
                player.sendRichMessage("<green>Denied teleport request from " + requestFromB.getName() + "</green>");
                return;
        }

        if (!players.contains(args[0])) {
            sender.sendRichMessage("<red>That player is either offline or invalid</red>");
            return;
        }

        String targetName = args[0];

        if (requests.containsKey(targetName)) {
            List<String> existing = new ArrayList<>(requests.get(targetName));

            for (String requester : existing) {
                if (requester.equalsIgnoreCase(player.getName())) {
                    requests.get(targetName).remove(requester);
                    Player old = Bukkit.getPlayer(requester);

                    if (old != null)
                        old.sendRichMessage("<red>Your previous teleport request to " + targetName + " was canceled</red>");
                }
            }
        } else {
            requests.put(targetName, new ArrayList<>());
        }

        requests.get(targetName).add(player.getName());
        player.sendRichMessage("<green>Teleport request to " + args[0] + " sent! It is automatically denied after " + 120 + " seconds</green>");
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
}
