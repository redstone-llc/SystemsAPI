package llc.redstone.systemsapi.api

import llc.redstone.systemsapi.importer.ActionContainer
import net.minecraft.item.ItemStack

/**
 * Represents a custom menu in Housing.
 *
 * Menus have a title, a variable size, and contain menu elements each holding an item
 * and optionally an action container.
 *
 * Implementations perform sequential behavior and therefore expose suspend functions for better
 * interaction with Housing.
 */
interface Menu {
    /**
     * The title of this menu. This technically does not need to be unique but having multiple
     * menus with the same title will mess with the API's ability to act on a specific menu, as
     * the title is our only identifier.
     */
    var title: String

    /**
     * Returns the current title of this menu.
     *
     * @return the menu title
     */
    suspend fun getTitle(): String = title

    /**
     * Sets a new title for this menu.
     *
     * @param newTitle the new title to set (1..32 characters)
     * @throws IllegalArgumentException if newTitle length is out of acceptable range
     * @return the updated [Menu] instance
     */
    suspend fun setTitle(newTitle: String): Menu

    /**
     * Returns the current size (number of rows) of the menu.
     *
     * @return integer representing the menu size
     */
    suspend fun getMenuSize(): Int

    /**
     * Changes the size of the menu.
     *
     * @param newSize the new menu size to apply (must be within 1..6)
     * @throws IllegalArgumentException if newSize is outside acceptable range
     * @return the updated Menu instance
     */
    suspend fun setMenuSize(newSize: Int): Menu

    /**
     * Retrieves the menu element at the given zero-based index.
     *
     * @param index index of the element to retrieve (0..9*newSize - 1)
     * @throws IllegalArgumentException if index is outside acceptable range
     * @return the MenuElement at the specified index
     */
    suspend fun getMenuElement(index: Int): MenuElement

    /**
     * Returns all menu elements as an array.
     *
     * @return array containing all MenuElement instances for this menu
     */
    suspend fun getAllMenuElements(): Array<MenuElement>

    /**
     * Deletes this menu.
     */
    suspend fun delete()

    /**
     * Represents a single element (slot) within a menu.
     *
     * A MenuElement contains a display [ItemStack] and an action container that
     * defines click behavior.
     */
    interface MenuElement {
        /**
         * Returns the item stack displayed in this menu element.
         *
         * @return the [ItemStack] for this element
         */
        suspend fun getItem(): ItemStack

        /**
         * Sets the item stack for this menu element.
         *
         * @param item the [ItemStack] to display
         * @return the updated [MenuElement] instance
         */
        suspend fun setItem(item: ItemStack): MenuElement

        /**
         * Returns the action container attached to this menu element. There must first
         * be an item assigned to the slot before an [ActionContainer] can be configured.
         *
         * @return the [ActionContainer] or null if no item is assigned
         */
        suspend fun getActionContainer(): ActionContainer?
    }
}