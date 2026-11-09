package com.evandev.reliable_requiem;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
    // Registry/asset namespace, kept from Reliable Requiem so its items and effects survive a switch to Last Rites.
    public static final String MOD_ID = "reliable_requiem";
    public static final String MOD_NAME = "Last Rites";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return id(MOD_ID, path);
    }

    public static Identifier id(String namespace, String path) {
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new Identifier(namespace, path);
        *///?}
    }

    public static Identifier parseId(String id) {
        //? if >=1.21 {
        return Identifier.parse(id);
        //?} else {
        /*return new Identifier(id);
        *///?}
    }
}
