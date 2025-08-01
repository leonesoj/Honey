package io.github.leonesoj.honey.utils.inventory;

import io.github.leonesoj.honey.observer.Observer;
import io.github.leonesoj.honey.observer.ObserverService;
import java.util.Locale;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ReactiveInventory<T> extends SerializedInventory
    implements Observer<T> {

  private final ObserverService<T> observerService;

  public ReactiveInventory(JavaPlugin plugin, FileConfiguration config, Locale locale,
      ObserverService<T> observerService, SimpleInventory parent) {
    super(plugin, config, locale, parent);

    this.observerService = observerService;
    this.observerService.registerObserver(this);
  }

  public ReactiveInventory(JavaPlugin plugin, ConfigurationSection section, Locale locale,
      ObserverService<T> observerService, SimpleInventory parent) {
    super(plugin, section, locale, parent);

    this.observerService = observerService;
    this.observerService.registerObserver(this);
  }

  @Override
  protected void onFinalClose() {
    observerService.unregisterObserver(this);
  }

  protected ObserverService<T> getObserverService() {
    return observerService;
  }
}
