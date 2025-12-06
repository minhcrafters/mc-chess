package minhcrafters.chess.game;

public class Move {
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final MoveType moveType;
    private final Piece capturedPiece;
    private final Piece.PieceType promotionType;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, MoveType.NORMAL, null, null);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, MoveType moveType, Piece capturedPiece,
            Piece.PieceType promotionType) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.moveType = moveType;
        this.capturedPiece = capturedPiece;
        this.promotionType = promotionType;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public Piece.PieceType getPromotionType() {
        return promotionType;
    }

    public enum MoveType {
        NORMAL, CASTLE_KINGSIDE, CASTLE_QUEENSIDE, EN_PASSANT, PROMOTION
    }

    @Override
    public String toString() {
        char fromFile = (char) ('a' + fromCol);
        char toFile = (char) ('a' + toCol);
        return "" + fromFile + (fromRow + 1) + toFile + (toRow + 1);
    }
}
