package io.github.leonesoj.honey.observer;

import java.util.ArrayList;
import java.util.List;

public class ObserverService<T> {

  private final List<Observer<T>> observers = new ArrayList<>();

  public void registerObserver(Observer<T> observer) {
    observers.add(observer);
  }

  public void unregisterObserver(Observer<T> observer) {
    observers.remove(observer);
  }

  public void publishEvent(T t, EventType eventType) {
    for (Observer<T> observer : observers) {
      if (eventType == EventType.CREATE) {
        observer.onCreate(t);
      } else if (observer.matches(t)) {
        switch (eventType) {
          case UPDATE -> observer.onUpdate(t);
          case DELETE -> observer.onDelete(t);
          default -> {
            return;
          }
        }
      }
    }
  }

}
