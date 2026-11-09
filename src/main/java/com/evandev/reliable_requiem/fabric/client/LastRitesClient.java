package com.evandev.reliable_requiem.fabric.client;

//? if fabric {
import com.evandev.reliable_requiem.corpse.ragdoll.CorpseRagdoll;
import com.evandev.reliable_requiem.fabric.CorpseNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class LastRitesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CorpseNetworking.FreshCorpse.TYPE,
                (payload, context) -> CorpseRagdoll.markFresh(payload.entityId()));
    }
}
//?}
