# Ajouter du contenu

## Nouveau mob (exemple : Fennec)
1. `entity/custom/XxxEntity.java` : la logique (IA, attributs, apprivoisement).
2. `registry/ModEntities.java` : enregistrer l'`EntityType`.
3. `registry/ModEvents.java` : enregistrer les attributs.
4. `client/renderer/XxxRenderer.java` + `client/ClientSetup.java` : rendu côté client.
5. `ModItems.java` : œuf d'apparition.
6. Ressources : `textures/entity/xxx.png`, `lang/*.json`, `loot_tables/entities/xxx.json`,
   `forge/biome_modifier/xxx_spawns.json` pour le spawn naturel.

## Nouvel item / bloc
- Code dans `item/` ou `block/`, enregistrement dans `ModItems` / `ModBlocks`.
- Modèles dans `assets/astralpack/models`, textures dans `textures`, traductions dans `lang`.
