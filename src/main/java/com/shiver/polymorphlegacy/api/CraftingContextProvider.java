package com.shiver.polymorphlegacy.api;

import javax.annotation.Nullable;

/** 容器直接实现或通过 Mixin 实现，向模组提供合成上下文。 */
public interface CraftingContextProvider {
    @Nullable
    CraftingContext polymorph$getCraftingContext();
}
