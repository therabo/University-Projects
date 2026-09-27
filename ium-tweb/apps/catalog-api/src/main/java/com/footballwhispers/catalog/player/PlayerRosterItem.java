package com.footballwhispers.catalog.player;

public record PlayerRosterItem(Long playerId, String name, String imageUrl) {
    public static PlayerRosterItem from(Player player) {
        return new PlayerRosterItem(player.getPlayer_id(), player.getName(),
                player.getImage_url() == null ? null : player.getImage_url().toExternalForm());
    }
}
