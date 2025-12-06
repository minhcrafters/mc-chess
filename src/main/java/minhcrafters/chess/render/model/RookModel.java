package minhcrafters.chess.render.model;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class RookModel extends AbstractPieceModel {
    @Override
    public List<DisplayEntity.BlockDisplayEntity> spawn(ServerWorld world, Vec3d pos, boolean isWhite) {
        List<DisplayEntity.BlockDisplayEntity> parts = new ArrayList<>();
        
        BlockState mainMaterial = isWhite ? Blocks.SMOOTH_QUARTZ.getDefaultState() : Blocks.POLISHED_DEEPSLATE.getDefaultState();
        BlockState detailMaterial = isWhite ? Blocks.CHISELED_QUARTZ_BLOCK.getDefaultState() : Blocks.DEEPSLATE_TILES.getDefaultState();

        // Base
        parts.add(createPart(world, pos, 0, 0, 0, 0.6f, 0.15f, 0.6f, mainMaterial));
        
        // Body
        parts.add(createPart(world, pos, 0, 0.15f, 0, 0.45f, 0.5f, 0.45f, detailMaterial));
        
        // Top Rim
        parts.add(createPart(world, pos, 0, 0.65f, 0, 0.55f, 0.15f, 0.55f, mainMaterial));
        
        // Battlements (Corners)
        float cornerOffset = 0.2f;
        float battlementSize = 0.15f;
        float battlementHeight = 0.1f;
        float topY = 0.8f;
        
        parts.add(createPart(world, pos, cornerOffset, topY, cornerOffset, battlementSize, battlementHeight, battlementSize, mainMaterial));
        parts.add(createPart(world, pos, -cornerOffset, topY, cornerOffset, battlementSize, battlementHeight, battlementSize, mainMaterial));
        parts.add(createPart(world, pos, cornerOffset, topY, -cornerOffset, battlementSize, battlementHeight, battlementSize, mainMaterial));
        parts.add(createPart(world, pos, -cornerOffset, topY, -cornerOffset, battlementSize, battlementHeight, battlementSize, mainMaterial));

        return parts;
    }
}
