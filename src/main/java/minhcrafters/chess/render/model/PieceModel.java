package minhcrafters.chess.render.model;

import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import java.util.List;

public interface PieceModel {
    /**
     * Spawns the piece model at the given position.
     * @param world The world to spawn in
     * @param pos The center position of the square (bottom center)
     * @param isWhite Whether the piece is white
     * @return List of spawned entities
     */
    List<DisplayEntity.BlockDisplayEntity> spawn(ServerWorld world, Vec3d pos, boolean isWhite);
}
