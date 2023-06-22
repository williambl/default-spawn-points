package com.williambl.defaultspawnpoints.mixin;

import com.mojang.authlib.GameProfile;
import com.williambl.defaultspawnpoints.DefaultSpawnPointOwner;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements DefaultSpawnPointOwner {
    @Unique BlockPos defaultBlockPos;
    @Unique Float defaultYaw;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    void defaultSpawnPoints$readDataFromNbt(NbtCompound tag, CallbackInfo ci) {
        if (tag.contains("DefaultSpawnPoint")) {
            NbtCompound dspTag = tag.getCompound("DefaultSpawnPoint");
            defaultBlockPos = getDefaultBlockPosFromTag(dspTag);
            defaultYaw = getDefaultYawFromTag(dspTag);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    void defaultSpawnPoints$writeDataToNbt(NbtCompound tag, CallbackInfo ci) {
        if (defaultBlockPos != null || defaultYaw != null) {
            NbtCompound dspTag = new NbtCompound();
            if (defaultBlockPos != null) {
                dspTag.putInt("X", defaultBlockPos.getX());
                dspTag.putInt("Y", defaultBlockPos.getY());
                dspTag.putInt("Z", defaultBlockPos.getZ());
            }
            if (defaultYaw != null) {
                dspTag.putFloat("Yaw", defaultYaw);
            }
            tag.put("DefaultSpawnPoint", dspTag);
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    void defaultSpawnPoints$copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        setDefaultBlockPos(((DefaultSpawnPointOwner)oldPlayer).getDefaultBlockPos());
        setDefaultYaw(((DefaultSpawnPointOwner)oldPlayer).getDefaultYaw());
    }

    @Unique
    private BlockPos getDefaultBlockPosFromTag(NbtCompound tag) {
        if (tag.contains("X") && tag.contains("Y") && tag.contains("Z")) {
            return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
        }
        return null;
    }

    @Unique
    private Float getDefaultYawFromTag(NbtCompound tag) {
        if (tag.contains("Yaw")) {
            return tag.getFloat("Yaw");
        }
        return null;
    }

    @Override
    public @Nullable BlockPos getDefaultBlockPos() {
        return defaultBlockPos;
    }

    @Override
    public @Nullable Float getDefaultYaw() {
        return defaultYaw;
    }

    @Override
    public void setDefaultBlockPos(BlockPos pos) {
        defaultBlockPos = pos;
    }

    @Override
    public void setDefaultYaw(Float value) {
        defaultYaw = value;
    }
}
