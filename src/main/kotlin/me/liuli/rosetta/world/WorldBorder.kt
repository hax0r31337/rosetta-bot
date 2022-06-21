package me.liuli.rosetta.world

class WorldBorder {

    var centerX = 0.0
    var centerZ = 0.0
    var worldSize = 29999984

    val diameter: Double
        get() {
            val time = System.currentTimeMillis()
            if (startDiameter != endDiameter && time < endTime) {
                return startDiameter + (endDiameter - startDiameter) * (time - startTime) / (endTime - startTime)
            } else {
                return endDiameter
            }
        }
    var startDiameter = 6.0E7
    var endDiameter = startDiameter
    var startTime = 0L
    var endTime = 0L

    var warningTime = 15
    var warningDistance = 5

    fun setTransition(startDiameter: Double, endDiameter: Double, time: Long) {
        this.startDiameter = startDiameter
        this.endDiameter = endDiameter
        this.startTime = System.currentTimeMillis()
        this.endTime = this.startTime + time
    }

    fun getBorder(): BorderValue {
        val radius = diameter / 2
        val size = worldSize.toDouble()
        return BorderValue((centerX - radius).coerceAtLeast(-size), (centerZ - radius).coerceAtMost(size),
            (centerX + radius).coerceAtLeast(-size), (centerZ + radius).coerceAtMost(size))
    }

    data class BorderValue(val minX: Double, val maxX: Double, val minZ: Double, val maxZ: Double)
}