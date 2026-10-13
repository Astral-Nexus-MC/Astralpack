package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.recipe.UniqueShapedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Astralpack.MOD_ID);

    public static final RegistryObject<RecipeSerializer<UniqueShapedRecipe>> UNIQUE_SHAPED =
            SERIALIZERS.register("unique_shaped", UniqueShapedRecipe.Serializer::new);
}
