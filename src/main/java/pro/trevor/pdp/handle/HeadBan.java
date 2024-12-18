package pro.trevor.pdp.handle;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import pro.trevor.pdp.Config;
import pro.trevor.pdp.ModMain;
import pro.trevor.pdp.Util;

import java.util.Date;

public class HeadBan implements Handle {
    @Override
    public String key() {
        return "head-ban";
    }

    @Override
    public void register() {
        NeoForge.EVENT_BUS.register(HeadBan.class);
    }

    @Override
    public void unregister() {
        NeoForge.EVENT_BUS.unregister(HeadBan.class);
    }

    @Override
    public void handleDeath(Player player, DamageSource source) {
        ItemStack head = Util.getPlayerHeadItemStack(player, "Right-click to unban player", true);

        long ttl = Long.MAX_VALUE;
        if (Config.BAN_DURATION_HOURS != 0) {
            ttl = System.currentTimeMillis() + (Config.BAN_DURATION_HOURS * 60 * 60 * 1000);
        }

        Util.addTtlCustomData(head, ttl);

        if (source.getEntity() instanceof ServerPlayer playerCause) {
            playerCause.spawnAtLocation(head);
        } else {
            player.spawnAtLocation(head);
        }

        Util.banPlayerWithMeaningfulDuration(player, "Banned due to dying", Config.BAN_DURATION_HOURS);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
        }
    }

    private static void handleRightClick(Player player, ItemStack stack, ICancellableEvent sourceEvent) {
        MinecraftServer server = Util.getServerAndWarnIfNull(player);
        if (server == null) {
            return;
        }

        // Ensure we are working with a player head, that it has an attached profile
        if (stack.is(Items.PLAYER_HEAD) && stack.has(DataComponents.PROFILE)) {

            GameProfile profile = stack.get(DataComponents.PROFILE).gameProfile();

            // Check that the head's ttl has not expired
            Date now = new Date();
            Date ttl = new Date(Util.getTtlCustomData(stack));
            if (now.before(ttl)) {
                ModMain.LOGGER.info("Unbanning player {}", profile.getName());
                UserBanList banList = server.getPlayerList().getBans();
                if (banList.isBanned(profile)) {
                    banList.remove(profile);
                }

                Component message = Component.literal(String.format("%s has been unbanned", profile.getName()));
                Util.broadcastMessage(server, message);
            } else {
                ModMain.LOGGER.info("{} attempted to unban player {} after item expiry",
                        player.getName().getString(),
                        profile.getName());
                Component message = Component.literal(String.format("Head for %s has expired", profile.getName()));
                Util.broadcastMessageToPlayer(server, player, message);
            }

            stack.setCount(0);
            sourceEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleRightClick(event.getEntity(), event.getItemStack(), event);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        handleRightClick(event.getEntity(), event.getItemStack(), event);
    }
}
