# Cahier des charges — Créer un mob Minecraft personnalisé

| | |
|---|---|
| **Version du document** | 2.0 |
| **Date** | 2026-10-06 |
| **Statut** | Validé |
| **Cible technique** | Minecraft Java 1.20.1 · Forge 47.x · GeckoLib 4.8.4 |
| **Outil de modélisation** | Blockbench 5.x + plugin GeckoLib |
| **Mob de référence** | Fennec → [FICHE_FENNEC.md](FICHE_FENNEC.md) |
| **Documents liés** | [FEUILLE_DE_ROUTE_RECETTE.md](FEUILLE_DE_ROUTE_RECETTE.md) · [RECETTE_FENNEC.md](RECETTE_FENNEC.md) · [ADDING_CONTENT.md](ADDING_CONTENT.md) |

Ce document est **général** : il décrit comment produire n'importe quel mob du mod, de l'idée à la mise en production. Tout ce qui est propre à une créature (statistiques, animations, comportements, recette) vit dans la **fiche de ce mob** (`docs/FICHE_<MOB>.md`) et dans sa **recette** (`docs/RECETTE_<MOB>.md`).

---

## 1. Contexte et objectifs

### 1.1 Contexte
Le mod **Astralpack** ajoute des créatures originales, absentes du jeu de base. Chaque mob suit le même processus, reproductible par toute personne de l'équipe.

### 1.2 Objectifs
- Un **processus unique** de bout en bout.
- Un **niveau de qualité homogène** : modèle, animations, comportement, sons, équilibrage.
- Des **conventions** (nommage, arborescence, versions) qui évitent les conflits.
- Des **critères d'acceptation vérifiables** avant toute mise en production : un mob ne casse pas le serveur.

### 1.3 Hors périmètre
- Le modpack lui-même (launcher, liste de mods tiers, configuration générale).
- Le portage vers d'autres versions de Minecraft ou d'autres chargeurs (Fabric, NeoForge).
- Les biomes, structures et dimensions (sauf nécessité décrite dans la fiche du mob).

---

## 2. Principe d'architecture

Tous les mobs vivent dans **un seul mod Forge**, dépôt `Astral-Nexus-MC/Astralpack` (package `fr.astralnexus.astralpack`, modid `astralpack`). Le serveur et les joueurs consomment le **`.jar` publié en release GitHub** ; ni le code ni les sources Blockbench ne sont embarqués dans le modpack.

```
Astralpack/
├── blockbench/entity/<id_mob>.bbmodel   ← source de vérité (modèle, texture, animations)
├── docs/                                ← ce cahier, fiches, recettes, feuille de route
├── src/main/java/…                      ← code (voir §5.2)
├── src/main/resources/…                 ← assets et données
├── src/test/java/…                      ← tests automatisés (cohérence des ressources)
├── gradle.properties                    ← mod_version (déclenche la release, voir §14)
└── .github/workflows/                   ← build.yml (CI), release.yml (publication)
```

---

## 3. Pile technique et versions

| Élément | Version / détail |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x (figée dans `build.gradle`) |
| Java | 17 |
| GeckoLib | 4.8.4 (`software.bernie.geckolib:geckolib-forge-1.20.1:4.8.4`) |
| Blockbench | 5.x, format **GeckoLib Animated Model** |
| Exports | `*.geo.json`, `*.animation.json`, `*.png` |

> Toute montée de version de GeckoLib ou de Forge est une **décision d'équipe** : elle impose de retester tous les mobs.

---

## 4. Périmètre fonctionnel d'un mob

Les éléments marqués **(opt.)** dépendent de la fiche du mob.

| # | Composant | Description |
|---|---|---|
| 1 | **Fiche de conception** | Rôle, comportement, apparence, statistiques (§6) |
| 2 | **Modèle 3D** | Géométrie GeckoLib, hiérarchie d'os imposée (§7) |
| 3 | **Texture** | Un seul PNG par défaut |
| 4 | **Animations** | Jeu minimal imposé + animations spécifiques (§8) |
| 5 | **Entité** | Classe Java, attributs, objectifs d'IA, données synchronisées (§9) |
| 6 | **Rendu** | Modèle GeckoLib, renderer, couches de rendu (opt.) |
| 7 | **Apparition** | Modificateurs de biome, règles de placement, œuf d'apparition |
| 8 | **Butin** | Table de butin (ou absence documentée) |
| 9 | **Sons** | Ambiant, dégâts, mort + sons spécifiques (§10) |
| 10 | **Objets liés (opt.)** | Armure, nourriture, accessoires |
| 11 | **Blocs liés (opt.)** | Abri, bloc d'interaction : état de bloc, forme, loot table, tag d'outil |
| 12 | **Comportements spécifiques (opt.)** | Rythme jour/nuit, ordres, ramassage, construction… |
| 13 | **Recettes (opt.)** | Fabrication des objets et blocs liés |
| 14 | **Textes** | Langues FR et EN |
| 15 | **Tests** | Tests automatisés + recette en jeu (§12) |

