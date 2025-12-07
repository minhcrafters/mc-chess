package minhcrafters.chess.config;

import com.google.gson.GsonBuilder;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.util.Identifier;

public class ChessConfig {
    public static final ConfigClassHandler<ChessConfig> HANDLER = ConfigClassHandler.createBuilder(ChessConfig.class)
            .id(Identifier.of("chess", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("chess.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Whether to highlight legal moves when a piece is selected.")
    public boolean showLegalMoves = true;

    @SerialEntry(comment = "Whether to show what you're doing on the action bar.")
    public boolean showOverlay = true;

    @SerialEntry(comment = "Path to the external UCI chess engine executable.")
    public String uciEnginePath = "";

    @SerialEntry(comment = "Default time in seconds for each player.")
    public int defaultTimeSeconds = 600;

    @SerialEntry(comment = "Time increment in seconds per move.")
    public int incrementSeconds = 0;
}
