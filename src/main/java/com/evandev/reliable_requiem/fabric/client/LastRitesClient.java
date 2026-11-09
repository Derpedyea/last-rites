package com.evandev.reliable_requiem.fabric.client;

//? if fabric {
import com.evandev.reliable_requiem.corpse.ragdoll.Ragdolls;
import com.evandev.reliable_requiem.fabric.CorpseNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class LastRitesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CorpseNetworking.FreshCorpse.TYPE,
                (payload, context) -> Ragdolls.markFresh(payload.entityId()));
    }
}
//?}
