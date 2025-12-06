package minhcrafters.chess.render.model;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class KingModel extends AbstractPieceModel {
    @Override
    public List<DisplayEntity.BlockDisplayEntity> spawn(ServerWorld world, Vec3d pos, boolean isWhite) {
        List<DisplayEntity.BlockDisplayEntity> parts = new ArrayList<>();
        
        BlockState mainMaterial = isWhite ? Blocks.GOLD_BLOCK.getDefaultState() : Blocks.NETHERITE_BLOCK.getDefaultState();
        BlockState secondaryMaterial = isWhite ? Blocks.RAW_GOLD_BLOCK.getDefaultState() : Blocks.ANCIENT_DEBRIS.getDefaultState();

        // Base
        parts.add(createPart(world, pos, 0, 0, 0, 0.6f, 0.15f, 0.6f, mainMaterial));
        
        // Lower Body
        parts.add(createPart(world, pos, 0, 0.15f, 0, 0.45f, 0.25f, 0.45f, secondaryMaterial));
        
        // Mid Body
        parts.add(createPart(world, pos, 0, 0.4f, 0, 0.35f, 0.45f, 0.35f, mainMaterial));
        
        // Collar
        parts.add(createPart(world, pos, 0, 0.85f, 0, 0.4f, 0.05f, 0.4f, secondaryMaterial));
        
        // Head
        parts.add(createPart(world, pos, 0, 0.9f, 0, 0.3f, 0.25f, 0.3f, mainMaterial));
        
        // Cross Base
        parts.add(createPart(world, pos, 0, 1.15f, 0, 0.1f, 0.1f, 0.1f, secondaryMaterial));
        
        // Cross Vertical
        parts.add(createPart(world, pos, 0, 1.25f, 0, 0.08f, 0.25f, 0.08f, mainMaterial));
        
        // Cross Horizontal
        parts.add(createPart(world, pos, 0, 1.35f, 0, 0.2f, 0.08f, 0.08f, mainMaterial));

        return parts;
    }
}
