package me.liuli.rosetta.world.block

class ComplexShape(vararg shape: Shape) : Shape(0.0, 0.0, 0.0, 0.0, 0.0, 0.0) {

    val shapes = shape.toMutableList()

    init {
        sync()
    }

    fun sync() {
        this.minX = shapes.minByOrNull { it.minX }?.minX ?: 0.0
        this.minY = shapes.minByOrNull { it.minY }?.minY ?: 0.0
        this.minZ = shapes.minByOrNull { it.minZ }?.minZ ?: 0.0
        this.maxX = shapes.maxByOrNull { it.maxX }?.maxX ?: 0.0
        this.maxY = shapes.maxByOrNull { it.maxY }?.maxY ?: 0.0
        this.maxZ = shapes.maxByOrNull { it.maxZ }?.maxZ ?: 0.0
    }

    override fun toString(): String {
        return "ComplexShape[${shapes.joinToString(", ")}]"
    }
}