package pro.trevor.pdp;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameType;

import java.util.Date;
import java.util.List;

public class Util {

    public static ItemStack getPlayerHeadItemStack(Player player) {
        ItemStack headItemStack = new ItemStack(Items.PLAYER_HEAD);
        headItemStack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        return headItemStack;
    }

    public static ItemStack getPlayerHeadItemStack(Player player, String lore, boolean indestructible) {
        ItemStack headItemStack = getPlayerHeadItemStack(player);
        headItemStack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(lore))));
        if (indestructible) {
            headItemStack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
            headItemStack.remove(DataComponents.DAMAGE);
        }
        return headItemStack;
    }

    public static MinecraftServer getServerAndWarnIfNull(Entity entity) {
        MinecraftServer server = entity.getServer();
        if (server == null) {
            ModMain.LOGGER.error("Server is null!");
        }
        return server;
    }

    public static void banPlayerWithMeaningfulDuration(Player player, String reason, long hours) {
        if (hours < 0) {
            ModMain.LOGGER.error("Invalid number of hours to ban player: {}", hours);
        } else if (hours == 0) {
            banPlayer(player, reason);
        } else {
            tempBanPlayer(player, reason, hours);
        }
    }

    public static void banPlayer(Player player, String reason) {
        ModMain.LOGGER.info("Banning player '{}'", player.getName().getString());

        MinecraftServer server = getServerAndWarnIfNull(player);
        if (server == null) {
            return;
        }

        UserBanList banList = server.getPlayerList().getBans();
        GameProfile profile = player.getGameProfile();

        if (banList.isBanned(profile)) {
            ModMain.LOGGER.warn("Player '{}' is already banned!", player.getName().getString());
            return;
        }

        UserBanListEntry newEntry = new UserBanListEntry(profile, new Date(), ModMain.MOD_ID, null, reason);
        banList.add(newEntry);
    }

    public static void tempBanPlayer(Player player, String reason, long hours) {
        ModMain.LOGGER.info("Temporarily banning player '{}'", player.getName().getString());

        MinecraftServer server = getServerAndWarnIfNull(player);
        if (server == null) {
            return;
        }

        UserBanList banList = server.getPlayerList().getBans();
        GameProfile profile = player.getGameProfile();

        if (banList.isBanned(profile)) {
            ModMain.LOGGER.warn("Player '{}' is already banned! Resetting ban duration.", player.getName().getString());
        }

        Date now = new Date();
        Date expiration = new Date(now.getTime() + hours * 60 * 60 * 1000);

        UserBanListEntry newEntry = new UserBanListEntry(profile, now, ModMain.MOD_ID, expiration, reason);
        banList.add(newEntry);
    }

    public static void broadcastMessage(MinecraftServer server, Component message) {
        if (server != null) {
            server.getPlayerList().getPlayers().forEach((player) -> player.sendSystemMessage(message));
        }
    }

    public static void broadcastMessageToPlayer(MinecraftServer server, Player player, Component message) {
        if (server != null && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message);
        }
    }

    public static void addTtlCustomData(ItemStack stack, long ttl) {
        CompoundTag tag;
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        } else {
            tag = new CompoundTag();
        }
        tag.putLong("ttl", ttl);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static long getTtlCustomData(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            return stack.get(DataComponents.CUSTOM_DATA).copyTag().getLong("ttl");
        } else {
            ModMain.LOGGER.warn("TTL is not set!");
            return 0;
        }
    }


    public static GameType setGameMode(ServerPlayer player, GameType gameMode) {
        GameType previousGameMode = player.gameMode.getGameModeForPlayer();
        player.gameMode.changeGameModeForPlayer(gameMode);
        return previousGameMode;
    }
}
