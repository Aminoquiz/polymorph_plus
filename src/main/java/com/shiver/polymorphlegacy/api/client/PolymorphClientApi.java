package com.shiver.polymorphlegacy.api.client;

import com.shiver.polymorphlegacy.client.ClientRecipes;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

/** 配方查看器及自定义界面的客户端入口。 */
public final class PolymorphClientApi {
    private PolymorphClientApi() {
    }

    /** 为当前打开的容器请求选择配方。 */
    public static void selectRecipe(Container container, ResourceLocation recipeId) {
        ClientRecipes.selectFromJei(container, recipeId);
    }
}
