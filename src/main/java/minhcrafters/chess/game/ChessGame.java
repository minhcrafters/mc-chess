package minhcrafters.chess.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import minhcrafters.chess.config.ChessConfig;
import minhcrafters.chess.game.ai.UciEngine;
import minhcrafters.chess.game.util.FenUtils;

public class ChessGame {
    private final UUID gameId;
    private final Board board;
    private Piece.PieceColor currentPlayer;
    private GameState state;
    private UUID whitePlayer;
    private UUID blackPlayer;

    private long whiteTime;
    private long blackTime;
    private long lastTickTime;
    private boolean timerStarted;

    public ChessGame(UUID gameId) {
        this.gameId = gameId;
        this.board = new Board();
        this.currentPlayer = Piece.PieceColor.WHITE;
        this.state = GameState.ACTIVE;

        this.whiteTime = ChessConfig.HANDLER.instance().defaultTimeSeconds * 20L;
        this.blackTime = ChessConfig.HANDLER.instance().defaultTimeSeconds * 20L;
        this.lastTickTime = 0;
        this.timerStarted = false;
    }

    public UUID getGameId() {
        return gameId;
    }

    public Board getBoard() {
        return board;
    }

    public Piece.PieceColor getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getState() {
        return state;
    }

    public UUID getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(UUID whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public UUID getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(UUID blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public boolean isPlayerTurn(UUID playerId) {
        if (currentPlayer == Piece.PieceColor.WHITE) {
            return playerId.equals(whitePlayer);
        } else {
            return playerId.equals(blackPlayer);
        }
    }

    public List<Move> getLegalMoves(int row, int col) {
        List<Move> pseudoLegalMoves = board.getLegalMoves(row, col, currentPlayer);
        List<Move> legalMoves = new ArrayList<>();

        for (Move move : pseudoLegalMoves) {
            if (isMoveLegal(move)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    private boolean isMoveLegal(Move move) {
        // Make the move temporarily
        Board testBoard = board.copy();
        applyMoveToBoard(testBoard, move);

        // Check if king is in check after the move
        int[] kingPos = testBoard.findKing(currentPlayer);
        if (kingPos == null)
            return false;

        boolean inCheck = testBoard.isSquareAttacked(kingPos[0], kingPos[1], currentPlayer.opposite());
        return !inCheck;
    }

    public UciEngine aiEngine;

    private Piece.PieceColor aiColor;
    private BiConsumer<Move, ChessGame> onAiMove;

    public void setAi(String enginePath, Piece.PieceColor aiColor, BiConsumer<Move, ChessGame> onAiMove) {
        this.aiEngine = new UciEngine(enginePath);
        this.aiColor = aiColor;
        this.onAiMove = onAiMove;
        try {
            this.aiEngine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (currentPlayer == aiColor) {
            triggerAiMove();
        }
    }

    private void triggerAiMove() {
        if (aiEngine == null)
            return;

        long wtime = whiteTime * 50;
        long btime = blackTime * 50;
        long inc = ChessConfig.HANDLER.instance().incrementSeconds * 1000L;

        aiEngine.getBestMove(FenUtils.getFen(board, currentPlayer), wtime, btime, inc, inc)
                .thenAccept(uciMove -> {
                    if (uciMove != null) {
                        makeAiMove(uciMove);
                    }
                });
    }

    private void makeAiMove(String uciMove) {
        if (uciMove.length() < 4)
            return;
        int fromCol = uciMove.charAt(0) - 'a';
        int fromRow = uciMove.charAt(1) - '1';
        int toCol = uciMove.charAt(2) - 'a';
        int toRow = uciMove.charAt(3) - '1';

        boolean success = makeMove(fromRow, fromCol, toRow, toCol);

        if (success && onAiMove != null) {
            onAiMove.accept(board.getLastMove(), this);
        }
    }

    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        List<Move> legalMoves = getLegalMoves(fromRow, fromCol);

        for (Move move : legalMoves) {
            if (move.getToRow() == toRow && move.getToCol() == toCol) {
                applyMoveToBoard(board, move);
                board.setLastMove(move);

                // Timer logic
                if (!timerStarted) {
                    timerStarted = true;
                    lastTickTime = 0;
                }

                long increment = ChessConfig.HANDLER.instance().incrementSeconds * 20L;
                if (currentPlayer == Piece.PieceColor.WHITE) {
                    whiteTime += increment;
                } else {
                    blackTime += increment;
                }

                // Switch player
                currentPlayer = currentPlayer.opposite();

                // Check game state
                updateGameState();

                if (state == GameState.ACTIVE && currentPlayer == aiColor && aiEngine != null) {
                    triggerAiMove();
                }

                return true;
            }
        }

        return false;
    }

    private void applyMoveToBoard(Board board, Move move) {
        Piece piece = board.getPiece(move.getFromRow(), move.getFromCol());

        if (move.getMoveType() == Move.MoveType.CASTLE_KINGSIDE) {
            // Move king
            board.setPiece(move.getToRow(), move.getToCol(), piece);
            board.setPiece(move.getFromRow(), move.getFromCol(), null);
            piece.setMoved(true);

            // Move rook
            Piece rook = board.getPiece(move.getFromRow(), 7);
            board.setPiece(move.getFromRow(), 5, rook);
            board.setPiece(move.getFromRow(), 7, null);
            rook.setMoved(true);
        } else if (move.getMoveType() == Move.MoveType.CASTLE_QUEENSIDE) {
            // Move king
            board.setPiece(move.getToRow(), move.getToCol(), piece);
            board.setPiece(move.getFromRow(), move.getFromCol(), null);
            piece.setMoved(true);

            // Move rook
            Piece rook = board.getPiece(move.getFromRow(), 0);
            board.setPiece(move.getFromRow(), 3, rook);
            board.setPiece(move.getFromRow(), 0, null);
            rook.setMoved(true);
        } else if (move.getMoveType() == Move.MoveType.EN_PASSANT) {
            // Move pawn
            board.setPiece(move.getToRow(), move.getToCol(), piece);
            board.setPiece(move.getFromRow(), move.getFromCol(), null);
            piece.setMoved(true);

            // Remove captured pawn
            board.setPiece(move.getFromRow(), move.getToCol(), null);
        } else if (move.getMoveType() == Move.MoveType.PROMOTION) {
            // Promote pawn
            Piece promotedPiece = new Piece(move.getPromotionType(), piece.getColor());
            promotedPiece.setMoved(true);
            board.setPiece(move.getToRow(), move.getToCol(), promotedPiece);
            board.setPiece(move.getFromRow(), move.getFromCol(), null);
        } else {
            // Normal move
            board.setPiece(move.getToRow(), move.getToCol(), piece);
            board.setPiece(move.getFromRow(), move.getFromCol(), null);
            piece.setMoved(true);
        }
    }

    private void updateGameState() {
        boolean hasLegalMoves = false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == currentPlayer) {
                    if (!getLegalMoves(row, col).isEmpty()) {
                        hasLegalMoves = true;
                        break;
                    }
                }
            }
            if (hasLegalMoves)
                break;
        }

        if (!hasLegalMoves) {
            int[] kingPos = board.findKing(currentPlayer);
            if (kingPos != null && board.isSquareAttacked(kingPos[0], kingPos[1], currentPlayer.opposite())) {
                state = currentPlayer == Piece.PieceColor.WHITE ? GameState.BLACK_WINS : GameState.WHITE_WINS;
            } else {
                state = GameState.STALEMATE;
            }
        }
    }

    public boolean isInCheck() {
        int[] kingPos = board.findKing(currentPlayer);
        if (kingPos == null)
            return false;
        return board.isSquareAttacked(kingPos[0], kingPos[1], currentPlayer.opposite());
    }

    public void tick(long worldTime) {
        if (state != GameState.ACTIVE || !timerStarted) {
            lastTickTime = worldTime;
            return;
        }

        if (lastTickTime == 0) {
            lastTickTime = worldTime;
            return;
        }

        long delta = worldTime - lastTickTime;
        lastTickTime = worldTime;

        // Prevent large jumps (e.g. lag spikes or time changes)
        if (delta > 20)
            delta = 1;
        if (delta < 0)
            delta = 0;

        if (currentPlayer == Piece.PieceColor.WHITE) {
            whiteTime -= delta;
            if (whiteTime <= 0) {
                whiteTime = 0;
                state = GameState.BLACK_WINS;
            }
        } else {
            blackTime -= delta;
            if (blackTime <= 0) {
                blackTime = 0;
                state = GameState.WHITE_WINS;
            }
        }
    }

    public long getWhiteTime() {
        return whiteTime;
    }

    public long getBlackTime() {
        return blackTime;
    }

    public enum GameState {
        ACTIVE, WHITE_WINS, BLACK_WINS, STALEMATE
    }
}
