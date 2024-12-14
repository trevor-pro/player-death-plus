package pro.trevor.pdp;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ModMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public class Config
{

    public static final String DEFAULT_DEATH_HANDLER_KEY = "none";
    private static final long DEFAULT_BAN_DURATION_HOURS = 24L;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> DEATH_HANDLER_CONFIG = BUILDER
            .comment("The method used to handle a player's death")
            .comment("Options: ")
            .comment("'none' - No effect on death")
            .comment("'ban' - Bans a player on death, configurable to be a timed ban or a permanent ban")
            .comment("'head-ban' - The same as ban, but drops a head on death that can be used to unban the player")
            .define("method", DEFAULT_DEATH_HANDLER_KEY);

    private static final ModConfigSpec.LongValue BAN_DURATION_HOURS_CONFIG = BUILDER
            .comment("The number of hours to ban a player for")
            .comment("Only applicable if method chosen is ban of head-ban")
            .comment("Setting this number to zero (0) will result in a permanent ban")
            .defineInRange("ban-hours", DEFAULT_BAN_DURATION_HOURS, 0, Long.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static String DEATH_HANDLER_VALUE = DEFAULT_DEATH_HANDLER_KEY;
    public static long BAN_DURATION_HOURS = DEFAULT_BAN_DURATION_HOURS;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        DEATH_HANDLER_VALUE = DEATH_HANDLER_CONFIG.get();
        BAN_DURATION_HOURS = BAN_DURATION_HOURS_CONFIG.get();
    }
}
