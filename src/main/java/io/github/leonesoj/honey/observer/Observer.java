package io.github.leonesoj.honey.observer;

import java.util.UUID;

public interface Observer<T> {

  default boolean matches(T t) {
    return true;
  }

  default boolean matchesId(UUID uuid) {
    return true;
  }

  default void onUpdate(T t) {
  }

  default void onDelete(UUID uuid) {
  }

  default void onCreate(T t) {
  }
}
