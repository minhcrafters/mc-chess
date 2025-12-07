package minhcrafters.chess;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import minhcrafters.chess.config.ChessConfig;
import net.minecraft.text.Text;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Chess Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Legal Moves"))
                                .description(OptionDescription
                                        .of(Text.literal("Whether to highlight legal moves when a piece is selected.")))
                                .binding(
                                        true,
                                        () -> ChessConfig.HANDLER.instance().showLegalMoves,
                                        newVal -> ChessConfig.HANDLER.instance().showLegalMoves = newVal)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Overlay"))
                                .description(OptionDescription
                                        .of(Text.literal("Whether to show what you're doing on the action bar.")))
                                .binding(
                                        true,
                                        () -> ChessConfig.HANDLER.instance().showOverlay,
                                        newVal -> ChessConfig.HANDLER.instance().showOverlay = newVal)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Engine"))
                        .option(Option.<String>createBuilder()
                                .name(Text.literal("UCI Engine Path"))
                                .description(OptionDescription
                                        .of(Text.literal("Path to the external UCI chess engine executable.")))
                                .binding(
                                        "",
                                        () -> ChessConfig.HANDLER.instance().uciEnginePath,
                                        newVal -> ChessConfig.HANDLER.instance().uciEnginePath = newVal)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.literal("Default Time (Seconds)"))
                                .description(
                                        OptionDescription.of(Text.literal("Default time in seconds for each player.")))
                                .binding(
                                        600,
                                        () -> ChessConfig.HANDLER.instance().defaultTimeSeconds,
                                        newVal -> ChessConfig.HANDLER.instance().defaultTimeSeconds = newVal)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).min(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.literal("Increment (Seconds)"))
                                .description(OptionDescription.of(Text.literal("Time increment in seconds per move.")))
                                .binding(
                                        0,
                                        () -> ChessConfig.HANDLER.instance().incrementSeconds,
                                        newVal -> ChessConfig.HANDLER.instance().incrementSeconds = newVal)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
                                .build())
                        .build())
                .save(ChessConfig.HANDLER::save)
                .build()
                .generateScreen(parent);
    }
}
