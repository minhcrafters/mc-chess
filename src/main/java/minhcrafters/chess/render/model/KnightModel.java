package minhcrafters.chess.render.model;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class KnightModel extends AbstractPieceModel {
    @Override
    public List<DisplayEntity.BlockDisplayEntity> spawn(ServerWorld world, Vec3d pos, boolean isWhite) {
        List<DisplayEntity.BlockDisplayEntity> parts = new ArrayList<>();
        
        BlockState mainMaterial = isWhite ? Blocks.CALCITE.getDefaultState() : Blocks.BASALT.getDefaultState();
        BlockState maneMaterial = isWhite ? Blocks.DIORITE.getDefaultState() : Blocks.BLACKSTONE.getDefaultState();

        // Base
        parts.add(createPart(world, pos, 0, 0, 0, 0.55f, 0.15f, 0.55f, mainMaterial));
        
        // Body (angled up?) - approximated with blocks
        parts.add(createPart(world, pos, 0, 0.15f, 0, 0.35f, 0.3f, 0.35f, mainMaterial));
        
        // Neck/Head base
        parts.add(createPart(world, pos, 0, 0.45f, -0.05f, 0.3f, 0.3f, 0.4f, mainMaterial));
        
        // Snout
        parts.add(createPart(world, pos, 0, 0.6f, -0.2f, 0.25f, 0.15f, 0.25f, mainMaterial));
        
        // Mane (back of neck)
        parts.add(createPart(world, pos, 0, 0.5f, 0.15f, 0.15f, 0.25f, 0.1f, maneMaterial));
        
        // Ears
        parts.add(createPart(world, pos, 0.08f, 0.75f, 0.05f, 0.05f, 0.1f, 0.05f, mainMaterial));
        parts.add(createPart(world, pos, -0.08f, 0.75f, 0.05f, 0.05f, 0.1f, 0.05f, mainMaterial));

        return parts;
    }
}
