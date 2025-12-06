package minhcrafters.chess;

import minhcrafters.chess.command.ChessCommand;
import minhcrafters.chess.interaction.ChessInteractionHandler;
import minhcrafters.chess.render.ChessBoardRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chess implements ModInitializer {
	public static final String MOD_ID = "chess";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static ChessBoardRenderer renderer;
	private static ChessInteractionHandler interactionHandler;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Chess Mod...");

		// Initialize renderer
		renderer = new ChessBoardRenderer();

		// Initialize interaction handler
		interactionHandler = new ChessInteractionHandler(renderer);
		interactionHandler.register();

		// Register commands
		ChessCommand chessCommand = new ChessCommand(renderer);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			chessCommand.register(dispatcher);
		});

		LOGGER.info("Chess Mod initialized!");
	}

	public static ChessBoardRenderer getRenderer() {
		return renderer;
	}

	public static ChessInteractionHandler getInteractionHandler() {
		return interactionHandler;
	}
}