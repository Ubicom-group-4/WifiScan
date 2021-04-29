package com.example.wifiscan

import kotlin.math.pow
import kotlin.math.sqrt

object NN {
    private fun distance(fst: Entry, snd: Entry): Double {

        // dist = sqrt((ss1-e1)^2+(ss2-e2)^2+...+(ssm-em)^2)
        val sofar = fst.strengths.map { y ->
            y.second - (snd.strengths.find { x -> x.first == y.first }?.second ?: Int.MIN_VALUE)
        }
                .map { it.toDouble().pow(2.0) }.sum()

        return sqrt(sofar)
    }

    fun nearestNeighbour(entries: List<Entry>, single: Entry): String {

        return entries.map { Pair(it.location, distance(single, it)) }.minBy { it.second }!!.first
    }
}

