package io.github.leonesoj.honey.observer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObserverService<T> {

  private final List<Observer<T>> observers = new CopyOnWriteArrayList<>();

  public void registerObserver(Observer<T> observer) {
    observers.add(observer);
  }

  public void unregisterObserver(Observer<T> observer) {
    observers.remove(observer);
  }

  public void publishCreate(T t) {
    for (Observer<T> observer : observers) {
      if (observer.matches(t)) {
        observer.onCreate(t);
      }
    }
  }

  public void publishUpdate(T t) {
    for (Observer<T> observer : observers) {
      if (observer.matches(t)) {
        observer.onUpdate(t);
      }
    }
  }

  public void publishDelete(UUID id) {
    for (Observer<T> observer : observers) {
      if (observer.matchesId(id)) {
        observer.onDelete(id);
      }
    }
  }

}
