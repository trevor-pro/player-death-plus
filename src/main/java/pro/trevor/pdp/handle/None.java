package pro.trevor.pdp.handle;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public class None implements Handle {

    @Override
    public String key() {
        return "none";
    }

    @Override
    public void register() {
        // Intentionally left blank
    }

    @Override
    public void unregister() {
        // Intentionally left blank
    }

    @Override
    public void handleDeath(Player player, DamageSource source) {
        // Intentionally left blank
    }
}
