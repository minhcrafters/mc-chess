package minhcrafters.chess.render.model;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class BishopModel extends AbstractPieceModel {
    @Override
    public List<DisplayEntity.BlockDisplayEntity> spawn(ServerWorld world, Vec3d pos, boolean isWhite) {
        List<DisplayEntity.BlockDisplayEntity> parts = new ArrayList<>();
        
        BlockState mainMaterial = isWhite ? Blocks.DIORITE.getDefaultState() : Blocks.BLACKSTONE.getDefaultState();
        BlockState trimMaterial = isWhite ? Blocks.QUARTZ_BLOCK.getDefaultState() : Blocks.POLISHED_BLACKSTONE.getDefaultState();

        // Base
        parts.add(createPart(world, pos, 0, 0, 0, 0.55f, 0.15f, 0.55f, mainMaterial));
        
        // Lower Body
        parts.add(createPart(world, pos, 0, 0.15f, 0, 0.4f, 0.2f, 0.4f, mainMaterial));
        
        // Upper Body (tapering)
        parts.add(createPart(world, pos, 0, 0.35f, 0, 0.3f, 0.3f, 0.3f, mainMaterial));
        
        // Collar
        parts.add(createPart(world, pos, 0, 0.65f, 0, 0.35f, 0.05f, 0.35f, trimMaterial));
        
        // Head (Mitre)
        parts.add(createPart(world, pos, 0, 0.7f, 0, 0.25f, 0.3f, 0.25f, mainMaterial));
        
        // Top knob
        parts.add(createPart(world, pos, 0, 1.0f, 0, 0.1f, 0.1f, 0.1f, trimMaterial));

        return parts;
    }
}
