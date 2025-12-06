package minhcrafters.chess.render.model;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PawnModel extends AbstractPieceModel {
    @Override
    public List<DisplayEntity.BlockDisplayEntity> spawn(ServerWorld world, Vec3d pos, boolean isWhite) {
        List<DisplayEntity.BlockDisplayEntity> parts = new ArrayList<>();
        
        BlockState mainMaterial = isWhite ? Blocks.BONE_BLOCK.getDefaultState() : Blocks.COAL_BLOCK.getDefaultState();
        BlockState secondaryMaterial = isWhite ? Blocks.QUARTZ_BLOCK.getDefaultState() : Blocks.BLACK_CONCRETE.getDefaultState();

        // Base
        parts.add(createPart(world, pos, 0, 0, 0, 0.5f, 0.15f, 0.5f, mainMaterial));
        
        // Body
        parts.add(createPart(world, pos, 0, 0.15f, 0, 0.3f, 0.35f, 0.3f, mainMaterial));
        
        // Collar
        parts.add(createPart(world, pos, 0, 0.5f, 0, 0.35f, 0.05f, 0.35f, secondaryMaterial));
        
        // Head
        parts.add(createPart(world, pos, 0, 0.55f, 0, 0.25f, 0.25f, 0.25f, mainMaterial));

        return parts;
    }
}
