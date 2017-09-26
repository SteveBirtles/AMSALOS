package Nikolai

import java.util.*

class Utilities
{
    companion object
    {
        fun randomElement(range: ClosedRange<Int>) = Random().nextInt(range.endInclusive- range.start) +  range.start

        fun <T> randomElement(array: Array<T>): T
        {
            val randomIndex = (Math.random() * array.size).toInt()
            return array[randomIndex]
        }

        fun <T> arrayToString(array: Array<T>): String
        {
            if (array.isNotEmpty())
            {
                var stringy = "${array[0]}"
                for (i in 1 until array.size) stringy = "$stringy, ${array[i]}"
                return stringy
            }
            else { return "EMPTY ARRAY" }
        }

        fun intGridToString(grid: Array<IntArray>): String
        {
            var stringy = ""
            for (row in grid)
            {
                stringy = "$stringy${row[0]}"
                for (i in 1 until grid.size) { stringy = "$stringy, ${row[i]}" }
                stringy = "$stringy\n"
            }
            return stringy
        }

        fun intGridVisualised(grid: Array<IntArray>): String
        {
            val oldWalkableString = Burrower.Companion.walkableTileID.toString()
            return intGridToString(grid).replace("0", "■").replace(oldWalkableString, "□").replace(", ", "  ")
        }

        fun array2dOfInt(sizeOuter: Int, sizeInner: Int): Array<IntArray> = Array(sizeOuter) { IntArray(sizeInner) }
    }



    class Coord(val x: Int, val y: Int)
    {
        companion object { val empty = Coord(-1, -1) }

        override fun toString(): String { return "($x, $y)" }
    }
}