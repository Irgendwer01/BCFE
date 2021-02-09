package ru.dbotthepony.mc.bcfe;

import net.minecraftforge.common.config.*;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.dbotthepony.mc.bcfe.BCFE;

import java.io.File;
import java.util.List;

@Mod.EventBusSubscriber(modid = BCFE.MODID)
@Config(modid = BCFE.MODID)
public class BCFEConfig
{
    @Config.Comment("Sets how many RF is 1 MJ (Standard 10)")
    @Config.Name("Ratio")
    @Config.RangeInt(min = 1)
    public static int RATIO = 10;

    @Mod.EventBusSubscriber
    private static class EventHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if(BCFE.MODID.equals(event.getModID()))
                ConfigManager.sync(BCFE.MODID, Config.Type.INSTANCE);
        }
    }
}