---

## 5. Conventions et arborescence

### 5.1 Nommage
- **modid** : `astralpack`. **Identifiant du mob** : `snake_case` minuscule, ex. `fennec`.
- **Géométrie** : `geometry.<id_mob>`. **Animations** : `animation.<id_mob>.<nom>`.
- **Objets** : `<id_mob>_<matériau>_<pièce>` ; **blocs** : `<id_mob>_<bloc>` (ex. `fennec_burrow`).
- **Os d'équipement** : `armor_<pièce>_<matériau>` ; **os de rendu** : `fur_*`, `mouth_item`…
- **Clés de langue** : `entity.astralpack.<id_mob>`, `item.astralpack.<id>`, `block.astralpack.<id>`.
- **Commits** : en français, une phrase claire qui décrit le changement.

### 5.2 Arborescence du mod

```
src/main/java/fr/astralnexus/astralpack/
├── entity/custom/   ← entités et leurs buts d'IA
├── block/           ← blocs liés
├── item/            ← objets liés
├── client/          ← modèles GeckoLib, renderers, couches
├── registry/        ← ModEntities, ModItems, ModBlocks, ModSounds, ModEvents, onglets
└── Astralpack.java
src/main/resources/
├── assets/astralpack/   geo/ animations/ textures/{entity,item,block}/ models/{item,block}/
│                        blockstates/ sounds/ sounds.json lang/{en_us,fr_fr}.json
└── data/astralpack/     loot_tables/{entities,blocks}/ recipes/ forge/biome_modifier/ tags/
```

> Le `.bbmodel` est la **source de vérité**. Les `.json` et `.png` exportés en sont dérivés : ne jamais les modifier à la main sans reporter la modification dans le `.bbmodel`.

---

## 6. Phase 1 — Conception

### 6.1 Fiche de conception (une par mob, `docs/FICHE_<MOB>.md`)

| Rubrique | Contenu attendu |
|---|---|
| Nom / identifiant | |
| Concept et références visuelles | Photos, croquis, proportions |
| Taille | Hitbox (largeur × hauteur) |
| Régime et comportement | Passif, neutre, hostile ; proie / prédateur ; rythme jour/nuit |
| Apprivoisable | Oui / non ; aliment ; probabilité ; ordres |
| Statistiques | Vie (sauvage / apprivoisé), dégâts, vitesse, portée de suivi |
| Biomes d'apparition | Liste, poids, taille des groupes |
| Butin | Objets et probabilités |
| Interactions | Clic droit, Maj + clic, objets spéciaux |
| Animations | Liste, boucle, déclencheur |
| Sons | Liste, déclencheurs |
| Objets et blocs liés | Armure, nourriture, abri… |
| Comportements spécifiques | Chaque règle, ses conditions, ses cas limites |

### 6.2 Livrable
Fiche validée **avant** toute modélisation. Chaque règle de comportement de la fiche devient au moins une ligne de la recette (§12).

---

## 7. Phase 2 — Modélisation et texture (Blockbench)

### 7.1 Projet
- Format **GeckoLib Animated Model** ; identifiant de modèle `<id_mob>`.
- **Taille de texture** : décidée dès le début et **définitive** dès qu'un export est livré (la changer décale les UV). Une texture haute résolution (×2, ×4) est possible : l'espace UV déclaré dans le `.geo.json` reste celui du modèle ; l'avertissement du validateur GeckoLib sur l'écart de taille est alors **volontaire** et documenté dans la fiche.
- Prévoir dès le départ la place pour l'équipement, les variantes et les couches de rendu (pelage…).

### 7.2 Hiérarchie d'os imposée (quadrupède)

