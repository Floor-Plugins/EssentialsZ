package com.floorplugins.essentialsz.utils;

import org.bukkit.entity.Player;

public record TpaRequest(Player requester, Player target, long expireAt) {
}
