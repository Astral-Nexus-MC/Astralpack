# Cahier des charges — Créer une arme personnalisée

| | |
|---|---|
| **Version du document** | 1.0 |
| **Date** | 2026-10-08 |
| **Statut** | À valider |
| **Cible technique** | Minecraft Java 1.20.1 · Forge 47.x · GeckoLib 4.8.4 |
| **Outil de modélisation** | Blockbench 5.x + plugin GeckoLib |
| **Arme de référence** | Death Scythe → [FICHE_DEATH_SCYTHE.md](FICHE_DEATH_SCYTHE.md) |
| **Documents liés** | [CAHIER_DES_CHARGES_MOB.md](CAHIER_DES_CHARGES_MOB.md) · [FEUILLE_DE_ROUTE_RECETTE.md](FEUILLE_DE_ROUTE_RECETTE.md) · [RECETTE_DEATH_SCYTHE.md](RECETTE_DEATH_SCYTHE.md) |

Ce document est **général** : il décrit comment produire n'importe quelle arme du mod, de l'idée à la mise en production. Il reprend la méthode du [cahier des charges des mobs](CAHIER_DES_CHARGES_MOB.md) (conventions, Git, versionnage, release, portes de recette), qui reste la référence pour tout ce qui n'est pas propre aux armes. Ce qui est propre à une arme (statistiques, effets, recette, modèle) vit dans **sa fiche** (`docs/FICHE_<ARME>.md`) et **sa recette** (`docs/RECETTE_<ARME>.md`).

---

## 1. Objectifs

- Un processus unique, reproductible, de l'idée à la production.
- Des armes **équilibrées** par rapport aux armes du jeu de base.
- Un **rendu cohérent** (modèle 3D, tenue en main, inventaire, sons).
- Des **critères d'acceptation vérifiables** : une arme ne casse ni le jeu ni le serveur.

Hors périmètre : portage vers d'autres versions ou chargeurs, contenus annexes (biomes, structures).

---

## 2. Pile technique

Identique à celle des mobs (§3 du cahier des mobs) : Minecraft 1.20.1, Forge 47.x, Java 17, GeckoLib 4.8.4, Blockbench 5.x au format **GeckoLib Animated Model**. Toute montée de version impose de retester toutes les armes et tous les mobs.

---

## 3. Choix de conception structurants

