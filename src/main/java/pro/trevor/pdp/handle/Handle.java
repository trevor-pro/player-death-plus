package pro.trevor.pdp.handle;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public interface Handle {
    String key();
    void register();
    void handleDeath(Player player, DamageSource source);
}
