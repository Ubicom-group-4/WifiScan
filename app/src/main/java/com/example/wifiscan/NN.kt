package com.example.wifiscan

import kotlin.math.pow
import kotlin.math.sqrt

object NN {

    private fun avg(list: List<Entry>, ssid: String): Int {
        return list.mapNotNull { it.strengths.find { x -> x.first == ssid } }
            .map { it.second }
            .average()
            .toInt()
    }

    private fun findOther(entry: Entry, entries: List<Entry>, ssid: String): Int {
        return entry.strengths.find { it.first == ssid }?.second ?: avg(entries, ssid)
    }

    private fun distance(fst: Entry, snd: Entry, entries: List<Entry>): Double {

        // dist = sqrt((ss1-e1)^2+(ss2-e2)^2+...+(ssm-em)^2)
        val sum = fst.strengths.map { it.second - findOther(snd, entries, it.first) }
            .map { it.toDouble().pow(2.0) }
            .sum()

        return sqrt(sum)
    }

    fun nearestNeighbour(entries: List<Entry>, single: Entry): String {

        val fixedValues = fixMissingValues(entries)

        return fixedValues.map { Pair(it.location, distance(single, it, fixedValues)) }
            .minByOrNull { it.second }!!.first
    }

    private fun fixMissingSSIDs(
        strengths: List<Pair<String, Int>>,
        ssids: HashSet<String>,
        entries: List<Entry>
    ): List<Pair<String, Int>> {
        return ssids.map { strengths.find { x -> x.first == it } ?: Pair(it, avg(entries, it)) }
    }

    private fun fixMissingValues(entries: List<Entry>): List<Entry> {

        val ssids = entries.flatMap { it.strengths }.map { it.first }.toHashSet()

        return entries.map { it.copy(strengths = fixMissingSSIDs(it.strengths, ssids, entries)) }

    }
}

