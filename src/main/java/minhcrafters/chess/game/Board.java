package minhcrafters.chess.game;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] pieces;
    private Move lastMove;

    public Board() {
        this.pieces = new Piece[8][8];
        setupInitialPosition();
    }

    private Board(Piece[][] pieces, Move lastMove) {
        this.pieces = new Piece[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (pieces[row][col] != null) {
                    this.pieces[row][col] = pieces[row][col].copy();
                }
            }
        }
        this.lastMove = lastMove;
    }

    private void setupInitialPosition() {
        // Black pieces
        pieces[7][0] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.BLACK);
        pieces[7][1] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.BLACK);
        pieces[7][2] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.BLACK);
        pieces[7][3] = new Piece(Piece.PieceType.KING, Piece.PieceColor.BLACK);
        pieces[7][4] = new Piece(Piece.PieceType.QUEEN, Piece.PieceColor.BLACK);
        pieces[7][5] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.BLACK);
        pieces[7][6] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.BLACK);
        pieces[7][7] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.BLACK);
        for (int col = 0; col < 8; col++) {
            pieces[6][col] = new Piece(Piece.PieceType.PAWN, Piece.PieceColor.BLACK);
        }

        // White pieces
        pieces[0][0] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.WHITE);
        pieces[0][1] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.WHITE);
        pieces[0][2] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE);
        pieces[0][3] = new Piece(Piece.PieceType.KING, Piece.PieceColor.WHITE);
        pieces[0][4] = new Piece(Piece.PieceType.QUEEN, Piece.PieceColor.WHITE);
        pieces[0][5] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE);
        pieces[0][6] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.WHITE);
        pieces[0][7] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.WHITE);
        for (int col = 0; col < 8; col++) {
            pieces[1][col] = new Piece(Piece.PieceType.PAWN, Piece.PieceColor.WHITE);
        }

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                System.out.print(pieces[y][x] + " ");
            }
            System.out.println();
        }
    }

    public Piece getPiece(int row, int col) {
        if (!isValidPosition(row, col))
            return null;
        return pieces[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (isValidPosition(row, col)) {
            pieces[row][col] = piece;
        }
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public void setLastMove(Move move) {
        this.lastMove = move;
    }

    public Board copy() {
        return new Board(pieces, lastMove);
    }

    public List<Move> getLegalMoves(int row, int col, Piece.PieceColor currentPlayer) {
        List<Move> moves = new ArrayList<>();
        Piece piece = getPiece(row, col);

        if (piece == null || piece.getColor() != currentPlayer) {
            return moves;
        }

        switch (piece.getType()) {
            case PAWN:
                addPawnMoves(moves, row, col, piece.getColor());
                break;
            case KNIGHT:
                addKnightMoves(moves, row, col, piece.getColor());
                break;
            case BISHOP:
                addBishopMoves(moves, row, col, piece.getColor());
                break;
            case ROOK:
                addRookMoves(moves, row, col, piece.getColor());
                break;
            case QUEEN:
                addQueenMoves(moves, row, col, piece.getColor());
                break;
            case KING:
                addKingMoves(moves, row, col, piece.getColor());
                break;
        }

        return moves;
    }

    private void addPawnMoves(List<Move> moves, int row, int col, Piece.PieceColor color) {
        int direction = color == Piece.PieceColor.WHITE ? 1 : -1;
        int startRow = color == Piece.PieceColor.WHITE ? 1 : 6;
        int promotionRow = color == Piece.PieceColor.WHITE ? 7 : 0;

        // Forward move
        int newRow = row + direction;
        if (isValidPosition(newRow, col) && getPiece(newRow, col) == null) {
            if (newRow == promotionRow) {
                moves.add(new Move(row, col, newRow, col, Move.MoveType.PROMOTION, null, Piece.PieceType.QUEEN));
            } else {
                moves.add(new Move(row, col, newRow, col));
            }

            // Double move from start
            if (row == startRow) {
                int doubleRow = row + 2 * direction;
                if (getPiece(doubleRow, col) == null) {
                    moves.add(new Move(row, col, doubleRow, col));
                }
            }
        }

        // Captures
        for (int dcol : new int[] { -1, 1 }) {
            int newCol = col + dcol;
            if (isValidPosition(newRow, newCol)) {
                Piece target = getPiece(newRow, newCol);
                if (target != null && target.getColor() != color) {
                    if (newRow == promotionRow) {
                        moves.add(new Move(row, col, newRow, newCol, Move.MoveType.PROMOTION, target,
                                Piece.PieceType.QUEEN));
                    } else {
                        moves.add(new Move(row, col, newRow, newCol, Move.MoveType.NORMAL, target, null));
                    }
                }
            }
        }

        // En passant
        if (lastMove != null && lastMove.getMoveType() == Move.MoveType.NORMAL) {
            Piece lastPiece = getPiece(lastMove.getToRow(), lastMove.getToCol());
            if (lastPiece != null && lastPiece.getType() == Piece.PieceType.PAWN &&
                    Math.abs(lastMove.getToRow() - lastMove.getFromRow()) == 2 &&
                    lastMove.getToRow() == row && Math.abs(lastMove.getToCol() - col) == 1) {
                moves.add(new Move(row, col, newRow, lastMove.getToCol(), Move.MoveType.EN_PASSANT, lastPiece, null));
            }
        }
    }

    private void addKnightMoves(List<Move> moves, int row, int col, Piece.PieceColor color) {
        int[][] offsets = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 }, { 2, 1 } };
        for (int[] offset : offsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];
            if (isValidPosition(newRow, newCol)) {
                Piece target = getPiece(newRow, newCol);
                if (target == null || target.getColor() != color) {
                    moves.add(new Move(row, col, newRow, newCol, Move.MoveType.NORMAL, target, null));
                }
            }
        }
    }

    private void addBishopMoves(List<Move> moves, int row, int col, Piece.PieceColor color) {
        addDirectionalMoves(moves, row, col, color, new int[][] { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } });
    }

    private void addRookMoves(List<Move> moves, int row, int col, Piece.PieceColor color) {
        addDirectionalMoves(moves, row, col, color, new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } });
    }

    private void addQueenMoves(List<Move> moves, int row, int col, Piece.PieceColor color) {
        addDirectionalMoves(moves, row, col, color,
                new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } });
    }

    private void addDirectionalMoves(List<Move> moves, int row, int col, Piece.PieceColor color, int[][] directions) {
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            while (isValidPosition(newRow, newCol)) {
                Piece target = getPiece(newRow, newCol);
                if (target == null) {
                    moves.add(new Move(row, col, newRow, newCol));
                } else {
                    if (target.getColor() != color) {
                        moves.add(new Move(row, col, newRow, newCol, Move.MoveType.NORMAL, target, null));
                    }
                    break;
                }
                newRow += dir[0];
                newCol += dir[1];
            }
        }
    }

    private void addKingMoves(List<Move> moves, int row, int col, Piece.PieceColor color) {
        int[][] offsets = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
        for (int[] offset : offsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];
            if (isValidPosition(newRow, newCol)) {
                Piece target = getPiece(newRow, newCol);
                if (target == null || target.getColor() != color) {
                    moves.add(new Move(row, col, newRow, newCol, Move.MoveType.NORMAL, target, null));
                }
            }
        }

        // Castling
        Piece king = getPiece(row, col);
        if (!king.hasMoved()) {
            // Kingside
            Piece kingsideRook = getPiece(row, 7);
            if (kingsideRook != null && !kingsideRook.hasMoved() &&
                    getPiece(row, 5) == null && getPiece(row, 6) == null) {
                moves.add(new Move(row, col, row, 6, Move.MoveType.CASTLE_KINGSIDE, null, null));
            }

            // Queenside
            Piece queensideRook = getPiece(row, 0);
            if (queensideRook != null && !queensideRook.hasMoved() &&
                    getPiece(row, 1) == null && getPiece(row, 2) == null && getPiece(row, 3) == null) {
                moves.add(new Move(row, col, row, 2, Move.MoveType.CASTLE_QUEENSIDE, null, null));
            }
        }
    }

    public int[] findKing(Piece.PieceColor color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = getPiece(row, col);
                if (piece != null && piece.getType() == Piece.PieceType.KING && piece.getColor() == color) {
                    return new int[] { row, col };
                }
            }
        }
        return null;
    }

    public boolean isSquareAttacked(int row, int col, Piece.PieceColor attacker) {
        // Check pawn attacks
        int pawnDir = attacker == Piece.PieceColor.WHITE ? 1 : -1;
        int pawnRow = row - pawnDir;
        for (int dcol : new int[] { -1, 1 }) {
            int pawnCol = col + dcol;
            if (isValidPosition(pawnRow, pawnCol)) {
                Piece piece = getPiece(pawnRow, pawnCol);
                if (piece != null && piece.getType() == Piece.PieceType.PAWN && piece.getColor() == attacker) {
                    return true;
                }
            }
        }

        // Check knight attacks
        int[][] knightOffsets = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 },
                { 2, 1 } };
        for (int[] offset : knightOffsets) {
            int r = row + offset[0];
            int c = col + offset[1];
            if (isValidPosition(r, c)) {
                Piece piece = getPiece(r, c);
                if (piece != null && piece.getType() == Piece.PieceType.KNIGHT && piece.getColor() == attacker) {
                    return true;
                }
            }
        }

        // Check sliding pieces (bishop, rook, queen)
        int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        for (int i = 0; i < directions.length; i++) {
            int[] dir = directions[i];
            boolean isDiagonal = i >= 4;
            int r = row + dir[0];
            int c = col + dir[1];
            while (isValidPosition(r, c)) {
                Piece piece = getPiece(r, c);
                if (piece != null) {
                    if (piece.getColor() == attacker) {
                        if (piece.getType() == Piece.PieceType.QUEEN ||
                                (isDiagonal && piece.getType() == Piece.PieceType.BISHOP) ||
                                (!isDiagonal && piece.getType() == Piece.PieceType.ROOK)) {
                            return true;
                        }
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }

        // Check king attacks
        int[][] kingOffsets = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
        for (int[] offset : kingOffsets) {
            int r = row + offset[0];
            int c = col + offset[1];
            if (isValidPosition(r, c)) {
                Piece piece = getPiece(r, c);
                if (piece != null && piece.getType() == Piece.PieceType.KING && piece.getColor() == attacker) {
                    return true;
                }
            }
        }

        return false;
    }
}
