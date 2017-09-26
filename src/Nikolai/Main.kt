package Nikolai

import Nikolai.Utilities.Coord
import Nikolai.Utilities.Companion.intGridVisualised
import Nikolai.Utilities.Companion.array2dOfInt

fun allBurrowersAreDead(burrowers: Array<Burrower>): Boolean
{
    return !burrowers.none { it.isDead }
}

fun getBurrowedGrid(gridSize: Coord, amountOfBurrowers: Int): Array<IntArray>
{
    if (amountOfBurrowers < 1) { throw IllegalArgumentException("Amount of burrowers is $amountOfBurrowers, which is less than 1") }

    val grid = array2dOfInt(gridSize.y, gridSize.x)
    val burrowers = Array(amountOfBurrowers, { Burrower(grid) })

    while (!allBurrowersAreDead(burrowers))
        burrowers.filter { !it.isDead }.forEach { it.burrow() }
    return grid
}

fun main(args: Array<String>)
{
    val gridSizeHere = Coord(400, 16)
    val amountOfBurrowersHere = 15

    val grid = getBurrowedGrid(gridSizeHere, amountOfBurrowersHere)
    //println(intGridToString(grid)) // <---- This will print out the 0s and 128s, the values actually in the grid.
    println(intGridVisualised(grid)) // <----  This will print it as a more easy-to-see grid, for debugging purposes.

}