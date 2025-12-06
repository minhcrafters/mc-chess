package minhcrafters.chess.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChessGame {
    private final UUID gameId;
    private final Board board;
    private Piece.PieceColor currentPlayer;
    private GameState state;
    private UUID whitePlayer;
    private UUID blackPlayer;

    public ChessGame(UUID gameId) {
        this.gameId = gameId;
        this.board = new Board();
        this.currentPlayer = Piece.PieceColor.WHITE;
        this.state = GameState.ACTIVE;
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

    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        List<Move> legalMoves = getLegalMoves(fromRow, fromCol);

        for (Move move : legalMoves) {
            if (move.getToRow() == toRow && move.getToCol() == toCol) {
                applyMoveToBoard(board, move);
                board.setLastMove(move);

                // Switch player
                currentPlayer = currentPlayer.opposite();

                // Check game state
                updateGameState();

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

    public enum GameState {
        ACTIVE, WHITE_WINS, BLACK_WINS, STALEMATE
    }
}
