package com.example.wifiscan

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Nearest Neighbour Classifier
 */
object NN {

    fun nearestNeighbour(entries: List<Entry>, single: Entry) =
        entries.fixMissingValues().nearestNeighbourForFixedValues(single)

    private fun List<Entry>.nearestNeighbourForFixedValues(single: Entry) =
        this.map { Pair(it.location, single.distance(it, this)) }
            .minByOrNull { it.second }!!
            .first

    /**
     * Calculate average for all value of the given SSID in the data set
     */
    private fun List<Entry>.avg(ssid: String) =
        this.mapNotNull { it.findBySSID(ssid) }
            .map { it.second }
            .average()
            .toInt()

    /**
     * Find value for SSID, if it doesnt exist calculate the average of all values
     * of that SSID in the data set.
     */
    private fun Entry.findOther(entries: List<Entry>, ssid: String) =
        this.strengths.find { it.first == ssid }?.second ?: entries.avg(ssid)

    /**
     * Eucledian distance formula
     * dist = sqrt((ss1-e1)^2+(ss2-e2)^2+...+(ssm-em)^2)
     */
    private fun Entry.distance(snd: Entry, entries: List<Entry>) = sqrt(
        this.strengths.map { it.second - snd.findOther(entries, it.first) }
            .map { it.toDouble().pow(2.0) }
            .sum()
    )

    /**
     * Find  an SSID value pair for the given SSID.
     */
    private fun Entry.findBySSID(ssid: String) = this.strengths.find { it.first == ssid }

    /**
     * Go through all SSIDs, try to find a match for each SSID int an entry.
     * If it doesnt exist create a new one with an average value of all values of that SSID.
     */
    private fun Entry.fixMissingSSIDs(entries: List<Entry>) =
        this.copy(strengths = entries.findSSIDs().map {
            this.findBySSID(it) ?: Pair(it, entries.avg(it))
        })

    /**
     * Find all SSIDs in the data set.
     */
    private fun List<Entry>.findSSIDs() =
        this.flatMap { it.strengths }
            .map { it.first }
            .toHashSet()

    /**
     * Fix Missing values in the data set.
     */
    private fun List<Entry>.fixMissingValues() = this.map { it.fixMissingSSIDs(this) }
}
