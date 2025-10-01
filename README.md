![](https://minecraft.wiki/images/Honeycomb_JE2_BE2.png?2ecff&format=original)

# Honey - A streamlined backend for modern Minecraft servers.

![](https://img.shields.io/badge/Paper%20API-1.21.9-black?style=for-the-badge&labelColor=%23D3D3D3&color=%23ffa142) ![](https://img.shields.io/badge/PRs-welcome-black?style=for-the-badge&labelColor=%23D3D3D3&color=%23ffa142) ![](https://img.shields.io/badge/License-MIT-black?style=for-the-badge&labelColor=%23D3D3D3&color=%23ffa142)
![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg)![paper](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/paper_vector.svg)



<h2><img src="https://minecraft.wiki/images/Bee.gif?69ac5" alt="Bee" width="45" /> Why use Honey?</h2>

- Robust **backend foundation** for any type of Minecraft server instance, enhanced with quality-of-life features.
- Built on the latest **Paper API** to support the modern Minecraft server ecosystem fully.
- Optimized with **asynchronous** database and cache operations to minimize main-thread usage and maximize performance.
- Zero-setup, **plug-and-play** experience tailored for Paper servers.
- Offers **extensive plugin customization** to seamlessly match your server's brand and experience.
- Synchronizes _player profiles_ across your **Velocity** network through a **Redis** cache.

<h2><img src="https://minecraft.wiki/images/Beehive_%28S%29_JE1.png?a2997" alt="Hive item" width="45" /> Features</h2>

- **Essential Commands** - Honey comes with a lean(but extended functionality) set of essential commands that are perfect for every type of Minecraft server.
- **Player Settings** - Let your players customize their server preferences by turning on/off private messages, public chat, sound alerts, and also changing their own chat color.
- **Punishment System** - A complete suite for managing your server bans, mutes, warnings, and kicks with added history tracking.
- **Chat Control** - Filter out unwanted content, apply chat formats, track chat/private message logs, and enforce rules to keep your server community healthy.
- **Reporting System** - Give power to your players with an intuitive reporting GUI, packaged with 24 ready-to-use report reasons/descriptions, making it easy for staff to review and respond efficiently.
- **Player Messaging** - Private messages, social spy, ignore lists, sound alerts, and network-wide messaging support.
- **MiniMessage** format support, **translations**, **auto config updates**, **SQLite support**, **local/Redis** caching, and more!

<h2><img src="https://minecraft.wiki/images/Honey_Block_JE1_BE2.png?94b6b" alt="Honey block item" width="45" /> Requirements</h2>

- Java 21 JDK or newer
- A [Paper](https://papermc.io/downloads/paper) or [Velocity](https://papermc.io/downloads/velocity) server instance

### Optional
- MySQL or MongoDB database
- Redis Database(**required** when using Velocity)
  - [Upstash](https://upstash.com/) offers a very capable free plan, with affordable paid options if you need to scale.
- [Vault](https://www.spigotmc.org/resources/vault.34315/) for prefix/suffix placeholders in chat formatting.

<h2><img src="https://minecraft.wiki/images/Honeycomb_Block_JE1_BE1.png?ff510" alt="Beenest item" width="45" /> For Server Owners</h2>

Plugin releases are available in the [Modrnith resource page](https://modrinth.com/project/xQ3pgVap).\
Check out the project wiki [here](https://github.com/leonesoj/honey/wiki) on GitHub for more details.

<h2><img src="https://minecraft.wiki/images/Bee_Nest_Honey_%28S%29_JE1.png?b623e" alt="Beenest item" width="45" /> For Developers</h2>

### Compiling from source

```bash
git clone https://github.com/leonesoj/Honey.git
cd Honey/
./gradlew build
```

### Running a test server

```bash
./gradlew runServer
```

### 💛 Contributing

Honey follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) except for any Javadoc rule.
Additionally, you can run Checkstyle on your local project by running `./gradlew check`.

This project is committed to staying up-to-date with the latest Paper API, prioritizing its features over legacy Spigot implementations.