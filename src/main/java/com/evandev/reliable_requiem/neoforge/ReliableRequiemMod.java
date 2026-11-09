package com.evandev.reliable_requiem.neoforge;

//? if neoforge {
/*import com.evandev.reliable_requiem.CommonClass;
import com.evandev.reliable_requiem.Constants;
import com.evandev.reliable_requiem.modules.ModEffects;
import com.evandev.reliable_requiem.modules.ModItems;
import com.evandev.reliable_requiem.neoforge.client.ClientConfigSetup;
import com.evandev.reliable_requiem.platform.RegistryHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(Constants.MOD_ID)
public class ReliableRequiemMod {

    public ReliableRequiemMod(IEventBus modEventBus, ModContainer modContainer) {
        ModEffects.load();
        ModItems.load();
        RegistryHelper.init(modEventBus);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ReliableRequiemMod::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ReliableRequiemMod::onPlayerRespawn);

        //? if >=26.1 {
        if (FMLEnvironment.getDist().isClient()) {
        //?} else {
        /^if (FMLEnvironment.dist.isClient()) {
        ^///?}
            ClientConfigSetup.register(modContainer);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CommonClass.init();
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            CommonClass.onPlayerClone(oldPlayer, newPlayer, event.isWasDeath());
        }
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonClass.onPlayerRespawn(player);
        }
    }
}
*///?}
