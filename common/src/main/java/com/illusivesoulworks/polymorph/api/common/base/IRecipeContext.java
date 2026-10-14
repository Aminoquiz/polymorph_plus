package com.illusivesoulworks.polymorph.api.common.base;

import javax.annotation.Nullable;

public interface IRecipeContext {

  @Nullable
  Object polymorph$getContext();

  void polymorph$setContext(Object context);
}
