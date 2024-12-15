package pro.trevor.pdp.handle;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import pro.trevor.pdp.Util;

public class Spectator implements Handle {

    @Override
    public String key() {
        return "spectator";
    }

    @Override
    public void register() {
        // Intentionally left blank
    }

    @Override
    public void handleDeath(Player player, DamageSource source) {
        if (player instanceof ServerPlayer serverPlayer) {
            Util.setGameMode(serverPlayer, GameType.SPECTATOR);
        }
    }
}
