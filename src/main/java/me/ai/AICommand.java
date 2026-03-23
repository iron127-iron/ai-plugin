package me.ai;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.*;

import java.util.LinkedList;
import java.util.Queue;

public class AICommand implements CommandExecutor {

    private final Main plugin;
    private NPC npc;

    public AICommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) return true;

        String prompt = String.join(" ", args);

        if (npc == null) {
            npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "AI");
            npc.spawn(p.getLocation());
        }

        KimiClient client = new KimiClient(plugin);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            String res = client.ask(prompt);

            try {
                JSONObject json = new JSONObject(res);
                String content = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                JSONObject data = new JSONObject(content);
                Queue<JSONObject> queue = new LinkedList<>();

                for (Object obj : data.getJSONArray("actions")) {
                    queue.add((JSONObject) obj);
                }

                runActions(queue, p.getWorld());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }

    private void runActions(Queue<JSONObject> queue, World world) {

        new BukkitRunnable() {
            public void run() {

                if (queue.isEmpty()) {
                    cancel();
                    return;
                }

                JSONObject action = queue.poll();
                String type = action.getString("type");

                switch (type) {

                    case "move":
                        Location loc = new Location(
                                world,
                                action.getDouble("x"),
                                action.getDouble("y"),
                                action.getDouble("z")
                        );
                        npc.getNavigator().setTarget(loc);
                        break;

                    case "place":
                        Location place = new Location(
                                world,
                                action.getDouble("x"),
                                action.getDouble("y"),
                                action.getDouble("z")
                        );
                        place.getBlock().setType(Material.valueOf(
                                action.getString("block").toUpperCase()
                        ));
                        break;

                    case "mine":
                        Block b = npc.getEntity().getLocation().getBlock();
                        b.breakNaturally();
                        break;
                }
            }
        }.runTaskTimer(plugin, 0, 40);
    }
}
