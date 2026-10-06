# Astralpack

Mod **Minecraft 1.20.1 / Forge 47.x** regroupant tous les ajouts personnalisés réalisés en interne
(mobs, items, blocs…), modèles et textures inclus.

## Contenu actuel
| Ajout | Type | État |
|-------|------|------|
| Fennec | Mob apprivoisable (poulet cru) | IA + apprivoisement OK, modèle/texture maison à intégrer |

## Structure
```
src/main/java/fr/astralnexus/astralpack/
├── Astralpack.java      point d'entrée
├── registry/            DeferredRegister (entités, items, onglet créatif, événements)
├── entity/custom/       mobs
├── item/  block/        items et blocs
├── client/              rendu (renderers, modèles)
├── config/  util/
src/main/resources/
├── META-INF/mods.toml, pack.mcmeta
├── assets/astralpack/   lang, models, textures, blockstates
└── data/astralpack/     loot tables, recipes, tags, biome modifiers
docs/                    guides internes
```

## Développement
Java 17 requis.
```
./gradlew genIntellijRuns   # ou genEclipseRuns
./gradlew runClient
./gradlew build
```
Voir [docs/ADDING_CONTENT.md](docs/ADDING_CONTENT.md) pour ajouter un mob, item ou bloc.

> Le wrapper Gradle (`gradlew`, `gradle/wrapper`) n'est pas encore versionné : à générer avec `gradle wrapper --gradle-version 8.1.1`.
> La texture `textures/entity/fennec.png` est à ajouter (le rendu provisoire utilise le modèle d'ocelot).
