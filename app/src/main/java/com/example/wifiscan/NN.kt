package com.example.wifiscan

import kotlin.math.pow
import kotlin.math.sqrt

object NN {

    private fun List<Entry>.avg(ssid: String) =
        this.mapNotNull { it.strengths.find { x -> x.first == ssid } }
            .map { it.second }
            .average()
            .toInt()

    private fun Entry.findOther(entries: List<Entry>, ssid: String) =
        this.strengths.find { it.first == ssid }?.second ?: entries.avg(ssid)

    /**
     * dist = sqrt((ss1-e1)^2+(ss2-e2)^2+...+(ssm-em)^2)
     */
    private fun Entry.distance(snd: Entry, entries: List<Entry>) = sqrt(
        this.strengths.map { it.second - snd.findOther(entries, it.first) }
            .map { it.toDouble().pow(2.0) }
            .sum()
    )

    fun nearestNeighbour(entries: List<Entry>, single: Entry): String {
        val fixedValues = entries.fixMissingValues()
        return fixedValues.map { Pair(it.location, single.distance(it, fixedValues)) }
            .minByOrNull { it.second }!!.first
    }

    private fun Entry.fixMissingSSIDs(ssids: HashSet<String>, entries: List<Entry>) =
        this.copy(strengths = ssids.map {
            this.strengths.find { x -> x.first == it } ?: Pair(it, entries.avg(it))
        })

    private fun List<Entry>.findSSIDs() =
        this.flatMap { it.strengths }.map { it.first }.toHashSet()

    private fun List<Entry>.fixMissingValues() =
        this.map { it.fixMissingSSIDs(this.findSSIDs(), this) }
}

