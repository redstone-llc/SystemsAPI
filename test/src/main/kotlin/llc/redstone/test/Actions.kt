package llc.redstone.test

import llc.redstone.systemsapi.data.Action.*
import llc.redstone.systemsapi.data.Condition.*
import llc.redstone.systemsapi.data.*
import llc.redstone.systemsapi.data.enums.*
import llc.redstone.systemsapi.util.ItemUtils
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.Items
import net.minecraft.text.Text
import kotlin.collections.listOf
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

object Actions {
    fun createNormalAction(index: Int = -1, nested: Boolean = false): Action {
        val nbt = randomStack().nbt
        val testLocation = randomLocation()

        val actions = listOf(
            ChangePlayerGroup(
                newGroup = "test",
                includeHigherGroups = true
            ),
            PlayerVariable(
                variable = "hello",
                op = StatOp.Dec,
                amount = StatValue.I32(10),
                unset = false
            ),
            ApplyPotionEffect(
                effect = PotionEffect.Strength,
                duration = 10,
                level = 10,
                override = true,
                showIcon = false
            ),
            ChangeHealth(
                op = StatOp.Inc,
                amount = 5.0
            ),
            ApplyInventoryLayout(
                layout = "test"
            ),
            DropItem(
                item = ItemStack(
                    nbt = nbt,
                    relativeFileLocation = "test"
                ),
                location = testLocation,
                dropNaturally = true,
                disableMerging = true,
                prioritizePlayer = true,
                inventoryFallback = true,
                despawnDurationTicks = 20,
                pickupDelayTicks = 20
            ),
            ChangeVelocity(
                0.1,
                0.2,
                0.3
            ),
            LaunchToTarget(
                location = testLocation,
                strength = 1.5,
            ),
            SetPlayerWeather(
                Weather.RAINING
            ),
            SetPlayerTime(
                Time.Custom(12000)
            ),
            ToggleNametagDisplay(
                displayNametag = false
            ),
            GlobalVariable(
                variable = "hello",
                op = StatOp.Set,
                amount = StatValue.I32(2),
                unset = false
            ),
            TeamVariable(
                teamName = "test",
                variable = "test",
                op = StatOp.Set,
                amount = StatValue.I32(5),
                unset = false
            ),
            ClearAllPotionEffects(),
            DisplayActionBar(message = "message hehe he ha"),
            DisplayTitle(
                title = "Hello World!",
                subtitle = "This is a subtitle!",
                fadeIn = 5,
                stay = 2,
                fadeOut = 3
            ),
            FailParkour(reason = "This is a reason!"),
            FullHeal(),
            GiveExperienceLevels(levels = 10),
            GiveItem(
                item = ItemStack(
                    nbt = nbt,
                    relativeFileLocation = "test"
                ),
                allowMultiple = false,
                inventorySlot = InventorySlot.HelmetSlot(),
                replaceExistingItem = false
            ),
            KillPlayer(),
            ParkourCheckpoint(),
            PlaySound(
                sound = Sound.AnvilLand,
                volume = 0.8,
                pitch = 1.1,
                location = testLocation
            ),
            RemoveItem(
                item = ItemStack(
                    nbt = nbt,
                    relativeFileLocation = "test"
                )
            ),
            ResetInventory(),
            SendMessage(message = "Hello there !This is a message"),
            SendToLobby(location = Lobby.MainLobby),
            SetCompassTarget(location = testLocation),
            SetGameMode(gamemode = GameMode.Creative),
            ChangeHunger(op = StatOp.Set, amount = 4.0),
            ChangeMaxHealth(op = StatOp.Set, amount = 40.0, healOnChange = true),
            TeleportPlayer(location = testLocation, preventInsideBlocks = false),
            ExecuteFunction(name = "test2", global = true),
            EnchantHeldItem(enchantment = Enchantment.Protection, level = 10),
            DisplayMenu(menu = "test"),
            PauseExecution(ticks = 5),
            SetPlayerTeam(team = "test"),
        ).plus(
            if (nested) {
                emptyList()
            } else {
                listOf(
                    Conditional(
                        conditions = listOf(createNormalCondition()),
                        matchAnyCondition = false,
                        ifActions = listOf(createNormalAction(-1, true)),
                        elseActions = listOf(createNormalAction(-1, true))
                    ),
                    RandomAction(
                        actions = listOf(createNormalAction(-1, true))
                    )
                )
            }
        )

        return if (index == -1) actions.random()
        else actions[index]
    }

