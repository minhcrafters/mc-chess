package minhcrafters.chess.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import minhcrafters.chess.game.ChessGame;
import minhcrafters.chess.game.ChessManager;
import minhcrafters.chess.game.Piece;
import minhcrafters.chess.render.ChessBoardRenderer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class ChessCommand {
    private final ChessBoardRenderer renderer;

    public ChessCommand(ChessBoardRenderer renderer) {
        this.renderer = renderer;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("chess")
                .then(CommandManager.literal("start")
                        .executes(this::startGame))
                .then(CommandManager.literal("join")
                        .then(CommandManager.argument("color", StringArgumentType.word())
                                .executes(this::joinGame)))
                .then(CommandManager.literal("end")
                        .executes(this::endGame))
                .then(CommandManager.literal("list")
                        .executes(this::listGames)));
    }

    private int startGame(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Only players can start chess games!"));
            return 0;
        }

        ServerWorld world = player.getEntityWorld();
        BlockPos playerPos = player.getBlockPos();

        // Create board in front of player
        BlockPos boardCenter = playerPos.offset(player.getHorizontalFacing(), 3);

        ChessManager manager = ChessManager.getInstance();

        // Check if there's already a game at this position
        if (manager.getGameAtPosition(boardCenter) != null) {
            source.sendError(Text.literal("§cThere's already a chess game here!"));
            return 0;
        }

        // Create new game
        String worldId = world.getRegistryKey().getValue().toString();
        ChessGame game = manager.createGame(boardCenter, worldId);

        // Assign player as white
        manager.assignPlayer(game.getGameId(), player.getUuid(), Piece.PieceColor.WHITE);

        // Render the board
        renderer.renderBoard(world, game, boardCenter);

        source.sendFeedback(() -> Text.literal("§aChess game started! You are playing as White."), false);
        source.sendFeedback(() -> Text.literal("§7Another player can join with: /chess join black"), false);

        return 1;
    }

    private int joinGame(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Only players can join chess games!"));
            return 0;
        }

        String colorStr = StringArgumentType.getString(context, "color").toLowerCase();
        Piece.PieceColor color;

        if (colorStr.equals("white")) {
            color = Piece.PieceColor.WHITE;
        } else if (colorStr.equals("black")) {
            color = Piece.PieceColor.BLACK;
        } else {
            source.sendError(Text.literal("§cColor must be 'white' or 'black'!"));
            return 0;
        }

        ChessManager manager = ChessManager.getInstance();

        // Find nearest game
        ChessGame nearestGame = findNearestGame(player);

        if (nearestGame == null) {
            source.sendError(Text.literal("§cNo chess game found nearby!"));
            return 0;
        }

        // Check if slot is available
        if (color == Piece.PieceColor.WHITE && nearestGame.getWhitePlayer() != null) {
            source.sendError(Text.literal("§cWhite slot is already taken!"));
            return 0;
        }

        if (color == Piece.PieceColor.BLACK && nearestGame.getBlackPlayer() != null) {
            source.sendError(Text.literal("§cBlack slot is already taken!"));
            return 0;
        }

        // Assign player
        manager.assignPlayer(nearestGame.getGameId(), player.getUuid(), color);

        String colorName = color == Piece.PieceColor.WHITE ? "White" : "Black";
        source.sendFeedback(() -> Text.literal("§aJoined the game as " + colorName + "!"), false);

        // Notify other player
        if (color == Piece.PieceColor.WHITE && nearestGame.getBlackPlayer() != null) {
            ServerPlayerEntity otherPlayer = source.getServer().getPlayerManager()
                    .getPlayer(nearestGame.getBlackPlayer());
            if (otherPlayer != null) {
                otherPlayer.sendMessage(Text.literal("§a" + player.getName().getString() + " joined as White!"), false);
            }
        } else if (color == Piece.PieceColor.BLACK && nearestGame.getWhitePlayer() != null) {
            ServerPlayerEntity otherPlayer = source.getServer().getPlayerManager()
                    .getPlayer(nearestGame.getWhitePlayer());
            if (otherPlayer != null) {
                otherPlayer.sendMessage(Text.literal("§a" + player.getName().getString() + " joined as Black!"), false);
            }
        }

        return 1;
    }

    private int endGame(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Only players can end chess games!"));
            return 0;
        }

        ChessManager manager = ChessManager.getInstance();
        ChessGame game = manager.getGameByPlayer(player.getUuid());

        if (game == null) {
            game = findNearestGame(player);
        }

        if (game == null) {
            source.sendError(Text.literal("§cNo chess game found!"));
            return 0;
        }

        // Clear the board
        renderer.clearBoard(player.getEntityWorld(), game.getGameId());
        manager.removeGame(game.getGameId());

        source.sendFeedback(() -> Text.literal("§aChess game ended."), false);

        return 1;
    }

    private int listGames(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ChessManager manager = ChessManager.getInstance();

        var games = manager.getAllGames();

        if (games.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§7No active chess games."), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("§6Active Chess Games:"), false);

        for (ChessGame game : games) {
            ChessManager.GameLocation location = manager.getGameLocation(game.getGameId());
            String whitePlayer = game.getWhitePlayer() != null
                    ? source.getServer().getPlayerManager().getPlayer(game.getWhitePlayer()).getName().getString()
                    : "Empty";
            String blackPlayer = game.getBlackPlayer() != null
                    ? source.getServer().getPlayerManager().getPlayer(game.getBlackPlayer()).getName().getString()
                    : "Empty";
            String state = game.getState().toString();
            String turn = game.getCurrentPlayer().toString();

            BlockPos pos = location.getBoardCenter();
            source.sendFeedback(() -> Text.literal(
                    "§7- Position: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() +
                            " | White: §f" + whitePlayer + " §7| Black: §f" + blackPlayer +
                            " §7| State: §f" + state + " §7| Turn: §f" + turn),
                    false);
        }

        return 1;
    }

    private ChessGame findNearestGame(ServerPlayerEntity player) {
        ChessManager manager = ChessManager.getInstance();
        BlockPos playerPos = player.getBlockPos();

        ChessGame nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ChessGame game : manager.getAllGames()) {
            ChessManager.GameLocation location = manager.getGameLocation(game.getGameId());
            if (location != null) {
                double distance = Math.sqrt(playerPos.getSquaredDistance(location.getBoardCenter()));
                if (distance < nearestDistance && distance <= 20) {
                    nearest = game;
                    nearestDistance = distance;
                }
            }
        }

        return nearest;
    }
}