| Os | Rôle |
|---|---|
| `body` | Racine ; l'animer déplace toute la créature |
| `head` | Conteneur de la tête |
| `leg_front_left`, `leg_front_right`, `leg_back_left`, `leg_back_right` | Pattes |

Recommandés : `torso`, `head_base`, `ear_left`, `ear_right`, `tail`. Ajouts selon la fiche : `jaw`, `mouth_item` (point d'accroche d'un objet tenu), os d'équipement, os de pelage.

Règles :
- Chaque cube est **rattaché à un os** (`addTo(groupe)` dans Blockbench) : un cube à la racine ne suit aucune animation.
- Pivots placés à l'articulation naturelle ; pattes au même niveau que `body` pour ne pas hériter de ses rotations.
- Pas de recouvrement de faces provoquant du z-fighting ; les couches superposées (pelage, armure) utilisent un `inflate` croissant.

### 7.3 Texture
- Mode **Box UV**, un seul PNG.
- Équipement et couches de rendu **intégrés au même PNG** dans des zones dédiées ; masqués par le code quand ils ne sont pas actifs.

### 7.4 Contrôles avant export
- Validation GeckoLib sans erreur (avertissements documentés).
- Os obligatoires présents, orthographe exacte.
- Modèle vérifié sous 3 angles (face, profil, trois-quarts) par captures d'écran.
- Texture sans pixels parasites dans les zones UV utilisées.
- `.bbmodel` enregistré **avant** de pousser (il doit refléter le modèle exporté).

---

## 8. Phase 3 — Animations

### 8.1 Jeu minimal imposé

| Animation | Boucle | Rôle |
|---|---|---|
| `idle` | `loop` | Au repos |
| `walk` | `loop` | Déplacement |
| `sit` | `hold_on_last_frame` | Assis (animal apprivoisé) |

### 8.2 Animations spécifiques
Selon la fiche (ex. se secouer, attaquer, mordre, dormir, creuser). Chaque animation a un **déclencheur unique** et une boucle explicite.

### 8.3 Règles
- Noms `animation.<id_mob>.<nom>`.
- Une animation en boucle revient à son image de départ sans à-coup.
- Les keyframes sont des **écarts** appliqués à la pose de repos, pas des positions absolues.
- Une pose extrême (sur le dos, couché, accroupi) est vérifiée **numériquement** : aucun cube ne s'enfonce dans le sol.
- Les signes de rotation Blockbench/GeckoLib sont à confirmer par un test visuel dès la première animation.

---

## 9. Phase 4 — Code Java

### 9.1 Classes attendues

| Classe | Rôle |
|---|---|
| `<Mob>Entity` | Étend `TamableAnimal` ou `Animal`, implémente `GeoEntity` |
| `ModEntities` | Enregistrement du type (taille, catégorie, portée de suivi) |
| `<Mob>Model` | `GeoModel` : chemins, masquage des os d'équipement, suivi du regard |
| `<Mob>Renderer` | `GeoEntityRenderer` + couches (`BlockAndItemGeoLayer` pour un objet tenu) |
| `ModEvents` | Attributs, règles de placement, onglets créatifs |
| `ModItems`, `ModBlocks`, `ModSounds` | Objets, blocs, sons liés |

### 9.2 Contrôleurs d'animation
- **`movement`** : états continus (`idle`, `walk`, `sit`, `sleep`, `attack`…), avec un ordre de priorité explicite.
- **`action`** : animations ponctuelles déclenchées par événement (`triggerableAnim`).
- Tout état utile au client passe par `SynchedEntityData`.

### 9.3 Comportement
- IA par *goals*, avec priorités et `Flag` (MOVE/LOOK/JUMP) explicites pour éviter les conflits.
- Apprivoisement : aliment, probabilité, vie à l'apprivoisement.
- Interactions réservées au propriétaire ; tout état d'ordre et d'équipement est **sauvegardé en NBT** et rechargé sans perte.
- Les comportements qui modifient le monde (creuser, poser un bloc, ramasser) **respectent `mobGriefing`** et ne touchent ni fluides ni blocs avec entité-bloc.
- Un comportement échoue **proprement** : abandon après N tentatives ou N ticks, jamais de boucle infinie ni de chargement de chunks forcé.
- Tout code qui peut tourner côté client uniquement ou serveur uniquement est séparé (`level().isClientSide`).