### 3.1 Type d'objet
- L'arme étend **`SwordItem`** (ou `Item` avec les mêmes modificateurs) pour bénéficier des enchantements d'arme (Tranchant, Sournoiserie, Aura de feu, Pillage…), de la réparation à l'enclume et de la catégorie « arme ».
- Un **`Tier` dédié** (`ModTiers`) fixe durabilité, enchantabilité, matériau de réparation et bonus de dégâts.
- Les modificateurs d'attributs (dégâts, vitesse d'attaque, allonge) sont déclarés **à un seul endroit** (classe de l'arme / `ModTiers`), avec des `UUID` fixes.

### 3.2 Rendu 3D : objet GeckoLib
Le modèle vanilla (`elements` JSON) est limité à une boîte de 48 pixels (−16 à 32) : une arme plus grande, comme une faux, ne tient pas. Les armes à grand gabarit utilisent donc un **objet GeckoLib** :
- l'objet implémente `GeoItem` ; son rendu passe par un `GeoItemRenderer` déclaré dans `IClientItemExtensions` ;
- la géométrie (`geo/<arme>.geo.json`) et la texture sont produites depuis le `.bbmodel`, comme pour les mobs ;
- les transformations de présentation (main, première et troisième personne, inventaire, sol, cadre) sont réglées dans le modèle d'objet JSON (`display`) ; l'icône d'inventaire est une **icône dédiée** (modèle réduit ou icône 2D), jamais le modèle en taille réelle ;
- animation facultative (balancement au repos) ; le coup lui-même reste l'animation du jeu.

### 3.3 Règle « deux mains »
Le jeu de base n'a pas de notion d'arme à deux mains. La règle retenue :
- **pleins dégâts uniquement si la main secondaire est vide** ; sinon, les dégâts de l'arme sont fortement réduits et ses effets spéciaux sont désactivés ;
- la règle s'applique côté **serveur** (événement `LivingHurtEvent`, attaquant joueur, arme en main principale), jamais côté client seul ;
- le **survol de l'objet** (infobulle) indique « Deux mains : main secondaire vide » ;
- aucun blocage de la main secondaire : bouclier, torche ou nourriture restent utilisables, au prix du malus.

### 3.4 Effets spéciaux
Chaque effet (frappe en arc, effet de potion, vol de vie…) est :
- appliqué **côté serveur** ;
- conditionné par la règle des deux mains ;
- limité par une **durée et une intensité** fixées dans la fiche ;
- sans effet sur le porteur sauf mention contraire ;
- respectueux des immunités vanilla (le Wither, les morts-vivants au Wither, etc.).

### 3.5 Sons
- Sons du jeu de base par défaut (balayage, impact), choisis dans la fiche.
- Sons personnalisés : `.ogg` mono déclarés dans `sounds.json`, libres de droits, crédités dans `docs/CREDITS_SONS.md`.

---

## 4. Périmètre fonctionnel d'une arme

| # | Composant | Description |
|---|---|---|
| 1 | **Fiche de conception** | Concept, statistiques, effets, recette (§5) |
| 2 | **Modèle 3D** | Géométrie GeckoLib + texture (§6) |
| 3 | **Objet Java** | Classe, `Tier`, attributs, règle des deux mains, effets |
| 4 | **Rendu** | `GeoItem`, `GeoItemRenderer`, extensions client |
| 5 | **Recette** | Fabrication ; avancement de recette si utile |
| 6 | **Butin (opt.)** | Coffres, boss |
| 7 | **Sons** | Coup, effets |
| 8 | **Textes** | FR et EN (nom, infobulle) |
| 9 | **Onglet créatif** | Entrée dans l'onglet du mod |
| 10 | **Tests** | Tests automatisés + recette en jeu (§8) |

---

## 5. Phase 1 — Conception : la fiche

| Rubrique | Contenu attendu |
|---|---|
| Nom / identifiant | `snake_case`, ex. `death_scythe` |
| Concept et références | Images, proportions, silhouette |
| Mains | Une main, deux mains (règle du §3.3) |
| Dégâts | Pleins dégâts / dégâts avec malus ; comparaison avec l'arme vanilla la plus proche |
| Cadence d'attaque | Modificateur de vitesse, comparaison |
| Allonge | Modificateur d'allonge d'attaque |
| Durabilité, enchantabilité, réparation | |
| Effets spéciaux | Déclencheur, durée, intensité, conditions |
| Recette | Grille, ingrédients, résultat |
| Obtention | Craft seul, butin, boss |
| Modèle | Dimensions, os, texture, présentation en main |
| Sons | Liste, déclencheurs |
| Équilibrage | Position par rapport à l'épée en diamant et en netherite |

La fiche est **validée avant toute modélisation**. Chaque règle de la fiche devient au moins une ligne de la recette.

---

## 6. Phase 2 — Modélisation et texture

- Projet Blockbench au format GeckoLib, identifiant `<id_arme>` ; `.bbmodel` versionné dans `blockbench/item/<id_arme>.bbmodel` (source de vérité).
- Un os racine `weapon`, puis un os par pièce (lame, manche, anneaux, ornements) pour permettre une animation ultérieure.
- Chaque cube est **rattaché à un os** (`addTo(groupe)`).
- Taille de texture décidée dès le départ et **définitive** (128×128 recommandé pour une arme détaillée).
- Contrôles avant export : validation GeckoLib sans erreur, vues de face, de profil et de trois-quarts par captures, texture sans pixels parasites.
- **Aperçu en main** : dans le jeu, vérifier la tenue en première et troisième personne pour la main droite et la main gauche.

---

## 7. Phase 3 — Code Java

Classes attendues :

| Classe | Rôle |
|---|---|
| `<Arme>Item` | `SwordItem` + `GeoItem` ; infobulle ; effets au coup ; action de balayage (`ToolActions.SWORD_SWEEP`) si frappe en arc |
| `ModTiers` | `Tier` des armes du mod |
| `<Arme>Renderer` / `<Arme>Model` | Rendu GeckoLib (`GeoItemRenderer`, `GeoModel`) |
| `TwoHandedEvents` | `LivingHurtEvent` : malus quand la main secondaire est occupée |
| `ModItems`, `ModSounds` | Enregistrement |
| `ModEvents` / extensions client | Rendu, onglet créatif |

Règles :
- logique de jeu **côté serveur uniquement** ; le client ne fait que le rendu ;
- aucune dépendance à un mod tiers ;
- valeurs d'équilibrage regroupées en constantes ;
- recette et textes dans `data/astralpack/recipes/` et `assets/astralpack/lang/` ;
- `ResourcesConsistencyTest` étendu : modèle, texture, modèle d'objet, recette, clés de langue.

---

## 8. Plan de recette (générique)

Chaque arme a sa recette `docs/RECETTE_<ARME>.md`, déroulée selon la [feuille de route](FEUILLE_DE_ROUTE_RECETTE.md).

### 8.1 Obtention
- [ ] La recette est utilisable ; l'objet apparaît dans l'onglet créatif ; `/give` fonctionne.
- [ ] Noms et infobulles en FR et EN.

### 8.2 Rendu
- [ ] Inventaire, sol, cadre, main (1re et 3e personne, main droite et gauche) : taille, position et orientation corrigées.
- [ ] Aucun clignotement ni z-fighting ; pas de chute de FPS notable à plusieurs joueurs.

### 8.3 Combat
- [ ] Dégâts conformes à la fiche à pleine puissance et avec malus.
- [ ] Cadence et allonge conformes.
- [ ] Effets spéciaux : déclenchement, durée, intensité, conditions, immunités.
- [ ] Enchantements applicables et fonctionnels.
- [ ] Durabilité, réparation (enclume), enchantement à la table.

### 8.4 Règle des deux mains
- [ ] Main secondaire vide : pleins dégâts et effets.
- [ ] Bouclier, torche ou nourriture en main secondaire : dégâts réduits, effets désactivés, sans erreur.
- [ ] L'infobulle explique la règle.

### 8.5 Sons
- [ ] Coup, effets : audibles, volume cohérent.

### 8.6 Persistance et serveur
- [ ] Arme conservée (durabilité, enchantements, nom) après déconnexion et redémarrage.
- [ ] Solo **et** serveur dédié : règle et effets appliqués côté serveur, rendu côté client ; client sans le mod refusé.
- [ ] Pas de crash au chargement ni au déchargement de chunks.

---

## 9. Organisation, versionnage, livraison

Comme les mobs (§14 du cahier des mobs) : développement poussé sur **`dev`** sans branche annexe, merge dans `main` après validation, **incrément de `mod_version` à chaque merge**, release GitHub avec jar et `.sha256`, contrôle après déploiement. Nouveaux fichiers livrés : fiche, `.bbmodel`, exports, code et ressources, recette, crédits des sons, README.

---

## 10. Risques et points de vigilance

| Risque | Parade |
|---|---|
| Modèle trop grand pour le format vanilla | Objet GeckoLib (§3.2) |
| Arme mal placée en main | Réglage de `display` et test des deux mains en jeu |
| Icône d'inventaire illisible | Icône dédiée, jamais le modèle plein format |
| Arme trop puissante | Comparaison à l'épée en netherite, valeurs en constantes |
| Effets appliqués côté client | Tout côté serveur |
| Règle contournée (échange de mains en plein coup) | Règle évaluée à chaque coup, côté serveur |
| Enchantements ou balayage absents | `SwordItem` + action `SWORD_SWEEP` |
| Perte d'état au redémarrage | NBT standard de l'objet, recette de persistance |
| Incompatibilité avec un mod tiers | Test sur le serveur avec la liste de mods réelle |

---

## Annexe — Modèle de fiche d'une nouvelle arme

```
Nom :                         Identifiant :
Concept / références :
Mains (une / deux, règle) :
Dégâts (pleins / avec malus) :   Cadence :   Allonge :
Durabilité / enchantabilité / réparation :
Effets spéciaux (déclencheur, durée, intensité) :
Recette :
Obtention :
Modèle (dimensions, os, texture) :
Sons :
Équilibrage (comparaison) :
Remarques :
```
