# Sword of the Morning (Fabric 1.20.1)

A Minecraft Fabric mod adding the legendary **Sword of the Morning** (*Dawn / Меч Зари*) inspired by Ser Arthur Dayne from *A Song of Ice and Fire*.

---

## ⚔️ Weapon Stats & Mechanics

- **Attack Damage:** 9.0 (Base 1.0 + Tier Bonus 4.0 + Modifier 4.0)
- **Attack Speed:** 1.6
- **Durability:** Unbreakable (cannot be depleted or damaged).
- **Rarity:** Epic, Fire-resistant.

### 🌟 Combat & Abilities

1. **Combat Speed Buff:** Striking an opponent grants the wielder **Speed II** for 2 seconds (40 ticks).
2. **Affliction:** 20% chance to afflict target with **Glowing** for 10 seconds.
3. **Striking Sparks:** Metallic electric spark particles, crit particles, and heavy anvil strike audio on impact.
4. **Right-Click Lunge (Выпад):** Dash forward with sweeping slash particles and audio (1.25 second cooldown).
5. **Soulbound / Retention:** Player death mixins ensure the sword is retained in the player's inventory upon death (`RetentionManager`).
6. **Ownership Lore:** Displays custom owner tag (`OwnerName`) and lore in tooltips.

---

## 🛠️ Requirements & Building

- **Minecraft:** 1.20.1
- **Fabric Loader & Fabric API**

```bash
git clone https://github.com/Lilsort/SwordOfTheMorning.git
cd SwordOfTheMorning
./gradlew build
```
