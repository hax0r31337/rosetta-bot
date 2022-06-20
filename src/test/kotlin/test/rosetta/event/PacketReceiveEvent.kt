package test.rosetta.event

import com.github.steveice10.mc.protocol.packet.MinecraftPacket
import me.liuli.rosetta.bot.event.EventCancellable

/**
 * this is a custom event called when MCProtocolLib received a packet from server,
 * cancelling it will prevent rosetta process the packet
 */
class PacketReceiveEvent(val packet: MinecraftPacket): EventCancellable()