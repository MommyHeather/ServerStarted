package computer.heather.serverstarted;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import computer.heather.serverstarted.config.ConfigManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("serverstarted")
public class ServerStarted
{

    public static final Logger LOGGER = LogUtils.getLogger();   


    private static int ticks = 0;


    public ServerStarted(IEventBus modEventBus)
    {
        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        ConfigManager.loadOrCreateConfig();
        
    }



    @SubscribeEvent
    public void onPostTick(ServerTickEvent.Post event) {
        ticks++;
        if (ticks == ConfigManager.delay.get() && ConfigManager.enabled.get()) {
            LOGGER.info(ConfigManager.message.get());
        }
    }


}
