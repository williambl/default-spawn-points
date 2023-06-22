package com.williambl.defaultspawnpoints.mixin;

import com.williambl.defaultspawnpoints.DefaultSpawnPointOwner;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "respawnPlayer", slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0),
            to = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/world/ServerWorld;isSpaceEmpty(Lnet/minecraft/entity/Entity;)Z", ordinal = 0)
    ),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isSpaceEmpty(Lnet/minecraft/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    void defaultRespawnPoints$setRespawnPoint(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Vec3d> optional2, ServerWorld serverWorld2, ServerPlayerEntity newPlayer, boolean bl2) {
        if (!optional2.isPresent()) {
            DefaultSpawnPointOwner dspPlayer = (DefaultSpawnPointOwner) player;
            BlockPos defaultPos = dspPlayer.getDefaultBlockPos();
            Float defaultYaw = dspPlayer.getDefaultYaw();
            if (defaultPos != null && defaultYaw != null) {
                newPlayer.refreshPositionAndAngles(defaultPos, defaultYaw, 0.0f);

                while(!serverWorld2.isSpaceEmpty(newPlayer) && newPlayer.getY() < (double)serverWorld2.getTopY()) {
                    newPlayer.setPosition(newPlayer.getX(), newPlayer.getY() + 1.0, newPlayer.getZ());
                }
            }
        }
    }
}
