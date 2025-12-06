package minhcrafters.chess.game;

public class Piece {
    private final PieceType type;
    private final PieceColor color;
    private boolean hasMoved;

    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }

    public PieceType getType() {
        return type;
    }

    public PieceColor getColor() {
        return color;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }

    public Piece copy() {
        Piece copy = new Piece(type, color);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    @Override
    public String toString() {
        return color.toString().charAt(0) + type.toString();
    }

    public enum PieceType {
        PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
    }

    public enum PieceColor {
        WHITE, BLACK;

        public PieceColor opposite() {
            return this == WHITE ? BLACK : WHITE;
        }
    }
}
