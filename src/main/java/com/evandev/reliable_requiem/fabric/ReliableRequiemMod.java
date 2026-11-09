package com.evandev.reliable_requiem.fabric;

//? if fabric {
import com.evandev.reliable_requiem.CommonClass;
import com.evandev.reliable_requiem.modules.ModEffects;
import com.evandev.reliable_requiem.modules.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class ReliableRequiemMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();
        ModEffects.load();
        ModItems.load();

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> CommonClass.onPlayerClone(oldPlayer, newPlayer, !alive));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> CommonClass.onPlayerRespawn(newPlayer));
    }
}
//?}
