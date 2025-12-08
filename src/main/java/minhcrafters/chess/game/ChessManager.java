package minhcrafters.chess.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import minhcrafters.chess.Chess;

public class ChessManager {
    private static ChessManager instance;
    private final Map<UUID, ChessGame> games;
    private final Map<UUID, GameLocation> gameLocations;
    private final Map<UUID, UUID> playerToGame;
    private final Map<BlockPos, UUID> boardPositionToGame;

    private ChessManager() {
        this.games = new ConcurrentHashMap<>();
        this.gameLocations = new ConcurrentHashMap<>();
        this.playerToGame = new ConcurrentHashMap<>();
        this.boardPositionToGame = new ConcurrentHashMap<>();
    }

    public static ChessManager getInstance() {
        if (instance == null) {
            instance = new ChessManager();
        }
        return instance;
    }

    public ChessGame createGame(BlockPos boardCenter, String worldId) {
        UUID gameId = UUID.randomUUID();
        ChessGame game = new ChessGame(gameId);

        games.put(gameId, game);
        gameLocations.put(gameId, new GameLocation(boardCenter, worldId));
        boardPositionToGame.put(boardCenter, gameId);

        return game;
    }

    public ChessGame getGame(UUID gameId) {
        return games.get(gameId);
    }

    public ChessGame getGameByPlayer(UUID playerId) {
        UUID gameId = playerToGame.get(playerId);
        return gameId != null ? games.get(gameId) : null;
    }

    public ChessGame getGameAtPosition(BlockPos pos) {
        UUID gameId = boardPositionToGame.get(pos);
        return gameId != null ? games.get(gameId) : null;
    }

    public GameLocation getGameLocation(UUID gameId) {
        return gameLocations.get(gameId);
    }

    public void assignPlayer(UUID gameId, UUID playerId, Piece.PieceColor color) {
        ChessGame game = games.get(gameId);
        if (game != null) {
            if (color == Piece.PieceColor.WHITE) {
                game.setWhitePlayer(playerId);
            } else {
                game.setBlackPlayer(playerId);
            }
            playerToGame.put(playerId, gameId);
        }
    }

    public void removeGame(UUID gameId) {
        ChessGame game = games.remove(gameId);
        if (game != null) {
            GameLocation location = gameLocations.remove(gameId);
            if (location != null) {
                boardPositionToGame.remove(location.boardCenter);
            }

            if (game.getWhitePlayer() != null) {
                playerToGame.remove(game.getWhitePlayer());
            }
            if (game.getBlackPlayer() != null) {
                playerToGame.remove(game.getBlackPlayer());
            }

            game.closeAi();
        }
    }

    public Collection<ChessGame> getAllGames() {
        return games.values();
    }

    public int[] worldToBoard(Vec3d worldPos, BlockPos boardCenter) {
        // Convert world coordinates to board coordinates (0-7, 0-7)
        double relX = worldPos.x - boardCenter.getX();
        double relZ = worldPos.z - boardCenter.getZ();

        int col = 7 - (int) Math.floor(relX);
        int row = (int) Math.floor(relZ);

        if (col >= 0 && col < 8 && row >= 0 && row < 8) {
            return new int[] { row, col };
        }

        return null;
    }

    public Vec3d boardToWorld(int row, int col, BlockPos boardCenter) {
        // Convert board coordinates to world coordinates
        // Center of each square
        return new Vec3d(
                boardCenter.getX() + (7 - col) + 0.5,
                boardCenter.getY() + 0.5,
                boardCenter.getZ() + row + 0.5);
    }

    public void tick(MinecraftServer server) {
        long worldTime = server.getTicks();

        for (ChessGame game : games.values()) {
            game.tick(worldTime);
            Chess.getRenderer().updateTimers(game);
        }
    }

    public static class GameLocation {
        private final BlockPos boardCenter;
        private final String worldId;

        public GameLocation(BlockPos boardCenter, String worldId) {
            this.boardCenter = boardCenter;
            this.worldId = worldId;
        }

        public BlockPos getBoardCenter() {
            return boardCenter;
        }

        public String getWorldId() {
            return worldId;
        }
    }
}
