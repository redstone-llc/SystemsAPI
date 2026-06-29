package llc.redstone.systemsapi.util

object ErrorCorrection {
    data class BasicClick(val slot: Int, val button: Int)

    var lastPlayerSlotClick: BasicClick? = null
    var lastMenuSlotClick: BasicClick? = null

    var lastTextInput: String? = null

    suspend fun isMenuCorrect(): Boolean {
        try {
            MenuUtils.onOpen(
                MenuUtils.pendingNameMatch,
                *MenuUtils.pendingClazz,
                errorCorrection = false
            )
            return true
        } catch (_: Exception) {
            return false
        }
    }

    suspend fun onMenuTimeout(): Boolean {
        lastPlayerSlotClick?.let {
            MenuUtils.clickPlayerSlot(it.slot, it.button)

            lastPlayerSlotClick = null

            if (isMenuCorrect()) {
                return true
            }
        }

        lastMenuSlotClick?.let {
            MenuUtils.interactionClick(it.slot, it.button)

            lastMenuSlotClick = null

            if (isMenuCorrect()) {
                return true
            }
        }

        lastTextInput?.let {
            InputUtils.textInput(it)

            lastTextInput = null

            if (isMenuCorrect()) {
                return true
            }
        }

        return false
    }
}