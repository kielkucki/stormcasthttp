package com.gungens.spongehttp;

import com.google.inject.Inject;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.net.InetSocketAddress;

@Plugin(id = "goldchecker", name = "Gold Checker", version = "1.0", description = "A plugin to check if a player has 16 gold ingots and send a POST request")
public class SpongeHTTP {

    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("GoldChecker plugin loaded and ready to check for gold!");
        CommandSpec storeCmd = StoreLink.build();
        Sponge.getCommandManager().register(this, storeCmd, "store");
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);
            server.createContext("/auth", new PlayerPurchaseHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port 8000");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        player.getTabList().setHeader(Text.of(TextColors.GREEN,"Welcome to Epstein's lovely island"));

    }


}
