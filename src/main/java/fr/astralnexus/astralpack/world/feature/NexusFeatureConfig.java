package fr.astralnexus.astralpack.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Réglages communs aux éléments de décor du Nexus : taille (1 normal, 2 grand) et distance minimale au centre du
 * monde, pour garder le milieu du hub libre et constructible.
 */
public record NexusFeatureConfig(int size, int minDistance) implements FeatureConfiguration {
    public static final Codec<NexusFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("size", 1).forGetter(NexusFeatureConfig::size),
            Codec.INT.optionalFieldOf("min_distance", 0).forGetter(NexusFeatureConfig::minDistance)
    ).apply(instance, NexusFeatureConfig::new));

    public boolean tooCloseToCenter(BlockPos pos) {
        long dx = pos.getX();
        long dz = pos.getZ();
        return dx * dx + dz * dz < (long) this.minDistance * this.minDistance;
    }
}
