package minhcrafters.chess.render;

import minhcrafters.chess.game.*;
import minhcrafters.chess.render.model.*;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.*;

public class ChessBoardRenderer {
    private final Map<UUID, List<DisplayEntity.BlockDisplayEntity>> gameEntities;
    private final Map<Piece.PieceType, PieceModel> pieceModels;

    public ChessBoardRenderer() {
        this.gameEntities = new HashMap<>();
        this.pieceModels = new HashMap<>();

        pieceModels.put(Piece.PieceType.PAWN, new PawnModel());
        pieceModels.put(Piece.PieceType.ROOK, new RookModel());
        pieceModels.put(Piece.PieceType.KNIGHT, new KnightModel());
        pieceModels.put(Piece.PieceType.BISHOP, new BishopModel());
        pieceModels.put(Piece.PieceType.QUEEN, new QueenModel());
        pieceModels.put(Piece.PieceType.KING, new KingModel());
    }

    public void renderBoard(ServerWorld world, ChessGame game, BlockPos boardCenter) {
        UUID gameId = game.getGameId();

        // Clear existing entities if any
        clearBoard(world, gameId);

        List<DisplayEntity.BlockDisplayEntity> entities = new ArrayList<>();

        // Render board squares
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isLightSquare = (row + col) % 2 == 0;
                DisplayEntity.BlockDisplayEntity square = createSquare(world, boardCenter, row, col, isLightSquare);
                entities.add(square);
            }
        }

        // Render pieces
        Board board = game.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    List<DisplayEntity.BlockDisplayEntity> pieceEntities = createPiece(world, boardCenter, row, col,
                            piece);
                    entities.addAll(pieceEntities);
                }
            }
        }

        gameEntities.put(gameId, entities);
    }

    private DisplayEntity.BlockDisplayEntity createSquare(ServerWorld world, BlockPos boardCenter, int row, int col,
            boolean isLight) {
        Vec3d pos = new Vec3d(
                boardCenter.getX() + col,
                boardCenter.getY(),
                boardCenter.getZ() + row);

        DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(
                net.minecraft.entity.EntityType.BLOCK_DISPLAY,
                world);

        entity.setPosition(pos);
        entity.setBlockState(
                isLight ? Blocks.QUARTZ_BLOCK.getDefaultState() : Blocks.DEEPSLATE_TILES.getDefaultState());

        // Set scale to fill the square (1x0.1x1 blocks)
        entity.setTransformation(new net.minecraft.util.math.AffineTransformation(
                null,
                null,
                new Vector3f(1.0f, 0.1f, 1.0f),
                null));

        world.spawnEntity(entity);
        return entity;
    }

    private List<DisplayEntity.BlockDisplayEntity> createPiece(ServerWorld world, BlockPos boardCenter, int row,
            int col,
            Piece piece) {
        Vec3d pos = new Vec3d(
                boardCenter.getX() + col + 0.5,
                boardCenter.getY() + 0.1,
                boardCenter.getZ() + row + 0.5);

        PieceModel model = pieceModels.get(piece.getType());

        if (model != null) {
            return model.spawn(world, pos, piece.getColor() == Piece.PieceColor.WHITE);
        }

        return new ArrayList<>();
    }

    public void updatePiece(ServerWorld world, ChessGame game, BlockPos boardCenter, int row, int col) {
        UUID gameId = game.getGameId();
        List<DisplayEntity.BlockDisplayEntity> entities = gameEntities.get(gameId);

        if (entities == null) {
            return;
        }

        // Remove old piece entity at this position
        Vec3d targetPos = new Vec3d(
                boardCenter.getX() + col + 0.5,
                boardCenter.getY() + 0.1,
                boardCenter.getZ() + row + 0.5);

        entities.removeIf(entity -> {
            Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            // Relaxed check to cover the whole square and height of pieces
            if (Math.abs(entityPos.x - targetPos.x) < 0.5 &&
                    Math.abs(entityPos.y - targetPos.y) < 2.0 &&
                    Math.abs(entityPos.z - targetPos.z) < 0.5) {
                entity.discard();
                return true;
            }
            return false;
        });

        // Add new piece if there is one
        Piece piece = game.getBoard().getPiece(row, col);
        if (piece != null) {
            List<DisplayEntity.BlockDisplayEntity> pieceEntities = createPiece(world, boardCenter, row, col, piece);
            entities.addAll(pieceEntities);
        }
    }

    public void clearBoard(ServerWorld world, UUID gameId) {
        List<DisplayEntity.BlockDisplayEntity> entities = gameEntities.remove(gameId);
        if (entities != null) {
            for (DisplayEntity.BlockDisplayEntity entity : entities) {
                entity.discard();
            }
        }
    }

    public void highlightSquares(ServerWorld world, ChessGame game, BlockPos boardCenter, List<int[]> squares) {
        // Add highlight entities for valid move squares
        UUID gameId = game.getGameId();
        List<DisplayEntity.BlockDisplayEntity> entities = gameEntities.get(gameId);

        if (entities == null) {
            return;
        }

        for (int[] square : squares) {
            int row = square[0];
            int col = square[1];

            Vec3d pos = new Vec3d(
                    boardCenter.getX() + col,
                    boardCenter.getY() + 0.11,
                    boardCenter.getZ() + row);

            DisplayEntity.BlockDisplayEntity highlight = new DisplayEntity.BlockDisplayEntity(
                    net.minecraft.entity.EntityType.BLOCK_DISPLAY,
                    world);

            highlight.setPosition(pos);
            highlight.setBlockState(Blocks.LIME_STAINED_GLASS.getDefaultState());

            highlight.setTransformation(new net.minecraft.util.math.AffineTransformation(
                    null,
                    null,
                    new Vector3f(0.8f, 0.05f, 0.8f),
                    null));

            world.spawnEntity(highlight);
            entities.add(highlight);
        }
    }

    public void clearHighlights(ServerWorld world, UUID gameId, BlockPos boardCenter) {
        List<DisplayEntity.BlockDisplayEntity> entities = gameEntities.get(gameId);

        if (entities == null) {
            return;
        }

        // Remove highlight entities (glass blocks)
        entities.removeIf(entity -> {
            if (entity.getBlockState().getBlock() == Blocks.LIME_STAINED_GLASS) {
                entity.discard();
                return true;
            }
            return false;
        });
    }
}
