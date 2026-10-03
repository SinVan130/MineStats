package com.example.examplemod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ExampleMod.MODID);

    public static final Supplier<AttachmentType<PityData>> PITY =
            ATTACHMENTS.register("pity", () -> AttachmentType.builder(() -> new PityData()).build());

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}