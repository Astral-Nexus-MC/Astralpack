# Astralpack

Mod **Minecraft 1.20.1 / Forge 47.x** regroupant tous les ajouts personnalisés réalisés en interne
(mobs, items, blocs…), modèles et textures inclus.

## Contenu actuel
| Ajout | Type | État |
|-------|------|------|
| Fennec | Mob apprivoisable (poulet cru) | **v1 validée en production** (0.2.0) : modèle, animations, sons, armure, pelage 3D, ordres, rythme jour/nuit, terrier, objets en gueule, apparition, bébé, serveur dédié |

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
docs/                    guides internes (ADDING_CONTENT, cahier des charges mob, fiche et recette du Fennec, feuille de route de mise en production, crédits sons)
```

## Développement
Java 17 requis. GeckoLib **4.8.4**.
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

Source du Fennec : `blockbench/entity/fennec.bbmodel` (export dans `geo/`, `animations/` et `textures/entity/`).


## Publier une version
1. Passer `mod_version` à la nouvelle version dans `gradle.properties`, committer, pousser sur `main`.
2. `.github/workflows/release.yml` voit le `gradle.properties` poussé, crée tout seul le tag `vX.Y.Z`
   (identique à `mod_version`), construit le jar et publie `astralpack-X.Y.Z.jar` + `.sha256` en release GitHub.
   Un tag poussé à la main donne le même résultat ; Actions > Release > Run workflow rattrape une version.
   Les sessions Claude ne peuvent pas pousser de tag (403) : ne pas essayer.
