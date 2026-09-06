package llc.redstone.test

//? if <26.1 {
//?} else {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
*///?}
import com.mojang.brigadier.context.CommandContext
import llc.redstone.htslreborn.htslio.HTSLExporter
import llc.redstone.htslreborn.parser.Parser
import llc.redstone.htslreborn.parser.PreProcess
import llc.redstone.htslreborn.tokenizer.Tokenizer
import llc.redstone.systemsapi.SystemsAPI
import llc.redstone.systemsapi.api.ImportProgress
import llc.redstone.systemsdata.Action
import llc.redstone.systemsdata.Condition
import llc.redstone.systemsdata.StatValue
import llc.redstone.test.tests.GroupsTest.withGroupsSubCommand
import llc.redstone.test.tests.HouseSettingsTest.withHouseSettingsSubCommand
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import net.minecraft.network.chat.contents.PlainTextContents.create as of


object TestMod : ClientModInitializer {
    const val MOD_ID = "testmod"
    val LOGGER: Logger = LoggerFactory.getLogger("TestMod")
    const val VERSION = /*$ mod_version*/ "0.0.1";
    const val MINECRAFT = /*$ minecraft*/ "1.21.11";
    val MC: Minecraft
        get() = Minecraft.getInstance()

    fun CommandContext<FabricClientCommandSource>.sendFeedback(label: String, value: Any) {
        val darkBlue = TextColor.fromRgb(0x1C5796)   // darker blue
        val lightBlue = TextColor.fromRgb(0x48719E)  // lighter blue
        val labelText: MutableComponent = MutableComponent.create(of("$label: ")).setStyle(Style.EMPTY.withColor(darkBlue))
        val valueText: MutableComponent = MutableComponent.create(of(value.toString())).setStyle(Style.EMPTY.withColor(lightBlue))
        this.source.sendFeedback(labelText.append(valueText))
    }

    override fun onInitializeClient() {
        LOGGER.info("Loaded v$VERSION for Minecraft $MINECRAFT.")

        ProgressHud.register()

        // Logs every meaningful change, so a run can be checked for monotonic fraction afterwards.
        ImportProgress.addListener { progress ->
            LOGGER.info(
                "[progress] {} {} {}% remaining={} steps={}/{} depth={} label={}",
                progress.phase,
                progress.outcome,
                (progress.fraction * 100f).toInt(),
                progress.remainingSeconds,
                progress.completedSteps,
                progress.totalSteps,
                progress.depth,
                progress.currentLabel,
            )
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            dispatcher.register(
                literal("htsl")
                    .executes {
                        it.source.sendFeedback(MutableComponent.create(of("Usage: /htsl <feature>")))
                        1
                    }
                    .then(literal("import").executes {
                        val homePath = Paths.get(System.getProperty("user.home"))
                        val path = homePath.resolve("Desktop/test.htsl")
                        if (!path.toFile().exists()) {
                            it.source.sendFeedback(MutableComponent.create(of("File does not exist: $path")))
                            return@executes 1
                        }
                        val tokens = Tokenizer.tokenize(path)
                        val preProcessedTokens = PreProcess.preProcess(tokens)
                        val gotoSplit = Parser.parse(preProcessedTokens, path)
                        it.source.sendFeedback(MutableComponent.create(of("Parsed ${gotoSplit.first().second.size} actions from $path")))

                        SystemsAPI.launch {
                            SystemsAPI.getHousingImporter().getFunction("test")
                                ?.getActionContainer()
                                ?.setActions(gotoSplit.first().second)
                        }
                        1
                    })
                    .then(literal("export").executes {
                        SystemsAPI.launch {
                            val actions = SystemsAPI.getHousingImporter().getFunction("test")
                                ?.getActionContainer()
                                ?.getActions()
                            it.sendFeedback("Exported", actions?.size ?: 0)
                            val lines = HTSLExporter.export(actions ?: emptyList())
                            val homePath = Paths.get(System.getProperty("user.home"))
                            val path = homePath.resolve("Desktop/test_export.htsl")
                            path.toFile().writeText(lines.joinToString("\n"))
                        }
                        1
                    })
            )

            dispatcher.register(
                literal("testmod")
                    .executes {
                        it.source.sendFeedback(MutableComponent.create(of("Usage: /testmod <feature>")))
                        1
                    }
                    .withHouseSettingsSubCommand()
                    .withGroupsSubCommand()
            )

            dispatcher.register(
                literal("demo")
                    .executes { context ->
                        SystemsAPI.launch {
                            val importer = SystemsAPI.getHousingImporter()

                            println(importer.getFunction("test")
                                ?.getActionContainer()
                                ?.setActions(
                                    listOf(
                                        Action.PlayerVariable(
                                            variable = "Test",
                                            amount = StatValue.Lng(10)
                                        ),
                                        Action.PlayerVariable(),
                                        Action.SendMessage("Hello from SystemsAPI")
                                    )
                                ))
                        }
                        1
                    }
                    // The deepest shape Housing allows: Conditional or Random Action cannot be
                    // placed inside a nested action list, so this is as deep as an import ever goes.
                    .then(literal("nested").executes {
                        SystemsAPI.launch {
                            SystemsAPI.getHousingImporter().getFunction("test")
                                ?.getActionContainer()
                                ?.setActions(
                                    listOf(
                                        Action.Conditional(
                                            conditions = listOf(Condition.RequiredGroup("Default")),
                                            ifActions = listOf(
                                                Action.SendMessage("matched"),
                                                Action.PlayerVariable(
                                                    variable = "Nested",
                                                    amount = StatValue.Lng(1)
                                                ),
                                            ),
                                            elseActions = listOf(
                                                Action.SendMessage("did not match"),
                                            ),
                                        ),
                                        Action.SendMessage("after"),
                                    )
                                )
                        }
                        1
                    })
                    // Reads the same function back, exercising the export side of progress.
                    .then(literal("export").executes { context ->
                        SystemsAPI.launch {
                            val actions = SystemsAPI.getHousingImporter().getFunction("test")
                                ?.getActionContainer()
                                ?.getActions()
                            context.sendFeedback("Exported", actions?.size ?: 0)
                            LOGGER.info("Exported actions: {}", actions)
                        }
                        1
                    })
                    // Cancels from the command thread, i.e. not the thread the tracker runs on.
                    .then(literal("cancel").executes {
                        SystemsAPI.getHousingImporter().cancelImport()
                        1
                    })
            )
        }
    }
}