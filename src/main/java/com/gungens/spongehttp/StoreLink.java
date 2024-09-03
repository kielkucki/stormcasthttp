package com.gungens.spongehttp;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class StoreLink implements CommandExecutor {

    static TokenService tokenPlayerNameMap = new TokenService();
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String jsonInputString = "";
        String token = TokenService.generateRandomToken(128);
        Player sender = (Player) src;
        String username = sender.getName();
        String uri = "http://gungens.shop/auth?token="+token+"&username="+username;
        try {
            sender.sendMessage(Text.builder("Click to access the Store page").color(TextColors.GREEN).onHover(TextActions.showText(Text.of("Store url"))).onClick(TextActions.openUrl(new URL(uri))).build());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        if (tokenPlayerNameMap.hasUser(username)) {
            tokenPlayerNameMap.removeUser(username);
        }
        tokenPlayerNameMap.addUser(username, token);
        System.out.printf("Updated memory with new user %s, %s%n",username,token);
        /* Send post
        HttpURLConnection connection;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            //handle response
            int code = connection.getResponseCode();
            System.out.println("Response Code "+code);
            String responseLine;
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                while ((responseLine = reader.readLine()) != null) {
                    response.append(responseLine);
                }
                System.out.println("Response "+response);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
        return CommandResult.success();


    }

    public static CommandSpec build() {
        return CommandSpec.builder()
                .description(Text.of("Send gold packets"))
                .executor(new StoreLink())
                .build();
    }
}
