package io.github.leonesoj.honey.observer;

public interface Observer<T> {

  default boolean matches(T t) {
    return true;
  }

  default void onUpdate(T t) {
  }

  default void onDelete(T t) {
  }

  default void onCreate(T t) {
  }
}