    fun createNormalCondition(): Condition {
        val nbt = randomStack().nbt
        val conditions = listOf(
            PlayerVariableRequirement(
                variable = "test",
                op = Comparison.Eq,
                value = StatValue.I32(5),
            ),
            GlobalVariableRequirement(
                variable = "test",
                op = Comparison.Le,
                value = StatValue.I32(10),
            ),
            TeamVariableRequirement(
                team = "test",
                variable = "test",
                op = Comparison.Ge,
                value = StatValue.I32(15),
            ),
            RequiredGroup(
                group = "test",
                includeHigherGroups = true,
            ),
            HasPermission(
                permission = Permission.EditCommands
            ),
            InRegion(
                region = "test"
            ),
            HasItem(
                item = ItemStack(
                    nbt = nbt,
                    relativeFileLocation = "test"
                ),
                whatToCheck = ItemCheck.ItemType,
                whereToCheck = InventoryLocation.Anywhere,
                amount = ItemAmount.Ge,
            ),
            InParkour(),
            RequiredEffect(
                effect = PotionEffect.Poison
            ),
            PlayerSneaking(),
            PlayerFlying(),
            RequiredHealth(
                mode = Comparison.Ge,
                amount = 10.0
            ),
            RequiredMaxHealth(
                mode = Comparison.Eq,
                amount = 20.0
            ),
            RequiredHungerLevel(
                mode = Comparison.Le,
                amount = 15.0
            ),
            RequiredGameMode(
                gameMode = GameMode.Survival
            ),
            RequiredPlaceholderNumber(
                placeholder = "%test%",
                mode = Comparison.Eq,
                amount = StatValue.I32(10),
            ),
            RequiredTeam(
                team = "test"
            ),
        )
        return conditions.random()
    }

