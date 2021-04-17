package com.williambl.defaultspawnpoints;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface DefaultSpawnPointOwner {
    @Nullable BlockPos getDefaultBlockPos();
    @Nullable Float getDefaultYaw();

    void setDefaultBlockPos(BlockPos pos);
    void setDefaultYaw(Float value);
}
