package com.footballwhispers.catalog.player;

public record PlayerListItem(Long playerId, String firstName, String lastName, String imageUrl) {
    public static PlayerListItem from(Player player) {
        Long id = player.getPlayer_id();
        return new PlayerListItem(id, player.getFirst_name(), player.getLast_name(),
                player.getImage_url() == null ? null : player.getImage_url().toExternalForm());
    }
}