    fun createRandomAction(nested: Boolean = false): Action {
        val actions = listOf(
            ChangePlayerGroup(
                newGroup = "test",
                includeHigherGroups = randomProp(Boolean::class)
            ),
            PlayerVariable(
                variable = randomWord(),
                op = randomProp(StatOp::class),
                amount = randomStatValue(),
                unset = randomProp(Boolean::class)
            ),
            ApplyPotionEffect(
                effect = PotionEffect.Strength,
                duration = randomProp(Int::class),
                level = randomProp(Int::class),
                override = randomProp(Boolean::class),
                showIcon = randomProp(Boolean::class)
            ),
            ChangeHealth(
                op = randomProp(StatOp::class),
                amount = randomProp(Double::class)
            ),
            ApplyInventoryLayout(
                layout = "test"
            ),
            DropItem(
                item = randomStack(),
                location = randomLocation(),
                dropNaturally = randomProp(Boolean::class),
                disableMerging = randomProp(Boolean::class),
                prioritizePlayer = randomProp(Boolean::class),
                inventoryFallback = randomProp(Boolean::class),
                despawnDurationTicks = randomProp(Int::class),
                pickupDelayTicks = randomProp(Int::class)
            ),
            ChangeVelocity(
                randomProp(Double::class),
                randomProp(Double::class),
                randomProp(Double::class)
            ),
            LaunchToTarget(
                location = randomLocation(),
                strength = randomProp(Double::class),
            ),
            SetPlayerWeather(
                randomProp(Weather::class)
            ),
            SetPlayerTime(
                randomTime()
            ),
            ToggleNametagDisplay(
                displayNametag = randomProp(Boolean::class)
            ),
            GlobalVariable(
                variable = randomWord(),
                op = randomProp(StatOp::class),
                amount = randomStatValue(),
                unset = randomProp(Boolean::class)
            ),
            TeamVariable(
                teamName = "test",
                variable = randomWord(),
                op = randomProp(StatOp::class),
                amount = randomStatValue(),
                unset = randomProp(Boolean::class)
            ),
            ClearAllPotionEffects(),
            DisplayActionBar(message = randomWords(10)),
            DisplayTitle(
                title = randomWords(10),
                subtitle = randomWords(10),
                fadeIn = randomProp(Int::class),
                stay = randomProp(Int::class),
                fadeOut = randomProp(Int::class)
            ),
            FailParkour(reason = randomWords(10)),
            FullHeal(),
            GiveExperienceLevels(levels = randomProp(Int::class)),
            GiveItem(
                item = randomStack(),
                allowMultiple = randomProp(Boolean::class),
                inventorySlot = randomSlot(),
                replaceExistingItem = randomProp(Boolean::class)
            ),
            KillPlayer(),
            ParkourCheckpoint(),
            PlaySound(
                sound = randomProp(Sound::class),
                volume = randomProp(Double::class),
                pitch = randomProp(Double::class),
                location = randomLocation()
            ),
            RemoveItem(
                item = randomStack()
            ),
            ResetInventory(),
            SendMessage(message = randomWords(10)),
            SendToLobby(location = Lobby.MainLobby),
            SetCompassTarget(location = randomLocation()),
            SetGameMode(gamemode = GameMode.Creative),
            ChangeHunger(op = randomProp(StatOp::class), amount = randomProp(Double::class)),
            ChangeMaxHealth(op = randomProp(StatOp::class), amount = randomProp(Double::class), healOnChange = randomProp(Boolean::class)),
            TeleportPlayer(location = randomLocation(), preventInsideBlocks = randomProp(Boolean::class)),
            ExecuteFunction(name = "test", global = randomProp(Boolean::class)),
            EnchantHeldItem(enchantment = randomProp(Enchantment::class), level = randomProp(Int::class)),
            DisplayMenu(menu = "test"),
            PauseExecution(ticks = randomProp(Int::class)),
            SetPlayerTeam(team = "test")
        ).plus(
            if (nested) {
                emptyList()
            } else {
                listOf(
                    Conditional(
                        conditions = List((1..10).random()) { createRandomCondition() },
                        matchAnyCondition = randomProp(Boolean::class),
                        ifActions = List((1..5).random()) { createRandomAction(true) },
                        elseActions = List((0..5).random()) { createRandomAction(true) }
                    ),
                    RandomAction(
                        actions = List((1..5).random()) { createRandomAction(true) }
                    )
                )
            }
        )
        return actions.random()
    }

    fun createRandomCondition(): Condition {
        val conditions = listOf(
            PlayerVariableRequirement(
                variable = randomWord(),
                op = randomProp(Comparison::class),
                value = randomStatValue(),
            ),
            GlobalVariableRequirement(
                variable = randomWord(),
                op = randomProp(Comparison::class),
                value = randomStatValue(),
            ),
            TeamVariableRequirement(
                team = "test",
                variable = randomWord(),
                op = randomProp(Comparison::class),
                value = randomStatValue(),
            ),
            RequiredGroup(
                group = "test",
                includeHigherGroups = randomProp(Boolean::class),
            ),
            HasPermission(
                permission = randomProp(Permission::class),
            ),
            InRegion(
                region = "test"
            ),
            HasItem(
                item = randomStack(),
                whatToCheck = randomProp(ItemCheck::class),
                whereToCheck = randomProp(InventoryLocation::class),
                amount = randomProp(ItemAmount::class)
            ),
            InParkour(),
            RequiredEffect(
                effect = randomProp(PotionEffect::class),
            ),
            PlayerSneaking(),
            PlayerFlying(),
            RequiredHealth(
                mode = randomProp(Comparison::class),
                amount = randomProp(Double::class)
            ),
            RequiredMaxHealth(
                mode = randomProp(Comparison::class),
                amount = randomProp(Double::class)
            ),
            RequiredHungerLevel(
                mode = randomProp(Comparison::class),
                amount = randomProp(Double::class)
            ),
            RequiredGameMode(
                gameMode = randomProp(GameMode::class),
            ),
            RequiredPlaceholderNumber(
                placeholder = "%player.health%",
                mode = randomProp(Comparison::class),
                amount = randomStatValue(),
            ),
            RequiredTeam(
                team = "test"
            ),
        )
        return conditions.random()
    }

