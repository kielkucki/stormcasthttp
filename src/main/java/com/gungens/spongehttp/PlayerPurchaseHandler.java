package com.gungens.spongehttp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerPurchaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handlePostRequest(exchange);
        handleGetRequest(exchange);
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            InputStream requestBody = exchange.getRequestBody();
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            String json;
            while ((json = reader.readLine()) != null) {
                sb.append(json);
            }
            reader.close();
            String data = sb.toString();
            JsonObject object = null;
            try {
                object = new JsonParser().parse(data).getAsJsonObject();
            } catch (JsonParseException exception) {
                System.out.println(exception.getMessage());
            }
            if (object.has("username") && object.has("token")) {
                String username = object.get("username").getAsString();
                String token = object.get("token").getAsString();
                if (StoreLink.tokenPlayerNameMap.validateTokenAgainstUsername(username, token)) {
                    sendResponse(exchange, "validated", 200);
                } else {
                    sendResponse(exchange, "not_valid", 401);
                }
            } else if (object.has("token") && object.has("item_name") && object.has("price") && object.has("quantity")) {
                String token = object.get("token").getAsString();
                String item_name = object.get("item_name").getAsString();
                int price = object.get("price").getAsInt();
                int quantity = object.get("quantity").getAsInt();

                if (StoreLink.tokenPlayerNameMap.hasToken(token)) {
                    String target = StoreLink.tokenPlayerNameMap.getUserByToken(token);
                    ItemStack stack = ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, item_name).orElse(ItemTypes.AIR)).quantity(quantity).build();

                    Player p = Sponge.getServer().getPlayer(target).get();
                    if (compareBalanceToItemCost(countGoldIngots(p),price)) {
                        p.getInventory().offer(stack);
                        removeGoldIngots(p,price);

                    } else {
                        p.sendMessage(Text.of(TextColors.RED,"You do not have enough gold to buy this"));
                    }
                }
            } else {
                Sponge.getServer().getBroadcastChannel().send(Text.of("Could not locate object "));
            }
            sendResponse(exchange, "transaction_code", 200);
        } else {
            sendResponse(exchange, "unauthorized", 401);
        }


    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            System.out.println("Get request sent " + exchange.getRemoteAddress());
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write("{\"redirect\":\"true\"}".getBytes());
            os.close();
        }
    }

    public void sendResponse(HttpExchange exchange, String message, int code) throws IOException {
        String jsonMessage = "{\"" + message + "\": " + code + "}";
        byte[] responseBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
    private int countGoldIngots(Player player) {
        AtomicInteger count = new AtomicInteger();
        Inventory inventory = player.getInventory();
        for (Inventory slot : inventory.slots()) {
            slot.peek().ifPresent(itemStack -> {
                if (itemStack.getType().equals(ItemTypes.GOLD_INGOT)) {
                    count.addAndGet(itemStack.getQuantity());
                }
            });
        }
        return count.get();
    }
    private void removeGoldIngots(Player player, int price) {
        Inventory inventory = player.getInventory();
        int goldIngots = countGoldIngots(player);
        for (Inventory slot : inventory.slots()) {
            slot.peek().ifPresent(itemStack -> {
                if (itemStack.getType().equals(ItemTypes.GOLD_INGOT)) {

                    slot.clear();
                }
            });
        }
        addItemsToInventory(player, ItemTypes.GOLD_INGOT, goldIngots-price);
    }
    private void addItemsToInventory(Player player, ItemType itemType, int quantity) {
        Inventory inventory = player.getInventory();
        ItemStack itemStack = ItemStack.of(itemType, quantity);

        inventory.offer(itemStack);
    }
    private boolean compareBalanceToItemCost(int gold, int price) {
        return gold >= price;
    }
}
