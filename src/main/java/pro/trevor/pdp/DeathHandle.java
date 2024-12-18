package pro.trevor.pdp;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import pro.trevor.pdp.handle.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

@EventBusSubscriber(modid = ModMain.MOD_NAME, bus = EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public class DeathHandle {

    // Death handler key -> Player death handle instance
    private static final HashMap<String, Handle> DEATH_HANDLES = new HashMap<>();

    private static Handle CURRENT_HANDLE = new None();

    public static boolean validateDeathHandlerKey(String key) {
        return DEATH_HANDLES.containsKey(key);
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(DeathHandle.class);
        registerDeathHandles(None.class, Ban.class, HeadBan.class, Spectator.class);
    }

    public static void primeDeathHandle(String key) {
        Handle newHandle = DEATH_HANDLES.get(key);
        if (CURRENT_HANDLE != newHandle) {
            CURRENT_HANDLE.unregister();
            newHandle.register();
            CURRENT_HANDLE = newHandle;
        }
    }

    public static <T extends Handle> void registerDeathHandle(Class<T> handleClass) {
        try {
            T handle = handleClass.getDeclaredConstructor().newInstance();
            String key = handle.key();

            if (DEATH_HANDLES.containsKey(key)) {
                ModMain.LOGGER.warn("Duplicate handle class name: '{}', overriding with the new value.", key);
            }

            DEATH_HANDLES.put(key, handle);
        } catch (NoSuchMethodException exception) {
            ModMain.LOGGER.error("Declared handle class '{}' does not have a default constructor", handleClass.getName());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            ModMain.LOGGER.error("Could not instantiate handle class '{}'", handleClass.getName());
            ModMain.LOGGER.error(exception.getMessage(), exception);
        }
    }

    @SafeVarargs
    private static void registerDeathHandles(Class<? extends Handle>... classes) {
        for (Class<? extends Handle> handleClass : classes) {
            registerDeathHandle(handleClass);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        MinecraftServer server = Util.getServerAndWarnIfNull(event.getEntity());

        if (server == null) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            if (validateDeathHandlerKey(Config.DEATH_HANDLER_VALUE)) {
                CURRENT_HANDLE.handleDeath(player, event.getSource());
            } else {
                ModMain.LOGGER.error("Invalid death handler: {}", Config.DEATH_HANDLER_VALUE);
            }
        }
    }
}
