package Nikolai

import Nikolai.Utilities.Companion.randomElement
import Nikolai.Utilities.*
class Burrower(private val grid: Array<IntArray>)
{

    private var coord: Coord
    var isDead = false; private set

    companion object { val walkableTileID = 128 }

    init { coord = Coord(randomElement(0 until grid[0].size), randomElement(0 until grid.size)) }

    private fun coordToBurrowTo(): Coord
    {
        val x = coord.x
        val y = coord.y

        val allPossibleCoords: Array<Coord> = arrayOf(
                Coord(x - 1, y - 1), Coord(x - 1, y), Coord(x - 1, y + 1),
                Coord(x, y - 1), Coord(x, y + 1),
                Coord(x + 1, y - 1), Coord(x + 1, y), Coord(x + 1, y + 1)
        )
        val validCoords = allPossibleCoords.filter { it.x >= 0 && it.x < grid[0].size
                && it.y >= 0 && it.y < grid.size
                && grid[it.y][it.x] == 0}.toTypedArray()
        return if (validCoords.isNotEmpty()) randomElement(validCoords) else Coord.empty
    }

    fun burrow()
    {
        coord = coordToBurrowTo()
        if (coord != Coord.empty) { grid[coord.y][coord.x] = walkableTileID
        }
        else { isDead = true }
    }
}