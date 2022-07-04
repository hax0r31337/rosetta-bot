package me.liuli.rosetta.pathfinding.algorithm

import me.liuli.rosetta.pathfinding.PathfinderSettings
import me.liuli.rosetta.pathfinding.goals.IGoal
import me.liuli.rosetta.pathfinding.path.Move
import me.liuli.rosetta.world.Chunk

class AStar(start: Move, private val settings: PathfinderSettings, val goal: IGoal) {

    private val closedDataSet = hashSetOf<Int>()
    private val openHeap = BinaryHeapOpenSet()
    private val openDataMap = hashMapOf<Int, PathNode>()
    private val visitedChunks = hashSetOf<Long>()
    private val maxCost: Int

    private var bestNode: PathNode

    init {
        val startNode = PathNode(start, .0, goal.heuristic(start))
        openHeap.push(startNode)
        openDataMap[startNode.data.hashCode()] = startNode
        bestNode = startNode

        maxCost = if (settings.searchRadius < 0) -1 else startNode.h.toInt() + settings.searchRadius
    }

    fun compute(): Result {
        val computeStartTime = System.currentTimeMillis()
        while (!openHeap.isEmpty()) {
            if (System.currentTimeMillis() - computeStartTime > settings.searchTimeout) {
                return makeResult(ResultStatus.TIMEOUT, this.bestNode, computeStartTime)
            }
            val node = this.openHeap.pop()
            if (this.goal.isEnd(node.data)) {
                return makeResult(ResultStatus.SUCCESS, node, computeStartTime)
            }
            // not done yet
            this.openDataMap.remove(node.data.hashCode())
            this.closedDataSet.add(node.data.hashCode())
            this.visitedChunks.add(getChunkCode(node.data.x, node.data.z))

            val neighbors = this.settings.getNeighbors(node.data)
            for (neighborData in neighbors) {
                if (this.closedDataSet.contains(neighborData.hashCode())) {
                    continue // skip closed neighbors
                }
                val gFromThisNode = node.g + neighborData.cost
                var neighborNode = this.openDataMap[neighborData.hashCode()]
                var update = false

                val heuristic = this.goal.heuristic(neighborData)
                if (this.maxCost > 0 && gFromThisNode + heuristic > this.maxCost) continue

                if (neighborNode == null) {
                    // add neighbor to the open set
                    neighborNode = PathNode()
                    // properties will be set later
                    this.openDataMap.set(neighborData.hashCode(), neighborNode)
                } else {
                    if (neighborNode.g < gFromThisNode) {
                        // skip this one because another route is faster
                        continue
                    }
                    update = true
                }
                // found a new or better route.
                // update this neighbor with this node as its new parent
                neighborNode.set(neighborData, gFromThisNode, heuristic, node)
                if (neighborNode.h < this.bestNode.h) this.bestNode = neighborNode
                if (update) {
                    this.openHeap.update(neighborNode)
                } else {
                    this.openHeap.push(neighborNode)
                }
            }
        }
        // all the neighbors of every accessible node have been exhausted
        return makeResult(ResultStatus.NO_PATH, this.bestNode, computeStartTime)
    }

    private fun getChunkCode(x: Int, z: Int): Long {
        return Chunk.code(x shr 4, z shr 4)
    }

    private fun makeResult(status: ResultStatus, node: PathNode, startTime: Long): Result {
        val time = System.currentTimeMillis() - startTime
        val path = mutableListOf<Move>()
        var iterNode: PathNode? = node
        while(iterNode?.parent != null) {
            path.add(iterNode.data)
            iterNode = iterNode.parent
        }
        path.reverse()

        return Result(status, node.g, time, this.closedDataSet.size,
            this.closedDataSet.size + this.openHeap.size(), path, this)
    }

    data class Result(val status: ResultStatus, val cost: Double, val timeCost: Long,
                      val visitedNodes: Int, val generatedNodes: Int, val path: MutableList<Move>, val context: AStar)

    enum class ResultStatus {
        TIMEOUT,
        NO_PATH,
        SUCCESS
    }

}