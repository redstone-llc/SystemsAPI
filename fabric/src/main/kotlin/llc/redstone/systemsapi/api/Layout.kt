package llc.redstone.systemsapi.api

import net.minecraft.item.ItemStack

/**
 * Represents a saved inventory layout within Housing.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Layout {
    /**
     * The name of this layout. This technically does not need to be unique but having multiple
     * layouts with the same name will mess with the API's ability to act on a specific layout, as
     * the name is our only identifier.
     */
    var name: String

    /**
     * Returns the current name of this layout.
     *
     * @return the layout name
     */
    suspend fun getName(): String = name

    /**
     * Returns the item stored in the helmet slot for this layout, or null if empty.
     *
     * @return helmet [ItemStack] or null
     */
    suspend fun getHelmet(): ItemStack?

    /**
     * Sets the helmet item for this layout.
     *
     * @param stack the [ItemStack] to set as helmet
     * @return the updated [Layout] instance
     */
    suspend fun setHelmet(stack: ItemStack): Layout

    /**
     * Returns the item stored in the chestplate slot for this layout, or null if empty.
     *
     * @return chestplate [ItemStack] or null
     */
    suspend fun getChestplate(): ItemStack?

    /**
     * Sets the chestplate item for this layout.
     *
     * @param stack the [ItemStack] to set as chestplate
     * @return the updated [Layout] instance
     */
    suspend fun setChestplate(stack: ItemStack): Layout

    /**
     * Returns the item stored in the leggings slot for this layout, or null if empty.
     *
     * @return leggings [ItemStack] or null
     */
    suspend fun getLeggings(): ItemStack?

    /**
     * Sets the leggings item for this layout.
     *
     * @param stack the [ItemStack] to set as leggings
     * @return the updated [Layout] instance
     */
    suspend fun setLeggings(stack: ItemStack): Layout

    /**
     * Returns the item stored in the boots slot for this layout, or null if empty.
     *
     * @return boots [ItemStack] or null
     */
    suspend fun getBoots(): ItemStack?

    /**
     * Sets the boots item for this layout.
     *
     * @param stack the [ItemStack] to set as boots
     * @return the updated [Layout] instance
     */
    suspend fun setBoots(stack: ItemStack): Layout

    /**
     * Returns the hotbar contents as an array of items.
     *
     * @return array containing hotbar [ItemStack]s
     */
    suspend fun getHotbar(): Array<ItemStack>

    /**
     * Sets the hotbar contents for this layout.
     *
     * @param stacks array of [ItemStack]s to use as the hotbar (length within 1..8)
     * @throws IllegalArgumentException if stacks length is out of acceptable range
     * @return the updated [Layout] instance
     */
    suspend fun setHotbar(stacks: Array<ItemStack>): Layout

    /**
     * Returns the full inventory contents as an array of items.
     *
     * @return array containing inventory [ItemStack]s
     */
    suspend fun getInventory(): Array<ItemStack>

    /**
     * Sets the full inventory contents for this layout.
     *
     * @param stacks array of [ItemStack]s to use as the inventory (length within 1..27)
     * @throws IllegalArgumentException if stacks is out of acceptable range
     * @return the updated [Layout] instance
     */
    suspend fun setInventory(stacks: Array<ItemStack>): Layout

    /**
     * Deletes this layout.
     */
    suspend fun delete()
}