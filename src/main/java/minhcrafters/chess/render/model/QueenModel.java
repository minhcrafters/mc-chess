package minhcrafters.chess.render.model;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class QueenModel extends AbstractPieceModel {
    @Override
    public List<DisplayEntity.BlockDisplayEntity> spawn(ServerWorld world, Vec3d pos, boolean isWhite) {
        List<DisplayEntity.BlockDisplayEntity> parts = new ArrayList<>();
        
        BlockState mainMaterial = isWhite ? Blocks.DIAMOND_BLOCK.getDefaultState() : Blocks.OBSIDIAN.getDefaultState();
        BlockState secondaryMaterial = isWhite ? Blocks.QUARTZ_PILLAR.getDefaultState() : Blocks.CRYING_OBSIDIAN.getDefaultState();

        // Base
        parts.add(createPart(world, pos, 0, 0, 0, 0.6f, 0.15f, 0.6f, mainMaterial));
        
        // Lower Body
        parts.add(createPart(world, pos, 0, 0.15f, 0, 0.45f, 0.25f, 0.45f, secondaryMaterial));
        
        // Mid Body
        parts.add(createPart(world, pos, 0, 0.4f, 0, 0.35f, 0.4f, 0.35f, mainMaterial));
        
        // Collar
        parts.add(createPart(world, pos, 0, 0.8f, 0, 0.4f, 0.05f, 0.4f, secondaryMaterial));
        
        // Head
        parts.add(createPart(world, pos, 0, 0.85f, 0, 0.3f, 0.25f, 0.3f, mainMaterial));
        
        // Crown
        parts.add(createPart(world, pos, 0, 1.1f, 0, 0.35f, 0.1f, 0.35f, secondaryMaterial));
        
        // Crown points
        float pointOffset = 0.12f;
        float pointSize = 0.06f;
        float pointHeight = 0.12f;
        float crownY = 1.2f;
        
        parts.add(createPart(world, pos, pointOffset, crownY, pointOffset, pointSize, pointHeight, pointSize, mainMaterial));
        parts.add(createPart(world, pos, -pointOffset, crownY, pointOffset, pointSize, pointHeight, pointSize, mainMaterial));
        parts.add(createPart(world, pos, pointOffset, crownY, -pointOffset, pointSize, pointHeight, pointSize, mainMaterial));
        parts.add(createPart(world, pos, -pointOffset, crownY, -pointOffset, pointSize, pointHeight, pointSize, mainMaterial));
        
        // Center ball
        parts.add(createPart(world, pos, 0, 1.2f, 0, 0.12f, 0.12f, 0.12f, mainMaterial));

        return parts;
    }
}
