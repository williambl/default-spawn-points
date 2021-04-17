package com.williambl.defaultspawnpoints;

import com.williambl.defaultspawnpoints.mixin.ServerPlayerEntityAccessor;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.*;

import static net.minecraft.command.argument.AngleArgumentType.*;
import static net.minecraft.command.argument.BlockPosArgumentType.*;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.server.command.CommandManager.*;

public class DefaultSpawnPoints implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register((literal("defaultspawnpoint")
					.requires(Permissions.require("commands.defaultspawnpoint", 2))

					.then(
						literal("set")
							.executes(ctx -> setDefaultSpawnPoint(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayer()), new BlockPos(ctx.getSource().getPosition()), 0.0F))
							.then(
								(argument("targets", players())
								.executes(ctx -> setDefaultSpawnPoint(ctx.getSource(), getPlayers(ctx, "targets"), new BlockPos(ctx.getSource().getPosition()), 0.0F)))

								.then(
									(argument("pos", blockPos())
									.executes(ctx -> setDefaultSpawnPoint(ctx.getSource(), getPlayers(ctx, "targets"), getBlockPos(ctx, "pos"), 0.0F)))

									.then(
										argument("angle", angle())
										.executes(ctx -> setDefaultSpawnPoint(ctx.getSource(), getPlayers(ctx, "targets"), getBlockPos(ctx, "pos"), getAngle(ctx, "angle")))
									)
							)
						)
					)
					.then(
						literal("unset").then(
							(argument("targets", players())
							.executes(ctx -> unsetDefaultSpawnPoint(ctx.getSource(), getPlayers(ctx, "targets"))))
						)
					)
			));
		});
	}

	private int setDefaultSpawnPoint(ServerCommandSource source, Collection<ServerPlayerEntity> players, BlockPos pos, float yaw) {
		RegistryKey<World> worldKey = source.getWorld().getRegistryKey();

		for (ServerPlayerEntity player : players) {
			((ServerPlayerEntityAccessor)player).setSpawnPointDimension(worldKey);
			((DefaultSpawnPointOwner)player).setDefaultBlockPos(pos);
			((DefaultSpawnPointOwner)player).setDefaultYaw(yaw);
		}

		String worldName = worldKey.getValue().toString();
		if (players.size() == 1) {
			source.sendFeedback(new TranslatableText("commands.spawnpoint.success.single", pos.getX(), pos.getY(), pos.getZ(), yaw, worldName, players.stream().findFirst().orElseThrow(NullPointerException::new).getDisplayName()), true);
		} else {
			source.sendFeedback(new TranslatableText("commands.spawnpoint.success.multiple", pos.getX(), pos.getY(), pos.getZ(), yaw, worldName, players.size()), true);
		}

		return players.size();
	}

	private int unsetDefaultSpawnPoint(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
		for (ServerPlayerEntity player : players) {
			((DefaultSpawnPointOwner)player).setDefaultBlockPos(null);
			((DefaultSpawnPointOwner)player).setDefaultYaw(null);
		}

		if (players.size() == 1) {
			source.sendFeedback(new LiteralText("Unset default spawn point for ").append(players.stream().findFirst().orElseThrow(NullPointerException::new).getDisplayName()), true);
		} else {
			source.sendFeedback(new LiteralText(String.format("Unset default spawn points for %d players", players.size())), true);
		}

		return players.size();
	}
}
