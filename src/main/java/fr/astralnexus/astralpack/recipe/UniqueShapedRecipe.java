package fr.astralnexus.astralpack.recipe;

import com.google.gson.JsonObject;
import fr.astralnexus.astralpack.registry.ModRecipeSerializers;
import fr.astralnexus.astralpack.world.NexusData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

/**
 * Recette à forme fixe qui ne fonctionne qu'une fois par serveur : tant qu'une faux a été fabriquée (donnée du monde),
 * la grille ne donne plus rien. Côté client la recette est toujours acceptée : seul le serveur tranche.
 */
public class UniqueShapedRecipe extends ShapedRecipe {
    public UniqueShapedRecipe(ShapedRecipe base) {
        super(base.getId(), base.getGroup(), base.category(), base.getWidth(), base.getHeight(),
                base.getIngredients(), base.getResultItem(RegistryAccess.EMPTY), base.showNotification());
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        }
        return level.isClientSide || level.getServer() == null || !NexusData.get(level.getServer()).isScytheCrafted();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.UNIQUE_SHAPED.get();
    }

    public static class Serializer implements RecipeSerializer<UniqueShapedRecipe> {
        @Override
        public UniqueShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new UniqueShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json));
        }

        @Override
        public UniqueShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new UniqueShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, UniqueShapedRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