### 9.4 Données
- **Apparition** : modificateurs de biome (`forge/biome_modifier`) + `SpawnPlacements`.
- **Œuf d'apparition** : couleurs cohérentes avec le mob.
- **Loot table** : sauvage et apprivoisé (ou absence assumée).
- **Langues** : `en_us.json` et `fr_fr.json` complets.
- **Blocs liés** : blockstate, modèle, loot table, tag d'outil, recette.

### 9.5 Tests automatisés
`ResourcesConsistencyTest` (src/test) vérifie que chaque ressource référencée existe (modèles, textures, sons, langues, recettes). Tout nouveau mob ou bloc **étend ce test** si ses ressources ne sont pas déjà couvertes.

---

## 10. Phase 5 — Sons

- Minimum : ambiant, dégâts, mort. Spécifiques : équipement, pas, actions.
- Sons vanilla acceptés pour un prototype ; sons personnalisés en **`.ogg` mono**, déclarés dans `sounds.json`.
- Volume et hauteur relatifs au jeu de base ; sons d'équipement différenciés par matériau et pièce.
- Tout son personnalisé est **libre de droits** ; source et licence consignées dans `docs/CREDITS_SONS.md`.

---

## 11. Phase 6 — Équilibrage

| Paramètre | Règle |
|---|---|
| Vie, dégâts, vitesse | Comparés à un mob vanilla de référence (loup, renard) ; référence notée dans la fiche |
| Apparition | Poids et taille de groupe cohérents avec les autres mobs du biome |
| Armure | Somme des pièces comparable à une armure vanilla de même matériau |
| Modificateurs temporaires (jour/nuit…) | Appliqués par modificateur d'attribut à UUID fixe, retirés proprement |

Les valeurs sont regroupées à un seul endroit du code (constantes, énumérations).

---

## 12. Plan de recette (générique)

Chaque mob dispose de sa recette `docs/RECETTE_<MOB>.md`, construite à partir de cette trame. L'exécution est encadrée par la [feuille de route](FEUILLE_DE_ROUTE_RECETTE.md).

### 12.1 Modèle et rendu
- [ ] Aucun message d'erreur à la console au chargement.
- [ ] Texture correcte, pas d'inversion gauche/droite ; hitbox et ombre cohérentes.
- [ ] Couches de rendu (pelage, équipement) sans z-fighting ni clipping, de près et de loin.

### 12.2 Animations
- [ ] `idle`, `walk`, `sit` dans les bons états ; transitions sans saut.
- [ ] Chaque animation spécifique se déclenche à son événement et s'arrête correctement.
- [ ] Aucune partie du modèle ne traverse le sol.

### 12.3 Comportement
- [ ] Apparition dans les biomes prévus uniquement.
- [ ] Apprivoisement : aliment et probabilité conformes ; ordres conformes.
- [ ] Attaque et cibles conformes ; pas d'attaque sur le propriétaire.
- [ ] Chaque règle de la fiche (rythme, abri, ramassage…) vérifiée, **cas limites inclus** (`mobGriefing`, dimension sans cycle, eau/lave, joueur accroupi…).
- [ ] Bébé : taille et hitbox cohérentes.

### 12.4 Équipement, objets, blocs
- [ ] Chaque pièce s'équipe, se retire, se récupère à la mort ; bonus cumulés.
- [ ] Recettes utilisables ; noms FR et EN ; icônes d'inventaire.
- [ ] Blocs : pose, orientation, casse à l'outil prévu, récupération.

### 12.5 Sons
- [ ] Ambiant, dégâts, mort, équipement (par matériau), pas.

### 12.6 Persistance
- [ ] Déconnexion/reconnexion et **redémarrage serveur** : propriétaire, ordre, équipement, objet tenu, blocs posés conservés.

### 12.7 Performance, compatibilité, serveur
- [ ] Pas de baisse notable de FPS avec 20 mobs visibles.
- [ ] Pas de conflit avec les mods du serveur.
- [ ] Fonctionne en solo **et** sur serveur dédié (rendu client, logique serveur) ; aucun crash au chargement ni au déchargement de chunks.

---

## 13. Critères d'acceptation (« terminé »)

