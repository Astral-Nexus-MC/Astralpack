# Cahier des charges — Créer une arme ou un outil personnalisé

| | |
|---|---|
| **Version du document** | 2.0 |
| **Date** | 2026-10-08 |
| **Statut** | À valider |
| **Cible technique** | Minecraft Java 1.20.1 · Forge 47.x · GeckoLib 4.8.4 |
| **Outil de modélisation** | Blockbench 5.x (modèle 2D, vanilla 3D ou GeckoLib) |
| **Exemple de référence** | Death Scythe (arme à deux mains) → [FICHE_DEATH_SCYTHE.md](FICHE_DEATH_SCYTHE.md) |
| **Documents liés** | [CAHIER_DES_CHARGES_MOB.md](CAHIER_DES_CHARGES_MOB.md) · [FEUILLE_DE_ROUTE_RECETTE.md](FEUILLE_DE_ROUTE_RECETTE.md) · [RECETTE_DEATH_SCYTHE.md](RECETTE_DEATH_SCYTHE.md) |

Ce document est **général** : il décrit comment produire n'importe quel objet de combat ou de travail du mod, **arme** (épée, hache, faux, lance, marteau, arc…) ou **outil** (pioche, pelle, hache, houe, cisailles, outil multifonction…), de l'idée à la mise en production. Il reprend la méthode du [cahier des charges des mobs](CAHIER_DES_CHARGES_MOB.md) (conventions, Git, versionnage, release, portes de recette), qui reste la référence pour tout ce qui n'est pas propre aux objets. Ce qui est propre à un objet (statistiques, mécaniques, recette, modèle) vit dans **sa fiche** (`docs/FICHE_<OBJET>.md`) et **sa recette** (`docs/RECETTE_<OBJET>.md`).

---

## 1. Objectifs

- Un processus unique, reproductible, de l'idée à la production, pour tout type d'arme ou d'outil.
- Des objets **équilibrés** par rapport à leurs équivalents du jeu de base.
- Un **rendu cohérent** : modèle, tenue en main, inventaire, sons.
- Des **critères d'acceptation vérifiables** : un objet ne casse ni le jeu ni le serveur.

Hors périmètre : portage vers d'autres versions ou chargeurs, contenus annexes (biomes, structures), armures (voir les armures du Fennec pour un exemple d'équipement porté).

---

## 2. Pile technique

Identique à celle des mobs (§3 du cahier des mobs) : Minecraft 1.20.1, Forge 47.x, Java 17, GeckoLib 4.8.4, Blockbench 5.x. Toute montée de version impose de retester tous les objets et tous les mobs.

---

## 3. Choix de conception structurants

### 3.1 Famille d'objet et classe Java

| Famille | Exemples | Classe de base (1.20.1) |
|---|---|---|
| Arme de mêlée | épée, faux, lance, marteau | `SwordItem` (ou `TieredItem`) |
| Hache (arme + outil) | hache de guerre, hachette | `AxeItem` |
| Outil de minage | pioche, pelle, marteau de mineur | `PickaxeItem`, `ShovelItem`, ou `DiggerItem` avec un tag de blocs |
| Outil agricole | houe, faucille | `HoeItem` |
| Arme à distance | arc, arbalète, lance-pierres | `BowItem`, `CrossbowItem`, ou `ProjectileWeaponItem` |
| Outil spécial | cisailles, brosse, outil multifonction | `Item` + `ToolActions` |

Règles communes :
- Un **`Tier` dédié** (`ModTiers`) fixe durabilité, vitesse de minage, bonus de dégâts, niveau de minage, enchantabilité et matériau de réparation. Les outils d'un même matériau partagent ce `Tier`.
- Les modificateurs d'attributs (dégâts, vitesse d'attaque, allonge) sont déclarés **à un seul endroit**, avec des `UUID` fixes.
- Le niveau de minage passe par un **tag de blocs** (`data/astralpack/tags/blocks/needs_<matériau>_tool.json`) et les blocs minables par les tags `mineable/pickaxe|axe|shovel|hoe` (ou un tag propre au mod pour un `DiggerItem`).
- Les **actions** de l'objet (balayage, décapage d'une bûche, labourage, tonte…) sont déclarées via `ToolActions`.
- Enchantements : un objet de catégorie arme ou outil reçoit les enchantements correspondants (Tranchant, Efficacité, Solidité, Raccommodage, Fortune…) ; un objet exotique précise sa catégorie dans la fiche.

