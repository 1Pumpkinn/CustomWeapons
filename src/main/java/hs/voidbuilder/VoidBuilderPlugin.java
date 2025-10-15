package hs.voidbuilder;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class VoidBuilderPlugin extends JavaPlugin implements Listener {

    private static final int VOID_THRESHOLD = 0; // Below this Y level is "void"
    private static final int TERRAIN_START = -40; // Where void terrain starts

    @Override
    public void onEnable() {
        getLogger().info("VoidBuilder plugin enabled!");

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);

        // Start terrain generation task
        startVoidTerrainGeneration();

        getLogger().info("Void mechanics activated for all worlds!");
    }

    @Override
    public void onDisable() {
        getLogger().info("VoidBuilder plugin disabled!");
    }

    private void startVoidTerrainGeneration() {
        // Generate void terrain chunks as players move
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isInVoid(player.getLocation())) {
                    generateVoidTerrainNearPlayer(player);
                }
            }
        }, 20L, 20L); // Check every second
    }

    private void generateVoidTerrainNearPlayer(Player player) {
        Location loc = player.getLocation();
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;

        VoidBiomeGenerator biomeGen = new VoidBiomeGenerator(player.getWorld().getSeed());

        // Generate terrain in a 3x3 chunk area around player
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                int currentChunkX = chunkX + cx;
                int currentChunkZ = chunkZ + cz;

                // Generate void terrain for this chunk
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int worldX = currentChunkX * 16 + x;
                        int worldZ = currentChunkZ * 16 + z;

                        generateVoidColumn(player.getWorld(), biomeGen, worldX, worldZ);
                    }
                }
            }
        }
    }

    private void generateVoidColumn(org.bukkit.World world, VoidBiomeGenerator biomeGen, int x, int z) {
        double height = biomeGen.getHeight(x, z);
        double density = biomeGen.getDensity(x, z);

        // Only generate if density is high enough (creates floating islands)
        if (density > 0.3) {
            int terrainHeight = (int) (TERRAIN_START + height * 15);

            for (int y = terrainHeight - 5; y <= terrainHeight; y++) {
                if (y < VOID_THRESHOLD && y >= world.getMinHeight()) {
                    Location blockLoc = new Location(world, x, y, z);

                    // Only place blocks if they're air (don't overwrite existing blocks)
                    if (blockLoc.getBlock().getType() == Material.AIR) {
                        Material material = biomeGen.getMaterial(x, y, z, terrainHeight);
                        if (material != null) {
                            blockLoc.getBlock().setType(material, false);
                        }
                    }
                }
            }
        }
    }

    private boolean isInVoid(Location loc) {
        return loc.getY() < VOID_THRESHOLD;
    }

    // Increased falling speed in void
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (isInVoid(loc) && player.getVelocity().getY() < 0) {
            Vector velocity = player.getVelocity();
            velocity.setY(velocity.getY() * 1.5); // Fall 50% faster
            player.setVelocity(velocity);
        }
    }

    // No void damage in void area
    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (isInVoid(player.getLocation()) && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
            }
        }
    }

    // Allow block breaking in void
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (isInVoid(event.getBlock().getLocation())) {
            // Allow breaking in void (default behavior, just logging)
            // You can add special effects or drops here if desired
        }
    }

    // Allow block placing in void
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (isInVoid(event.getBlock().getLocation())) {
            // Allow placing in void (default behavior)
            // You can add special effects here if desired
        }
    }
}