1. Fiche de conception validée et à jour.
2. `.bbmodel` versionné ; validation GeckoLib sans erreur.
3. Build CI vert (`./gradlew build`, tests inclus).
4. Recette du mob entièrement cochée, ou chaque exception documentée.
5. Textes FR et EN complets ; crédits des sons à jour.
6. Test **sur serveur dédié** réalisé sur la version candidate.
7. Version incrémentée, release publiée et vérifiée (§14).

---

## 14. Organisation, versionnage, livraison

### 14.1 Git
- **Pas de branche de fonctionnalité** : tout le développement est poussé sur **`dev`**.
- **`dev` → `main`** uniquement une fois la recette validée par le responsable.
- Commits en français, un sujet par commit.

### 14.2 Versionnage
- Chaque merge dans `main` **incrémente `mod_version`** dans `gradle.properties` (ex. 0.1.3 → 0.1.4). Sans changement de version, le workflow de release ne se déclenche pas.
- Le même commit de version est ramené sur `dev` (fast-forward) pour que les deux branches restent alignées.

### 14.3 Publication
Un push sur `main` qui change `mod_version` lance `release.yml` : build, tag `vX.Y.Z`, release GitHub avec le jar et son `.sha256`. Le serveur se synchronise ensuite sur cette release. **Toute release est donc en production** : la recette passe avant le merge (voir la feuille de route).

### 14.4 Livrables par mob
Fiche, `.bbmodel`, exports (geo, animations, texture), code et ressources, recette remplie, mise à jour de `README.md`, crédits des sons.

---

## 15. Risques et points de vigilance

| Risque | Parade |
|---|---|
| Animation sans effet | Cubes rattachés à un os ; noms d'os identiques modèle/animation |
| Écart de rotation Blockbench/jeu | Test en jeu dès la première animation |
| Texture décalée | Fixer la taille avant export ; vérifier en jeu après tout changement |
| Mob invisible / erreur de chargement | Chemins `assets/astralpack/…`, identifiants, test de cohérence |
| Comportement qui modifie le monde | `mobGriefing`, fluides, blocs-entités, abandon propre |
| État perdu au redémarrage | NBT complet + recette de persistance |
| Mob trop gourmand en FPS | Test à 20 mobs ; limiter les couches de rendu |
| Conflit avec un mod tiers | Test sur le serveur avec la liste de mods réelle |
| Release cassée en production | Feuille de route, tests sur la version candidate, procédure de retour arrière |
| Dérive de version GeckoLib/Forge | Versions figées (§3) |

---

## Annexe A — Mob de référence
Le **Fennec** illustre l'ensemble du processus : [FICHE_FENNEC.md](FICHE_FENNEC.md) (conception, statistiques, os, animations, comportements) et [RECETTE_FENNEC.md](RECETTE_FENNEC.md) (recette).

## Annexe B — Enseignements de production
1. **Rattacher chaque cube à son os**, sinon il ne suit pas les animations.
2. **Les keyframes sont des écarts** à la pose de repos.
3. **Mesurer les poses extrêmes** pour éviter l'enfoncement dans le sol.
4. **Décider de la taille de texture dès le début** ; prévoir équipement et couches.
5. **Une texture unique + os masquables** permettent de mélanger les équipements.
6. **Deux contrôleurs** (états / actions ponctuelles) évitent les conflits de lecture.
7. **Un comportement « creuser/construire » doit être testé en jeu tôt** : un premier jet qui marchait sur le papier (rampe de 3 blocs) n'a jamais fonctionné ; une version simple (2 blocs + bloc posé) l'a remplacée.
8. **Les maquettes avant de coder** (ex. forme du terrier) évitent des allers-retours.
9. **Vérifier le rendu de profil** pour tout objet tenu ou accessoire : un sprite plat peut traverser le modèle.
10. **Garder les captures d'écran** de production comme preuve de recette.

## Annexe C — Modèle de fiche d'une nouvelle créature

```
Nom :                         Identifiant :
Concept / références :
Hitbox (L × H) :
Comportement (passif / neutre / hostile), rythme jour/nuit :
Apprivoisable (aliment, probabilité, ordres) :
Vie sauvage / apprivoisé :    Dégâts / vitesse :
Biomes d'apparition (poids, groupes) :
Butin :
Animations (nom, boucle, déclencheur) :
Sons :
Objets et blocs liés :
Comportements spécifiques (règle, conditions, cas limites) :
Remarques :
```
