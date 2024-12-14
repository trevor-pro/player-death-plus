package pro.trevor.pdp.handle;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import pro.trevor.pdp.Config;
import pro.trevor.pdp.Util;

public class Ban implements Handle {
    @Override
    public String key() {
        return "ban";
    }

    @Override
    public void register() {
        // Intentionally left blank
    }

    @Override
    public void handleDeath(Player player, DamageSource source) {
        Util.banPlayerWithMeaningfulDuration(player, "Banned due to dying", Config.BAN_DURATION_HOURS);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
        }
    }
}
