package me.ai;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class KimiClient {

    private final JavaPlugin plugin;

    public KimiClient(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String ask(String prompt) {
        try {
            String apiKey = plugin.getConfig().getString("api-key");
            String model = plugin.getConfig().getString("model");
            String system = plugin.getConfig().getString("system-prompt");

            URL url = new URL("https://api.moonshot.ai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = "{\"model\":\"" + model + "\",\"messages\":[{\"role\":\"system\",\"content\":\"" 
                + system.replace("\n"," ") + "\"},{\"role\":\"user\",\"content\":\"" + prompt + "\"}]}";

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
