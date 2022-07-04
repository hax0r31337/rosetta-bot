package test.rosetta

import me.liuli.rosetta.bot.MinecraftBot
import me.liuli.rosetta.bot.event.*
import me.liuli.rosetta.entity.EntityPlayer
import me.liuli.rosetta.util.getEyesLocation
import me.liuli.rosetta.util.getRotationOf
import me.liuli.rosetta.util.vec.Vec3d
import me.liuli.rosetta.world.data.EnumBlockFacing

class EventListener(val bot: MinecraftBot) : ListenerSet() {

    @Listen
    fun onDisconnect(event: DisconnectEvent) {
        println("Disconnect: ${event.reason}")
    }

    @Listen
    fun onChat(event: ChatReceiveEvent) {
        println(event.message)
        if (event.message.contains("/register")) {
            bot.chat("/register passwd0000 passwd0000")
        } else if (event.message.contains("/login")) {
            bot.chat("/login passwd0000")
        } else if (event.message.contains("forward")) {
            bot.controller.forward = !bot.controller.forward
            bot.chat("MOVE_F ${bot.controller.forward}")
        } else if (event.message.contains("jump")) {
            bot.controller.jump = !bot.controller.jump
            bot.chat("MOVE_J ${bot.controller.jump}")
        } else if (event.message.contains("sneak")) {
            bot.player.sneaking = !bot.player.sneaking
            bot.chat("MOVE_SN ${bot.player.sneaking}")
        } else if (event.message.contains("sprint")) {
            bot.player.sprinting = !bot.player.sprinting
            bot.chat("MOVE_SP ${bot.player.sprinting}")
        }
    }

//    @Listen
//    fun onTitle(event: TitleEvent) {
//        println("${event.type} ${event.message}")
//    }

    @Listen
    fun onPreMotion(event: PreMotionEvent) {
    }

//    @Listen
//    fun onPacketReceive(event: PacketReceiveEvent) {
//        println(event.packet)
//    }

    @Listen
    fun onDeath(event: PlayerDeathEvent) {
        println("Death: ${event.cause}")
        bot.protocol.respawn()
    }
}