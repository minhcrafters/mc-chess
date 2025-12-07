package minhcrafters.chess.game.util;

import minhcrafters.chess.game.Board;
import minhcrafters.chess.game.Piece;
import minhcrafters.chess.game.Piece.PieceColor;
import minhcrafters.chess.game.Piece.PieceType;

public class FenUtils {
    public static String getFen(Board board, PieceColor activeColor) {
        StringBuilder fen = new StringBuilder();

        // Piece placement
        for (int row = 7; row >= 0; row--) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(getPieceChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row > 0) {
                fen.append('/');
            }
        }

        // Active color
        fen.append(' ').append(activeColor == PieceColor.WHITE ? 'w' : 'b');

        // Castling availability
        fen.append(' ');
        StringBuilder castling = new StringBuilder();

        // White
        Piece whiteKing = board.getPiece(0, 4); // Assuming standard setup
        if (whiteKing != null && whiteKing.getType() == PieceType.KING && !whiteKing.hasMoved()) {
            Piece whiteRookKing = board.getPiece(0, 7);
            if (whiteRookKing != null && whiteRookKing.getType() == PieceType.ROOK && !whiteRookKing.hasMoved()) {
                castling.append('K');
            }
            Piece whiteRookQueen = board.getPiece(0, 0);
            if (whiteRookQueen != null && whiteRookQueen.getType() == PieceType.ROOK && !whiteRookQueen.hasMoved()) {
                castling.append('Q');
            }
        }

        // Black
        Piece blackKing = board.getPiece(7, 4);
        if (blackKing != null && blackKing.getType() == PieceType.KING && !blackKing.hasMoved()) {
            Piece blackRookKing = board.getPiece(7, 7);
            if (blackRookKing != null && blackRookKing.getType() == PieceType.ROOK && !blackRookKing.hasMoved()) {
                castling.append('k');
            }
            Piece blackRookQueen = board.getPiece(7, 0);
            if (blackRookQueen != null && blackRookQueen.getType() == PieceType.ROOK && !blackRookQueen.hasMoved()) {
                castling.append('q');
            }
        }

        if (castling.length() == 0) {
            fen.append('-');
        } else {
            fen.append(castling);
        }

        // En passant target square
        fen.append(' ');
        if (board.getLastMove() != null) {
            int fromRow = board.getLastMove().getFromRow();
            int toRow = board.getLastMove().getToRow();
            int toCol = board.getLastMove().getToCol();
            Piece movedPiece = board.getPiece(toRow, toCol);

            if (movedPiece != null && movedPiece.getType() == PieceType.PAWN && Math.abs(toRow - fromRow) == 2) {
                int enPassantRow = (fromRow + toRow) / 2;
                char file = (char) ('a' + toCol);
                fen.append(file).append(enPassantRow + 1);
            } else {
                fen.append('-');
            }
        } else {
            fen.append('-');
        }

        // Halfmove clock
        fen.append(" 0"); // TODO: Placeholder

        // Fullmove number
        fen.append(" 1"); // TODO: Placeholder

        return fen.toString();
    }

    private static char getPieceChar(Piece piece) {
        char c;
        switch (piece.getType()) {
            case PAWN:
                c = 'p';
                break;
            case KNIGHT:
                c = 'n';
                break;
            case BISHOP:
                c = 'b';
                break;
            case ROOK:
                c = 'r';
                break;
            case QUEEN:
                c = 'q';
                break;
            case KING:
                c = 'k';
                break;
            default:
                throw new IllegalArgumentException("Unknown piece type");
        }
        return piece.getColor() == PieceColor.WHITE ? Character.toUpperCase(c) : c;
    }
}
