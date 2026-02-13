package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import net.minecraft.item.Item

/**
 * Represents a function within Housing.
 *
 * Provides accessors and mutators for all function settings, and its associated action container.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Function {
    /**
     * The name of this function. This technically does not need to be unique but having multiple
     * functions with the same name will mess with the API's ability to act on a specific function, as
     * the name is our only identifier.
     */
    var name: String

    /**
     * Returns the current name of the function.
     *
     * @return the current function name
     */
    suspend fun getName(): String = name

    /**
     * Updates the function name.
     *
     * @param newName the new name to set (1..50 characters)
     * @throws IllegalArgumentException if newName is out of acceptable range
     * @return the updated [Function] instance
     */
    suspend fun setName(newName: String): Function

    /**
     * Returns the current description of the function.
     *
     * @return the function description
     */
    suspend fun getDescription(): String

    /**
     * Sets the description for the function.
     *
     * @param newDescription the new description text (1..120 characters)
     * @throws IllegalArgumentException if newDescription is out of acceptable range
     * @return the updated [Function] instance
     */
    suspend fun setDescription(newDescription: String): Function

    /**
     * Returns the icon item representing this function.
     *
     * @return the Item used as the function's icon
     */
    suspend fun getIcon(): Item

    /**
     * Sets the icon item for this function.
     *
     * @param newIcon the Item to use as the icon
     * @return the updated [Function] instance
     */
    suspend fun setIcon(newIcon: Item): Function

    /**
     * Returns the automatic execution setting, in ticks.
     *
     * @return integer representing automatic execution frequency in ticks, 0 for off.
     */
    suspend fun getAutomaticExecution(): Int

    /**
     * Sets the automatic execution frequency, or disables it with 0.
     *
     * @param newAutomaticExecution new automatic execution frequency, in ticks (0..18000)
     * @throws IllegalArgumentException if newAutomaticExecution is out of acceptable range
     * @return the updated [Function] instance
     */
    suspend fun setAutomaticExecution(newAutomaticExecution: Int): Function

    /**
     * Returns the action container associated with this function.
     *
     * @return the [ActionContainer] containing the function's actions
     */
    suspend fun getActionContainer(): ActionContainer

    /**
     * Deletes this function.
     */
    suspend fun delete()
}