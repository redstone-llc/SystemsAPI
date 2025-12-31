package llc.redstone.test.tests

import com.github.shynixn.mccoroutine.fabric.launch
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import llc.redstone.systemsapi.SystemsAPI
import llc.redstone.test.Actions
import llc.redstone.test.TestMod
import llc.redstone.test.TestMod.MC
import llc.redstone.test.TestMod.sendFeedback
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.of
import java.awt.Color

object Function {
    fun LiteralArgumentBuilder<FabricClientCommandSource>.withFunctionSubCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(
            literal("function")
                .then(
                    argument("indexes", IntegerArgumentType.integer())
                        .then(
                            argument("arguments", StringArgumentType.greedyString())
                                .executes { context ->
                                    val amount = IntegerArgumentType.getInteger(context, "indexes")
                                    val random = StringArgumentType.getString(context, "arguments")
                                    execute(context, amount, random)
                                }
                        )
                        .executes { context ->
                            val amount = IntegerArgumentType.getInteger(context, "indexes")
                            execute(context, amount)
                        }

                )
                .executes { context ->
                    execute(context)
                }


        )
    }

    fun execute(context: CommandContext<FabricClientCommandSource>, amount: Int? = null, args: String = ""): Int {
        val amount = amount ?: (1..10).random()
        val random = args.contains("-r")
        val onlyOne = args.contains("-o")
        TestMod.launch {
            try {
                MC.player?.sendMessage(
                    MutableText.of(
                        of("[Test Mod] Updating function 'test' actions...")
                    ).withColor(Color.YELLOW.rgb), false
                )
                val actions = List(if (onlyOne) 1 else amount) { i -> Actions.createNormalAction(
                    index = if (random) -1 else (if (onlyOne) amount else i)
                )}
                for (action in actions) {
                    context.sendFeedback("Prepared Action", action)
                }
                SystemsAPI.getHousingImporter().getFunction("test")?.let { function ->
                    function.getActionContainer().setActions(actions)
//                    for (action in function.getActionContainer().getActions()) {
//                        context.sendFeedback("Found Action", action)
//                    }
                }
                MC.player?.sendMessage(
                    MutableText.of(
                        of("[Test Mod] Function 'test' actions updated successfully.")
                    ).withColor(Color.GREEN.rgb), false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                MC.player?.sendMessage(
                    MutableText.of(
                        of("[Test Mod] An error occurred: ${e.message}")
                    ).withColor(Color.RED.rgb), false
                )
            }
        }


        return 1
    }

}