/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents;

import io.github.retrooper.packetevents.event.PacketListenerAbstract;
import io.github.retrooper.packetevents.event.impl.PacketDecodeEvent;
import io.github.retrooper.packetevents.settings.PacketEventsSettings;
import io.github.retrooper.packetevents.utils.netty.bytebuf.ByteBufAbstract;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketEventsPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        PacketEventsSettings settings = PacketEvents.create(this).getSettings();
        settings
                .fallbackServerVersion(ServerVersion.v_1_7_10)
                .compatInjector(false)
                .checkForUpdates(true)
                .bStats(true);
        PacketEvents.get().loadAsyncNewThread();
        //You can do something here as it is loading
    }

    @Override
    public void onEnable() {
        PacketEvents.get().registerListener(new PacketListenerAbstract() {
            public int readVarInt(ByteBufAbstract byteBuf) {
                int i = 0;
                int j = 0;
                while (true) {
                    byte b0 = byteBuf.readByte();
                    i |= (b0 & Byte.MAX_VALUE) << j++ * 7;
                    if (j > 5)
                        throw new RuntimeException("VarInt too big");
                    if ((b0 & 0x80) != 128)
                        return i;
                }
            }

            @Override
            public void onPacketDecode(PacketDecodeEvent event) {
                if (event.getPlayer() != null) {
                    int packetID = readVarInt(event.getByteBuf());
                    event.getPlayer().sendMessage("id: " + packetID);
                }
            }
        });
        PacketEvents.get().init();
    }

    @Override
    public void onDisable() {
        PacketEvents.get().terminate();
    }
}