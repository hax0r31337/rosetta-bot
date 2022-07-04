package me.liuli.rosetta.pathfinding

import me.liuli.rosetta.bot.event.ListenerSet

class Pathfinder(val settings: PathfinderSettings) : ListenerSet() {

    init {
        settings.bot.registerListeners(*(this.listeners))
    }
}