### 3.2 Rendu : trois niveaux

| Niveau | Quand | Technique |
|---|---|---|
| **2D classique** | objet de gabarit vanilla | texture 16×16 ou 32×32 + modèle `item/handheld` |
| **3D vanilla** | objet plus détaillé tenant dans 48 px | modèle JSON à `elements` (−16 à 32) + `display` |
| **3D GeckoLib** | grand gabarit (faux, lance, hallebarde, marteau) ou animation | `GeoItem` + `GeoItemRenderer` via `IClientItemExtensions` |

Règles :
- Le modèle vanilla est limité à une boîte de 48 px : au-delà, GeckoLib.
- Les transformations (main, 1re et 3e personne, inventaire, sol, cadre, bouclier) sont réglées dans le `display` du modèle d'objet ; l'icône d'inventaire est **dédiée** (modèle réduit ou icône 2D), jamais le modèle plein format.
- Une animation éventuelle (balancement au repos, rotation, lueur) reste facultative ; le coup lui-même reste l'animation du jeu.
- Source de vérité : le `.bbmodel` (`blockbench/item/<id>.bbmodel`) ; les exports en sont dérivés.

### 3.3 Mécaniques propres à l'objet

Une mécanique spéciale est décrite comme un **module** (classe d'événements ou méthode de l'objet), avec :
- un **déclencheur** (coup, clic droit, minage, fin d'utilisation) ;
- une **condition** (main secondaire vide, accroupi, cible précise, charge) ;
- une **durée, une portée et une intensité** chiffrées dans la fiche ;
- une exécution **côté serveur uniquement** ; le client ne fait que le rendu et les sons locaux ;
- le respect des immunités du jeu de base et des règles du serveur (`mobGriefing`, protections, `LivingHurtEvent`, `BlockEvent.BreakEvent`).

Catalogue de mécaniques déjà décrites ou prévisibles :

| Mécanique | Exemple | Point d'entrée |
|---|---|---|
| **Deux mains** | faux, marteau | `LivingHurtEvent` : pleins dégâts seulement si la main secondaire est vide ; sinon malus fort et effets désactivés ; infobulle explicite ; aucun blocage de la main |
| **Frappe en arc** | faux, glaive | `ToolActions.SWORD_SWEEP` |
| **Effet de potion au coup** | Wither, poison, lenteur | `hurtEnemy` |
| **Vol de vie** | arme maudite | `LivingHurtEvent` |
| **Minage en zone** | marteau de mineur, pelle large | `BlockEvent.BreakEvent` (3×3, durabilité par bloc) |
| **Veine entière** | hache de bûcheron | idem, plafonnée en nombre de blocs |
| **Fonte automatique** | pioche de forge | modificateur de loot global |
| **Charge** | arc, lance lancée | `releaseUsing` + `getUseDuration` |
| **Outil multifonction** | pioche + pelle | tag de blocs combiné + `ToolActions` multiples |

Toute nouvelle mécanique est ajoutée à ce catalogue après sa première utilisation.

### 3.4 Sons
- Sons du jeu de base par défaut, choisis dans la fiche (balayage, impact, minage, cassure).
- Sons personnalisés : `.ogg` mono déclarés dans `sounds.json`, libres de droits, crédités dans `docs/CREDITS_SONS.md`.

---

## 4. Périmètre fonctionnel d'un objet

| # | Composant | Description |
|---|---|---|
| 1 | **Fiche de conception** | Concept, statistiques, mécaniques, recette (§5) |
| 2 | **Modèle** | 2D, 3D vanilla ou GeckoLib + texture (§6) |
| 3 | **Objet Java** | Classe, `Tier`, attributs, mécaniques |
| 4 | **Rendu** | Modèle d'objet, `display`, `GeoItemRenderer` si GeckoLib |
| 5 | **Recette** | Fabrication ; avancement de recette si utile ; tags de réparation |
| 6 | **Butin (opt.)** | Coffres, boss, échange avec un villageois |
| 7 | **Sons** | Coup, minage, effets |
| 8 | **Textes** | FR et EN (nom, infobulle) |
| 9 | **Onglet créatif** | Entrée dans l'onglet du mod |
| 10 | **Tags (opt.)** | Blocs minables, niveau de minage, objets réparables |
| 11 | **Tests** | Tests automatisés + recette en jeu (§8) |

---

## 5. Phase 1 — Conception : la fiche

| Rubrique | Contenu attendu |
|---|---|
| Nom / identifiant | `snake_case`, ex. `death_scythe`, `ember_pickaxe` |
| Famille | Arme de mêlée, hache, outil de minage, outil agricole, arme à distance, spécial (§3.1) |
| Concept et références | Images, proportions, silhouette |
| Mains | Une main, deux mains (§3.3) |
| Armes : dégâts, cadence, allonge | Comparaison avec l'arme vanilla la plus proche |
| Outils : vitesse de minage, niveau, blocs concernés, zone | Comparaison avec l'outil vanilla le plus proche |
| Durabilité, enchantabilité, réparation | |
| Mécaniques spéciales | Déclencheur, condition, durée, portée, intensité (§3.3) |
| Recette | Grille, ingrédients, résultat |
| Obtention | Craft seul, butin, boss |
| Modèle | Niveau de rendu (§3.2), dimensions, os, texture, présentation en main |
| Sons | Liste, déclencheurs |
| Équilibrage | Position par rapport aux équivalents en diamant et en netherite |

La fiche est **validée avant toute modélisation**. Chaque règle de la fiche devient au moins une ligne de la recette.

---

## 6. Phase 2 — Modélisation et texture

**2D classique.** Texture pixel-art au format du jeu (16×16 ou 32×32), modèle `item/handheld`, icône identique en inventaire et en main.

**3D vanilla.** Éléments dans Blockbench au format Java Block/Item ; contrôle des 48 px ; `display` réglé pour chaque contexte.

**3D GeckoLib.**
- Projet au format GeckoLib, identifiant `<id>` ; `.bbmodel` versionné dans `blockbench/item/`.
- Un os racine `weapon` (ou `tool`), puis un os par pièce (lame, manche, anneaux, ornements) pour permettre une animation ultérieure.
- Chaque cube est **rattaché à un os** (`addTo(groupe)`).
- Taille de texture décidée dès le départ et **définitive** (128×128 recommandé pour un objet détaillé).
- Contrôles avant export : validation GeckoLib sans erreur, vues de face, de profil et de trois-quarts par captures, texture sans pixels parasites.

Dans tous les cas, **vérifier la tenue en main en jeu** (1re et 3e personne, main droite et gauche, inventaire, sol, cadre).

---

## 7. Phase 3 — Code Java

Classes attendues (à adapter selon la famille) :

| Classe | Rôle |
|---|---|
| `<Objet>Item` | Classe de base de la famille (§3.1) ; infobulle ; mécaniques de l'objet ; `GeoItem` si GeckoLib |
| `ModTiers` | `Tier` des objets du mod |
| `<Objet>Renderer` / `<Objet>Model` | Rendu GeckoLib (`GeoItemRenderer`, `GeoModel`), si besoin |
| `<Mécanique>Events` | Événements Forge d'une mécanique transversale (`TwoHandedEvents`, `AreaMiningEvents`…) |
| `ModItems`, `ModSounds` | Enregistrement |
| `ModEvents` / extensions client | Rendu, onglet créatif |

Règles :
- logique de jeu **côté serveur uniquement** ;
- aucune dépendance à un mod tiers ;
- valeurs d'équilibrage regroupées en constantes ;
- recette, tags et textes dans `data/astralpack/` et `assets/astralpack/lang/` ;
- `ResourcesConsistencyTest` étendu : modèle, texture, modèle d'objet, recette, tags, clés de langue.

---

## 8. Plan de recette (générique)

Chaque objet a sa recette `docs/RECETTE_<OBJET>.md`, déroulée selon la [feuille de route](FEUILLE_DE_ROUTE_RECETTE.md).

### 8.1 Obtention
- [ ] La recette est utilisable ; l'objet apparaît dans l'onglet créatif ; `/give` fonctionne.
- [ ] Noms et infobulles en FR et EN.

### 8.2 Rendu
- [ ] Inventaire, sol, cadre, main (1re et 3e personne, main droite et gauche) : taille, position et orientation corrigées.
- [ ] Aucun clignotement ni z-fighting ; pas de chute de FPS notable à plusieurs joueurs.

### 8.3 Armes — combat
- [ ] Dégâts, cadence et allonge conformes à la fiche.
- [ ] Mécaniques spéciales : déclenchement, durée, intensité, conditions, immunités.
- [ ] Enchantements d'arme applicables et fonctionnels.

### 8.4 Outils — travail
- [ ] Vitesse de minage et niveau conformes sur les blocs prévus ; les autres blocs ne sont pas minés plus vite que prévu.
- [ ] Blocs qui exigent un niveau supérieur : aucun butin.
- [ ] Actions (décapage, labourage, tonte…) fonctionnelles.
- [ ] Mécanique de zone ou de veine : plafond respecté, durabilité consommée, aucun bloc protégé ou à entité-bloc touché sans condition, `mobGriefing` et protections respectés.
- [ ] Enchantements d'outil applicables (Efficacité, Fortune, Toucher de soie).

### 8.5 Durabilité et réparation
- [ ] Durabilité, réparation à l'enclume, enchantement à la table, Raccommodage.

### 8.6 Mécaniques conditionnelles (deux mains, charge…)
- [ ] Condition remplie : pleins effets.
- [ ] Condition non remplie : malus ou effets désactivés, sans erreur.
- [ ] La règle est évaluée à chaque utilisation, côté serveur, contre les mobs comme contre les joueurs.

### 8.7 Sons
- [ ] Coup, minage, effets : audibles, volume cohérent.

### 8.8 Persistance et serveur
- [ ] Durabilité, enchantements et nom conservés après déconnexion et redémarrage.
- [ ] Solo **et** serveur dédié : logique côté serveur, rendu côté client ; client sans le mod refusé.
- [ ] Pas de crash au chargement ni au déchargement de chunks.

---

## 9. Organisation, versionnage, livraison

Comme les mobs (§14 du cahier des mobs) : développement poussé sur **`dev`** sans branche annexe, merge dans `main` après validation, **incrément de `mod_version` à chaque merge**, release GitHub avec jar et `.sha256`, contrôle après déploiement. Livrables : fiche, modèle source, exports, code et ressources, recette, crédits des sons, README.

---

## 10. Risques et points de vigilance

| Risque | Parade |
|---|---|
| Modèle trop grand pour le format vanilla | GeckoLib (§3.2) |
| Objet mal placé en main | Réglage de `display` et test des deux mains en jeu |
| Icône d'inventaire illisible | Icône dédiée, jamais le modèle plein format |
| Objet trop puissant ou trop rapide | Comparaison avec le diamant et le netherite, valeurs en constantes |
| Outil qui mine tout trop vite ou ne mine pas | Tags `mineable/*` et `needs_*_tool` vérifiés bloc par bloc |
| Minage en zone qui casse des blocs protégés | `BlockEvent.BreakEvent`, plafond de blocs, durabilité par bloc |
| Mécanique appliquée côté client | Tout côté serveur |
| Règle contournée (échange de mains en plein coup) | Évaluation à chaque utilisation, côté serveur |
| Enchantements ou balayage absents | Bonne classe de base et `ToolActions` |
| Perte d'état au redémarrage | NBT standard de l'objet, recette de persistance |
| Incompatibilité avec un mod tiers | Test sur le serveur avec la liste de mods réelle |

---

## Annexe — Modèle de fiche d'un nouvel objet

```
Nom :                         Identifiant :
Famille (arme mêlée / hache / outil minage / agricole / distance / spécial) :
Concept / références :
Niveau de rendu (2D / 3D vanilla / GeckoLib) :
Mains (une / deux, règle) :
Armes : dégâts (pleins / malus) :  Cadence :  Allonge :
Outils : vitesse / niveau de minage / blocs / zone :
Durabilité / enchantabilité / réparation :
Mécaniques spéciales (déclencheur, condition, durée, portée, intensité) :
Recette :
Obtention :
Modèle (dimensions, os, texture) :
Sons :
Équilibrage (comparaison) :
Remarques :
```