    fun randomSlot(): InventorySlot {
        val subClazz = InventorySlot::class.sealedSubclasses.random()
        if (subClazz == InventorySlot.ManualInput::class) {
            return InventorySlot.ManualInput((-2..40).random())
        } else {
            val constructor = subClazz.primaryConstructor!!
            val params = constructor.parameters.associateWith { param ->
                when (param.type.classifier) {
                    Int::class -> (0..9).random()
                    else -> null
                }
            }
            return constructor.callBy(params)
        }
    }

    fun randomStatValue(): StatValue {
        val types = listOf(
            StatValue.I32((0..100).random()),
            StatValue.Lng((0..1000).random().toLong()),
            StatValue.Str(randomWords((1..5).random())),
            StatValue.Dbl(Math.random() * 100)
        )
        return types.random()
    }

    fun <T : Any> randomProp(clazz: KClass<T>): T {
        val returnVal = when (clazz) {
            String::class -> randomWords((1..5).random()) as T
            Int::class -> (0..100).random() as T
            Double::class -> (Math.random() * 100) as T
            Float::class -> (Math.random() * 100).toFloat() as T
            Boolean::class -> listOf(true, false).random() as T
            else -> null
        }

        if (clazz.isSubclassOf(Enum::class) && returnVal == null) {
            val enumConstants = clazz.java.enumConstants
            return enumConstants.random() as T
        }

        if (returnVal == null) error("Could not generate random value for class ${clazz.simpleName}")

        return returnVal
    }

    fun randomTime(): Time {
        val subClazz = Time::class.sealedSubclasses.random()
        return if (subClazz == Time.Custom::class) {
            Time.Custom((0..24000).random().toLong())
        } else {
            subClazz.objectInstance!!
        }
    }

    fun randomLocation(): Location {
        val random = Math.random()
        return when {
            random < 0.25 -> {
                Location.InvokersLocation
            }

            random < 0.5 -> {
                Location.CurrentLocation
            }

            random < 0.75 -> {
                Location.HouseSpawn
            }

            else -> {
                Location.Custom(
                    relX = listOf(true, false).random(),
                    relY = listOf(true, false).random(),
                    relZ = listOf(true, false).random(),
                    relPitch = listOf(false).random(),
                    relYaw = listOf(false).random(),
                    x = Math.random() * 100,
                    y = Math.random() * 100,
                    z = Math.random() * 100,
                    pitch = if (Math.random() > 0.5) Math.random().toFloat() * 90f else null,
                    yaw = if (Math.random() > 0.5) Math.random().toFloat() * 90f else null
                )
            }
        }
    }

    fun randomStack(): ItemStack {
        val item = listOf(
            Items.PAPER,
            Items.DIAMOND_AXE,
            Items.GOLDEN_APPLE,
            Items.ARROW,
            Items.BOW,
            Items.COOKED_BEEF,
            Items.IRON_HELMET,
            Items.ENDER_PEARL
        ).random()
        val stack = net.minecraft.item.ItemStack(item)
        stack.count = (1..64).random()
        if (Math.random() > 0.5) {
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(randomWords((1..10).random())))
        }
        return ItemStack(
            nbt = ItemUtils.toNBT(stack),
            relativeFileLocation = ""
        )
    }

    fun randomWord(): String {
        return randomWords(1)
    }

    fun randomWords(amount: Int): String {
        val words = listOf(
            "Alpha", "Bravo", "Charlie", "Delta", "Echo",
            "Foxtrot", "Golf", "Hotel", "India", "Juliet",
            "Kilo", "Lima", "Mike", "November", "Oscar",
            "Papa", "Quebec", "Romeo", "Sierra", "Tango",
            "Uniform", "Victor", "Whiskey", "X-ray", "Yankee", "Zulu"
        )
        return (1..amount).joinToString(" ") { words.random() }
    }
}