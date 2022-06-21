package me.liuli.rosetta.world.block

class MultipleBB(vararg bb: AxisAlignedBB) : AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0) {

    val bbs = bb.toMutableList()

    init {
        sync()
    }

    fun sync() {
        this.minX = bbs.minByOrNull { it.minX }?.minX ?: 0.0
        this.minY = bbs.minByOrNull { it.minY }?.minY ?: 0.0
        this.minZ = bbs.minByOrNull { it.minZ }?.minZ ?: 0.0
        this.maxX = bbs.maxByOrNull { it.maxX }?.maxX ?: 0.0
        this.maxY = bbs.maxByOrNull { it.maxY }?.maxY ?: 0.0
        this.maxZ = bbs.maxByOrNull { it.maxZ }?.maxZ ?: 0.0
    }

    override fun toString(): String {
        return "MultipleBB[${bbs.joinToString(", ")}]"
    }
}