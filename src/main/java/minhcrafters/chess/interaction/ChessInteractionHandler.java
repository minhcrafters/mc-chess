package minhcrafters.chess.interaction;

import minhcrafters.chess.config.ChessConfig;
import minhcrafters.chess.game.*;
import minhcrafters.chess.render.ChessBoardRenderer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class ChessInteractionHandler {
    private final ChessBoardRenderer renderer;
    private final Map<UUID, SelectedPiece> selectedPieces;

    public ChessInteractionHandler(ChessBoardRenderer renderer) {
        this.renderer = renderer;
        this.selectedPieces = new HashMap<>();
    }

    public void register() {
        UseBlockCallback.EVENT.register(this::onUseBlock);
    }

    private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!(world instanceof ServerWorld serverWorld) || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        // Check if player clicked near a chess board
        BlockPos clickedPos = hitResult.getBlockPos();
        ChessGame game = findNearbyGame(clickedPos);

        if (game == null) {
            return ActionResult.PASS;
        }

        ChessManager manager = ChessManager.getInstance();
        ChessManager.GameLocation location = manager.getGameLocation(game.getGameId());

        if (location == null) {
            return ActionResult.PASS;
        }

        // Convert click position to board coordinates
        Vec3d clickPos = hitResult.getPos();
        int[] boardPos = manager.worldToBoard(clickPos, location.getBoardCenter());

        if (boardPos == null) {
            return ActionResult.PASS;
        }

        int row = boardPos[0];
        int col = boardPos[1];

        // Handle interaction
        handleBoardClick(serverWorld, serverPlayer, game, location.getBoardCenter(), row, col);

        return ActionResult.SUCCESS;
    }

    private ChessGame findNearbyGame(BlockPos pos) {
        ChessManager manager = ChessManager.getInstance();

        // Check if directly at a board position
        ChessGame game = manager.getGameAtPosition(pos);
        if (game != null) {
            return game;
        }

        // Check nearby positions (within 8 blocks, the size of a chess board)
        for (ChessGame g : manager.getAllGames()) {
            ChessManager.GameLocation location = manager.getGameLocation(g.getGameId());
            if (location != null) {
                BlockPos boardCenter = location.getBoardCenter();
                double distance = Math.sqrt(pos.getSquaredDistance(boardCenter));
                if (distance <= 12) {
                    return g;
                }
            }
        }

        return null;
    }

    private void handleBoardClick(ServerWorld world, ServerPlayerEntity player, ChessGame game, BlockPos boardCenter,
            int row, int col) {
        UUID playerId = player.getUuid();

        // Check if game is over
        if (game.getState() != ChessGame.GameState.ACTIVE) {
            player.sendMessage(Text.literal("§cGame is over!"), false);
            return;
        }

        // Check if it's player's turn
        if (!game.isPlayerTurn(playerId)) {
            player.sendMessage(Text.literal("§cIt's not your turn!"), false);
            return;
        }

        SelectedPiece selected = selectedPieces.get(playerId);

        if (selected == null) {
            // Try to select a piece
            Piece piece = game.getBoard().getPiece(row, col);

            if (piece != null && piece.getColor() == game.getCurrentPlayer()) {
                List<Move> legalMoves = game.getLegalMoves(row, col);

                if (!legalMoves.isEmpty()) {
                    selectedPieces.put(playerId, new SelectedPiece(row, col, game.getGameId()));
                    if (ChessConfig.HANDLER.instance().showOverlay) {
                        player.sendMessage(Text.literal("§aPiece selected. Click a highlighted square to move."), true);
                    }

                    // Highlight valid moves
                    List<int[]> validSquares = new ArrayList<>();
                    for (Move move : legalMoves) {
                        validSquares.add(new int[] { move.getToRow(), move.getToCol() });
                    }

                    if (ChessConfig.HANDLER.instance().showLegalMoves) {
                        renderer.highlightSquares(world, game, boardCenter, validSquares);
                    }
                } else {
                    if (ChessConfig.HANDLER.instance().showOverlay) {
                        player.sendMessage(Text.literal("§cThis piece has no legal moves."), true);
                    }
                }
            } else {
                if (ChessConfig.HANDLER.instance().showOverlay) {
                    player.sendMessage(Text.literal("§cSelect one of your pieces."), true);
                }
            }
        } else {
            // Try to move selected piece
            if (!selected.gameId.equals(game.getGameId())) {
                selectedPieces.remove(playerId);
                return;
            }

            renderer.clearHighlights(world, game.getGameId(), boardCenter);

            if (row == selected.row && col == selected.col) {
                // Deselect
                selectedPieces.remove(playerId);
                if (ChessConfig.HANDLER.instance().showOverlay) {
                    player.sendMessage(Text.literal("§7Piece deselected."), true);
                }
            } else {
                // Try to make the move
                boolean success = game.makeMove(selected.row, selected.col, row, col);

                if (success) {
                    selectedPieces.remove(playerId);

                    // Update the board visually
                    Move lastMove = game.getBoard().getLastMove();
                    
                    renderer.updatePiece(world, game, boardCenter, selected.row, selected.col);
                    renderer.updatePiece(world, game, boardCenter, row, col);

                    // Handle special moves
                    if (lastMove != null) {
                        if (lastMove.getMoveType() == Move.MoveType.CASTLE_KINGSIDE) {
                            renderer.updatePiece(world, game, boardCenter, row, 7);
                            renderer.updatePiece(world, game, boardCenter, row, 5);
                        } else if (lastMove.getMoveType() == Move.MoveType.CASTLE_QUEENSIDE) {
                            renderer.updatePiece(world, game, boardCenter, row, 0);
                            renderer.updatePiece(world, game, boardCenter, row, 3);
                        } else if (lastMove.getMoveType() == Move.MoveType.EN_PASSANT) {
                            renderer.updatePiece(world, game, boardCenter, selected.row, col);
                        }
                    }

                    // Notify players
                    String moveNotation = getMoveNotation(selected.row, selected.col, row, col);
                    broadcastToGame(world, game, Text.literal("§a" + moveNotation));

                    if (game.isInCheck()) {
                        broadcastToGame(world, game, Text.literal("§cCheck!"));
                        spawnCheckParticles(world, game, boardCenter);
                    }

                    // Check game state
                    if (game.getState() == ChessGame.GameState.WHITE_WINS) {
                        broadcastToGame(world, game, Text.literal("§6§lCheckmate! White wins!"));
                        spawnCheckmateParticles(world, game, boardCenter);
                    } else if (game.getState() == ChessGame.GameState.BLACK_WINS) {
                        broadcastToGame(world, game, Text.literal("§6§lCheckmate! Black wins!"));
                        spawnCheckmateParticles(world, game, boardCenter);
                    } else if (game.getState() == ChessGame.GameState.STALEMATE) {
                        broadcastToGame(world, game, Text.literal("§6§lStalemate! Draw!"));
                    } else {
                        String turn = game.getCurrentPlayer() == Piece.PieceColor.WHITE ? "White" : "Black";
                        broadcastToGame(world, game, Text.literal("§7" + turn + "'s turn"));
                    }
                } else {
                    selectedPieces.remove(playerId);
                    if (ChessConfig.HANDLER.instance().showOverlay) {
                        player.sendMessage(Text.literal("§cIllegal move!"), true);
                    }
                }
            }
        }
    }

    private String getMoveNotation(int fromRow, int fromCol, int toRow, int toCol) {
        char fromFile = (char) ('a' + fromCol);
        char toFile = (char) ('a' + toCol);
        return "" + fromFile + (fromRow + 1) + " → " + toFile + (toRow + 1);
    }

    private void broadcastToGame(ServerWorld world, ChessGame game, Text message) {
        UUID whitePlayer = game.getWhitePlayer();
        UUID blackPlayer = game.getBlackPlayer();

        if (whitePlayer != null) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(whitePlayer);
            if (player != null) {
                player.sendMessage(message, false);
            }
        }

        if (blackPlayer != null) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(blackPlayer);
            if (player != null) {
                player.sendMessage(message, false);
            }
        }
    }

    private void spawnCheckParticles(ServerWorld world, ChessGame game, BlockPos boardCenter) {
        int[] kingPos = game.getBoard().findKing(game.getCurrentPlayer());
        if (kingPos != null) {
            double x = boardCenter.getX() + kingPos[1] + 0.5;
            double y = boardCenter.getY() + 1.5;
            double z = boardCenter.getZ() + kingPos[0] + 0.5;

            world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 5, 0.3, 0.3, 0.3, 0.1);
            world.playSound(null, x, y, z, SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void spawnCheckmateParticles(ServerWorld world, ChessGame game, BlockPos boardCenter) {
        // Find the losing king
        int[] kingPos = game.getBoard().findKing(game.getCurrentPlayer());
        if (kingPos != null) {
            double x = boardCenter.getX() + kingPos[1] + 0.5;
            double y = boardCenter.getY() + 1.0;
            double z = boardCenter.getZ() + kingPos[0] + 0.5;

            world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 50, 0.5, 0.5, 0.5, 0.5);
            world.playSound(null, x, y, z, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private static class SelectedPiece {
        final int row;
        final int col;
        final UUID gameId;

        SelectedPiece(int row, int col, UUID gameId) {
            this.row = row;
            this.col = col;
            this.gameId = gameId;
        }
    }
}
