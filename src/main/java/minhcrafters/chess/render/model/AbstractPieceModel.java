package minhcrafters.chess.render.model;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public abstract class AbstractPieceModel implements PieceModel {

    protected DisplayEntity.BlockDisplayEntity createPart(ServerWorld world, Vec3d rootPos, float offsetX,
            float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, BlockState state) {
        DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);

        // Position relative to the root position of the piece
        entity.setPosition(rootPos.add(offsetX, offsetY, offsetZ));
        entity.setBlockState(state);

        // Center the block on X and Z, but keep Y base at 0 (so it sits on the offset)
        entity.setTransformation(new AffineTransformation(
                new Vector3f(-sizeX / 2, 0, -sizeZ / 2),
                null,
                new Vector3f(sizeX, sizeY, sizeZ),
                null));

        world.spawnEntity(entity);
        return entity;
    }
}
