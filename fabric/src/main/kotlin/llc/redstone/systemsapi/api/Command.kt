package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer

/**
 * Represents a command within Housing.
 *
 * Provides accessors and mutators for all command settings, and its associated action container.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Command {
    /**
     * The unique name of this command, omitting the slash.
     */
    var name: String

    /**
     * Gets the current name of this command.
     *
     * @return the current command name, omitting the slash
     */
    suspend fun getName(): String = name

    /**
     * Sets the name of this command.
     *
     * @param newName the new name to set (length must be within 1..30)
     * @throws IllegalArgumentException if newName length is outside acceptable range
     * @return the updated [Command] instance
     */
    suspend fun setName(newName: String): Command

    /**
     * Gets the targeting mode that this command runs with.
     *
     * @return the command targeting mode
     */
    suspend fun getCommandMode(): CommandMode

    /**
     * Sets the targeting mode that this command will run with.
     *
     * @param newCommandMode the new command targeting mode to apply
     * @return the [Command] instance
     */
    suspend fun setCommandMode(newCommandMode: CommandMode): Command

    /**
     * Retrieves the group priority required to execute this command.
     *
     * @return integer priority representing the required group priority
     */
    suspend fun getRequiredGroupPriority(): Int

    /**
     * Sets the minimum group priority required to execute this command.
     *
     * @param newPriority the new required group priority (0..20)
     * @throws IllegalArgumentException if newPriority is out of acceptable range
     * @return the [Command] instance
     */
    suspend fun setRequiredGroupPriority(newPriority: Int): Command

    /**
     * Fetches whether the command will be listed in command listings/tab complete.
     *
     * @return true if listed, false otherwise
     */
    suspend fun getListed(): Boolean

    /**
     * Controls whether this command appears in command listings/tab complete.
     *
     * @param newListed true to include the command in listings, false to hide it
     * @return the [Command] instance
     */
    suspend fun setListed(newListed: Boolean): Command

    /**
     * Returns the action container associated with this command.
     *
     * @return the [ActionContainer] containing the command's actions
     */
    suspend fun getActionContainer(): ActionContainer

    /**
     * Deletes this command.
     */
    suspend fun delete()

    /**
     * The mode in which a command is executed: either on the sender itself or on
     * a target provided via command argument.
     */
    enum class CommandMode {
        /** The command acts on the executor itself (no external target). */
        SELF,
        /** The command acts on a specified target (e.g. another player). */
        TARGETED
    }
}