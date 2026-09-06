package llc.redstone.systemsapi.importer

//? if >=26.2 {
/*import llc.redstone.systemsapi.screen
*///?}

import llc.redstone.systemsapi.SystemsAPI.LOGGER
import llc.redstone.systemsapi.SystemsAPI.MC
import llc.redstone.systemsapi.SystemsAPI.scaledDelay
import llc.redstone.systemsapi.api.ProgressPhase
import llc.redstone.systemsapi.progress.*
import llc.redstone.systemsapi.util.ItemStackUtils.loreLines
import llc.redstone.systemsapi.util.MenuUtils
import llc.redstone.systemsapi.util.PredicateUtils.ItemMatch.ItemExact
import llc.redstone.systemsapi.util.PredicateUtils.ItemSelector
import llc.redstone.systemsapi.util.PredicateUtils.NameMatch.NameExact
import llc.redstone.systemsapi.util.TextUtils
import llc.redstone.systemsdata.Action
import llc.redstone.systemsdata.ActionDefinition
import llc.redstone.systemsdata.VariableHolder
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Items
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

//The title of the actions gui, either Actions: <name> or Edit Actions
class ActionContainer(
    val title: String =
        if (MC.screen?.title?.string?.matches(Regex("Edit Actions")) == true)
            "Edit Actions"
        else if (MC.screen?.title?.string?.matches(Regex("Actions: .*")) == true)
            "Actions: "
        else MC.screen?.title?.string
            ?: throw IllegalStateException("No screen is currently open")
) {
    companion object {
        private val slots = mutableMapOf(
            0 to 10,
            1 to 11,
            2 to 12,
            3 to 13,
            4 to 14,
            5 to 15,
            6 to 16,
            7 to 19,
            8 to 20,
            9 to 21,
            10 to 22,
            11 to 23,
            12 to 24,
            13 to 25,
            14 to 28,
            15 to 29,
            16 to 30,
            17 to 31,
            18 to 32,
            19 to 33,
            20 to 34,
        )
    }

    suspend fun getActions() = getActions(true)

    suspend fun getActions(exportItems: Boolean): List<Action> = ProgressTracker.runRootIfIdle(
        ProgressPhase.EXPORTING,
        planned = null,
        label = "Read $title",
        indeterminate = true,
    ) {
        MenuUtils.onOpen(title)

        if (MenuUtils.findSlots(MenuItems.NO_ACTIONS).firstOrNull() != null) {
            ExportPlanner.finishDiscovery()
            return@runRootIfIdle emptyList()
        }

        // Pass one: walk forward to the last page, pricing each from its lore alone. By the time any
        // expensive reading starts, the whole container's cost is known, instead of growing every
        // time a nested container happens to be opened.
        var pages = 0
        while (true) {
            pages++
            ExportPlanner.discoverPage(
                currentPageSlots(),
                if (pages == 1) ExportPlanner.FIRST_PAGE_ROUNDTRIPS else ExportPlanner.LATER_PAGE_ROUNDTRIPS,
            )
            if (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.NEXT_PAGE).firstOrNull() == null) break
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
            MenuUtils.onOpen(" $title", checkIfOpen = false)
        }
        ExportPlanner.finishDiscovery()

        // Go back to page 1
        if (pages > 1) {
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.PREVIOUS_PAGE, button = 1)
            MenuUtils.onOpen(title, checkIfOpen = false)
            LOGGER.debug("Export: returned to page 1 of {}, now on '{}'", pages, MC.screen?.title?.string)
        }

        // Pass two: read forward again, one page at a time, with the same navigation proven in pass
        // one -- the total is already known from discovery, so this pass is purely reading.
        val actions = mutableListOf<Action>()
        while (true) {
            actions.addAll(readCurrentPage(exportItems))
            if (MenuUtils.findSlots(MenuUtils.GlobalMenuItems.NEXT_PAGE).firstOrNull() == null) break
            MenuUtils.clickItems(MenuUtils.GlobalMenuItems.NEXT_PAGE)
            MenuUtils.onOpen(" $title", checkIfOpen = false)
        }

        actions
    }

    private fun currentPageSlots(): List<Slot> = slots.values.map { MenuUtils.getSlot(it) }

    private suspend fun readCurrentPage(exportItems: Boolean): List<Action> {
        val actions = mutableListOf<Action>()
        for (slotIndex in slots.values) {
            val slot = MenuUtils.getSlot(slotIndex)
            if (!slot.hasItem()) break

            parseAction(slot, exportItems)?.let { actions.add(it) }
        }
        MenuUtils.onOpen(title)
        return actions
    }

    private suspend fun parseAction(slot: Slot, exportItems: Boolean): Action? {
        val item = slot.item
        val loreLines = item.loreLines(true).filter { it.contains(":") }
        val allLines = item.loreLines(true)
        val name = TextUtils.convertTextToString(item.hoverName, false)

        val actionClass = Action::class.sealedSubclasses.firstOrNull {
            it.findAnnotations(ActionDefinition::class).any { ann -> ann.displayName == name }
        } ?: return null

        return buildAction(actionClass, loreLines, allLines, slot, 0, exportItems)
    }

    private suspend fun buildAction(
        actionClass: KClass<out Action>,
        loreLines: List<String>,
        allLines: List<String>,
        slot: Slot,
        indexOffset: Int,
        exportItems: Boolean
    ): Action? {
        val constructor = actionClass.primaryConstructor ?: return null
        val properties = constructor.parameters.mapNotNull { param ->
            val prop = actionClass.memberProperties.find { it.name == param.name } as? KProperty1<Action, *>
            prop?.let { it to param }
        }

        val args = mutableMapOf<KParameter, Any?>()

        for ((index, pair) in properties.withIndex()) {
            val (prop, param) = pair
            var colorValue = loreLines.getOrNull(index + indexOffset)
                ?.split(": ")?.drop(1)?.joinToString(": ")
                ?.replaceFirst("&f", "") ?: continue
            var value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")

            if (value.isEmpty()) {
                //Dont question this :)
                //Used to catch when there is no actions or conditionals
                val index = allLines.indexOf(loreLines.getOrNull(index + indexOffset))
                colorValue = allLines.getOrNull(index + 1)
                    ?.split(" - ")?.drop(1)?.joinToString(" - ")
                    ?.replaceFirst("&f", "") ?: continue
                value = colorValue.replace(Regex("&[0-9a-fk-or]"), "")
            }

            val returnValue =
                PropertySettings.export(title, prop, slot, slots[index + indexOffset]!!, value, colorValue, exportItems)

            // Handle VariableHolder by switching to the appropriate subclass
            if (returnValue is VariableHolder) {
                val newClass = when (returnValue) {
                    VariableHolder.Player -> Action.PlayerVariable::class
                    VariableHolder.Global -> Action.GlobalVariable::class
                    VariableHolder.Team -> Action.TeamVariable::class
                }
                return buildAction(newClass, loreLines, allLines, slot, 1, exportItems)
            }

            args[param] = returnValue
        }


        return if (args.size != constructor.parameters.size) {
            actionClass.constructors.firstOrNull { it.parameters.size == constructor.parameters.size }?.callBy(args)
                ?: constructor.callBy(args)
        } else {
            constructor.isAccessible = true
            constructor.callBy(args)
        }
    }

    suspend fun setActions(newActions: List<Action>) {
        val plan = ImportPlanner.planActions(newActions, title, fresh = true)

        ProgressTracker.runRoot(ProgressPhase.PREPARING, plan, "Import $title") {
            //Clear existing actions
            MenuUtils.onOpen(title)
            if (MenuUtils.findSlots(MenuItems.NO_ACTIONS).firstOrNull() == null) {
                ProgressTracker.setPhase(ProgressPhase.CLEARING)
                var counted = false
                //There are existing actions, remove them
                while (true) {
                    val actionSlots = mutableListOf<Int>()
                    for (slotIndex in slots.values) {
                        val slot = MenuUtils.getSlot(slotIndex)
                        if (!slot.hasItem()) break //No more actions
                        actionSlots.add(slotIndex)
                    }

                    if (!counted) {
                        // One round-trip per action. Only the current page is visible, so this is a
                        // lower bound.
                        counted = true
                        ProgressTracker.reviseTotal(
                            actionSlots.size * CostModel.estimateMs(OpKind.MENU_ROUNDTRIP),
                            actionSlots.size,
                        )
                    }

                    if (MenuUtils.findSlots(MenuItems.NO_ACTIONS).firstOrNull() != null) break

                    MenuUtils.packetClick(10, 1)
                    MenuUtils.onCurrentScreenUpdate()
                }
            }

            //Add new actions
            ProgressTracker.setPhase(ProgressPhase.IMPORTING)
            addActions(newActions, fresh = true, plan = plan)
        }
    }

    suspend fun updateActions(newActions: List<Action>) {
        TODO("Not yet implemented")
    }

    suspend fun addActions(actions: List<Action>) =
        addActions(actions, fresh = false, plan = null)

    /** @param plan a pre-built cost prediction, when the caller already made one. */
    internal suspend fun addActions(actions: List<Action>, fresh: Boolean, plan: PlanCost?) {
        if (actions.isEmpty()) return

        val actionsPlan = plan ?: ImportPlanner.planActions(actions, title, fresh)
        // Owns the run when called on its own, joins the enclosing one when nested.
        ProgressTracker.runRootIfIdle(ProgressPhase.IMPORTING, actionsPlan, "Import $title") {
            addPlannedActions(actions, actionsPlan)
        }
    }

    private suspend fun addPlannedActions(actions: List<Action>, actionsPlan: PlanCost) {
        ProgressTracker.beginScope(actionsPlan)
        try {
            for ((index, action) in actions.withIndex()) {
                val actionPlan = actionsPlan.children.getOrNull(index) ?: PlanCost.EMPTY
                ProgressTracker.beginScope(actionPlan)
                try {
                    //Wait for the "Actions: <name>" or "Edit Actions" to open
                    //We do this every iteration to make sure we are right back at the Actions page
                    MenuUtils.onOpen(title)

                    if (action is Action.CustomAction) {
                        action.function(action.parameters)
                        continue
                    }

                    //Add an action
                    MenuUtils.clickItems(MenuItems.ADD_ACTION)
                    MenuUtils.onOpen("Add Action")

                    //Get the action parameters/properties, in the order the importer walks them
                    val properties = PropertyReflection.propertiesOf(action)

                    //Get the Display Name of the action and add it
                    val displayName =
                        (action::class.annotations.find { it is ActionDefinition } as ActionDefinition).displayName
                    MenuUtils.clickItems(displayName, paginated = true)

                    //Iterate through parameters
                    for ((propertyIndex, property) in properties.withIndex()) {
                        //Get the property and its values
                        val value = property.get(action)

                        val propertyPlan = actionPlan.children.getOrNull(propertyIndex) ?: PlanCost.EMPTY
                        ProgressTracker.beginScope(propertyPlan)
                        val opsBefore = OpRecorder.opCount()
                        try {
                            //Make sure we are in the right gui before continuing
                            MenuUtils.onOpen("Action Settings")

                            //Place in the gui to click
                            val slotIndex = slots[propertyIndex]!!
                            val slot = MenuUtils.getSlot(slotIndex)

                            PropertySettings.import(property, slot, value)
                        } finally {
                            // One operation means only the settings-screen wait happened, so the
                            // property short-circuited.
                            CostModel.recordWork(
                                "${action::class.simpleName}#${property.name}",
                                OpRecorder.opCount() - opsBefore > 1,
                            )
                            ProgressTracker.endScope()
                        }
                    }
                    //Make sure we are in the action settings menu before we go back to actions to add another one
                    if (properties.isNotEmpty()) {
                        MenuUtils.onOpen("Action Settings")
                        MenuUtils.clickItems(MenuItems.BACK)
                    }
                    MenuUtils.onOpen(title)
                } finally {
                    ProgressTracker.endScope()
                }
            }
        } finally {
            ProgressTracker.endScope()
        }
    }

    suspend fun copyToHousingClipboard() {
        MenuUtils.onOpen(title)
        MenuUtils.packetClick(51, 1)
        scaledDelay(1.0)
        if (MenuUtils.findSlots(MenuItems.PASTE_ACTIONS).firstOrNull() == null) {
            error("Failed to copy actions to clipboard")
        }
    }

    suspend fun pasteFromHousingClipboard() {
        MenuUtils.onOpen(title)
        if (MenuUtils.findSlots(MenuItems.PASTE_ACTIONS).firstOrNull() == null) {
            error("Clipboard is empty or does not contain valid actions")
        }
        MenuUtils.packetClick(51, 0)
        scaledDelay(1.0)
    }

    object MenuItems {
        val ADD_ACTION = ItemSelector(
            name = NameExact("Add Action"),
            item = ItemExact(Items.PAPER)
        )
        val BACK = ItemSelector(
            name = NameExact("Go Back"),
            item = ItemExact(Items.ARROW)
        )
        val TOGGLE_ADVANCED_OPERATIONS = ItemSelector(
            name = NameExact("Toggle Advanced Operations"),
            item = ItemExact(Items.COMMAND_BLOCK)
        )
        val NO_ACTIONS = ItemSelector(
            name = NameExact("No Actions!"),
            item = ItemExact(Items.BEDROCK)
        )
        val PASTE_ACTIONS = ItemSelector(
            name = NameExact("Paste Actions"),
            item = ItemExact(Items.BOOK)
        )
    }
}
