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
├── client/              rendu GeckoLib (model/, renderer/)
├── config/  util/
src/main/resources/
├── META-INF/mods.toml, pack.mcmeta
├── assets/astralpack/   lang, geo, animations, textures, models, blockstates
└── data/astralpack/     loot tables, recipes, tags, biome modifiers
blockbench/              sources .bbmodel (non chargées par le jeu)
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

## Modèles 3D (Blockbench + GeckoLib)
Les modèles sont faits avec Blockbench au format **GeckoLib Animated Model** :
1. Sauvegarder la source dans `blockbench/entity/xxx.bbmodel`.
2. Exporter le modèle vers `assets/astralpack/geo/xxx.geo.json`.
3. Exporter les animations vers `assets/astralpack/animations/xxx.animation.json`
   (noms `animation.xxx.idle`, `.walk`, `.sit`).
4. Texture : `assets/astralpack/textures/entity/xxx.png`.

`geo/fennec.geo.json` et `animations/fennec.animation.json` sont des **placeholders** à remplacer par l'export Blockbench.

> Le wrapper Gradle (`gradlew`, `gradle/wrapper`) n'est pas encore versionné : à générer avec `gradle wrapper --gradle-version 8.1.1`.
> `textures/entity/fennec.png` est à ajouter.
