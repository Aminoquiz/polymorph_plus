package com.shiver.polymorphlegacy.core;

import com.google.common.eventbus.EventBus;
import java.util.Collections;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

/** Registers the mod id {@code polymorph}, so mods that test for Polymorph find Polymorph+. */
public final class PolymorphAliasContainer extends DummyModContainer {
    public PolymorphAliasContainer() {
        super(metadata());
    }

    private static ModMetadata metadata() {
        ModMetadata meta = new ModMetadata();
        meta.modId = "polymorph";
        meta.name = "Polymorph (Polymorph+ alias)";
        meta.version = "1.3.2";
        meta.parent = "polymorph_plus";
        meta.description = "Alias of Polymorph+ for mods that look for the id polymorph.";
        meta.authorList = Collections.singletonList("Aminoquiz");
        return meta;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
