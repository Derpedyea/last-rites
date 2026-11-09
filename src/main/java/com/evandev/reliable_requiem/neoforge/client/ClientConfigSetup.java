package com.evandev.reliable_requiem.neoforge.client;

//? if neoforge {
/*import com.evandev.reliable_requiem.config.YaclConfigIntegration;
import com.evandev.reliable_requiem.platform.Services;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ClientConfigSetup {
    public static void register(ModContainer container) {
        if (!Services.PLATFORM.isModLoaded("yet_another_config_lib_v3")) return;
        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> YaclConfigIntegration.createScreen(parent));
    }
}
*///?}
