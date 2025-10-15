package hs.voidbuilder;

import org.bukkit.Material;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class VoidBiomeGenerator {
    private final SimplexOctaveGenerator heightGen;
    private final SimplexOctaveGenerator densityGen;

    public VoidBiomeGenerator(long seed) {
        // Height noise for terrain variation
        this.heightGen = new SimplexOctaveGenerator(seed, 8);
        this.heightGen.setScale(0.008);

        // Density noise for island generation
        this.densityGen = new SimplexOctaveGenerator(seed + 1000, 4);
        this.densityGen.setScale(0.02);
    }

    public double getHeight(int x, int z) {
        return heightGen.noise(x, z, 0.5, 0.5);
    }

    public double getDensity(int x, int z) {
        return densityGen.noise(x, z, 0.5, 0.5);
    }

    public Material getMaterial(int x, int y, int z, int terrainHeight) {
        int depthFromSurface = terrainHeight - y;

        if (depthFromSurface < 0) {
            return null; // Air
        } else if (depthFromSurface == 0) {
            return Material.GRASS_BLOCK; // Top layer
        } else if (depthFromSurface <= 3) {
            return Material.DIRT; // Dirt layer
        } else if (depthFromSurface <= 5) {
            return Material.STONE; // Stone layer
        } else {
            return Material.DEEPSLATE; // Deep stone
        }
    }
}