# Cahier des charges — Production d'un mob Minecraft personnalisé

| | |
|---|---|
| **Version du document** | 1.0 |
| **Date** | 2026-10-06 |
| **Statut** | Brouillon à valider |
| **Cible technique** | Minecraft Java 1.20.1 · Forge · GeckoLib 4.2.4 |
| **Outil de modélisation** | Blockbench 5.x + plugin GeckoLib |
| **Mob de référence** | Fennec (voir [annexe A](#annexe-a--mob-de-référence-le-fennec)) |

---

## 1. Contexte et objectifs

### 1.1 Contexte
Le modpack inclura des créatures originales, absentes du jeu de base. Ce document décrit **comment produire un mob de bout en bout**, de l'idée à son intégration, de façon reproductible. Il sert de gabarit pour le premier mob (le Fennec) et pour les suivants.

### 1.2 Objectifs
- Définir un **processus unique** que toute personne de l'équipe peut suivre.
- Garantir un **niveau de qualité homogène** (modèle, animations, comportement, sons, équilibrage).
- Fixer les **conventions** (nommage, arborescence, versions) pour éviter les conflits.
- Fournir des **critères d'acceptation** vérifiables avant d'intégrer un mob au modpack.

### 1.3 Hors périmètre
- Création du modpack lui-même (launcher, liste de mods tiers, configuration générale).
- Portage vers d'autres versions de Minecraft ou d'autres chargeurs de mods (Fabric, NeoForge).
- Contenus liés au mob au-delà de ce qui est listé en section 4 (nouveaux biomes, structures, dimensions).

---

## 2. Principe d'architecture

Le mob est développé dans un **mod Forge dédié** (projet séparé ou sous-dossier du dépôt). Le modpack **consomme le `.jar` produit** : il ne contient ni le code source ni les sources Blockbench du mob.

```
depot-modpack/
├── docs/
│   └── CAHIER_DES_CHARGES_MOB.md      ← ce document
├── modpack/                            ← définition du modpack (liste des mods, configs, scripts)
└── mods-custom/
    └── <nom-du-mod>/                   ← mod Forge contenant les mobs (voir §5)
```

Si le dépôt du modpack ne doit contenir que de la configuration, placer `mods-custom/` dans un dépôt séparé et ne versionner dans le modpack que le `.jar` ou sa référence.

---

## 3. Pile technique et versions

| Élément | Version / détail |
|---|---|
| Minecraft | 1.20.1 |
| Forge | Version 47.x correspondant à 1.20.1 (à figer dans `build.gradle`) |
| Java | 17 |
| GeckoLib | 4.2.4 (`software.bernie.geckolib:geckolib-forge-1.20.1:4.2.4`) |
| Blockbench | 5.x avec plugin GeckoLib, format de projet **GeckoLib Animated Model** |
| Formats d'export | `*.geo.json`, `*.animation.json`, `*.png` |

> Toute montée de version de GeckoLib ou de Forge est une **décision d'équipe** : elle impose de retester tous les mobs.

---

## 4. Périmètre fonctionnel d'un mob

Chaque mob livré comprend les composants suivants. Les éléments marqués **(opt.)** sont optionnels selon la fiche du mob.

| # | Composant | Description |
|---|---|---|
| 1 | **Fiche de conception** | Rôle, comportement, apparence, statistiques (voir §6) |
| 2 | **Modèle 3D** | Géométrie GeckoLib avec la hiérarchie d'os imposée |
| 3 | **Texture** | Un seul fichier PNG par défaut |
| 4 | **Animations** | Jeu minimal imposé + animations spécifiques |
| 5 | **Entité** | Classe Java, attributs, objectifs d'IA (goals) |
| 6 | **Rendu** | Modèle GeckoLib + renderer |
| 7 | **Apparition** | Apparition naturelle, œuf d'apparition |
| 8 | **Butin** | Table de butin (loot table) |
| 9 | **Sons** | Cris ambiant, dégâts, mort + sons spécifiques |
| 10 | **Objets liés (opt.)** | Armure, nourriture, accessoires |
| 11 | **Recettes (opt.)** | Fabrication des objets liés |
| 12 | **Textes** | Fichiers de langue FR et EN |
| 13 | **Tests** | Plan de recette (voir §9) |

---

## 5. Conventions et arborescence

### 5.1 Nommage
- **Identifiant du mod (modid)** : minuscules, sans espace, ex. `astralpack`.
- **Identifiant du mob** : minuscules, `snake_case`, ex. `fennec`.
- **Identifiant de géométrie** : `geometry.<id_mob>`.
- **Animations** : `animation.<id_mob>.<nom>` ; noms en minuscules, `snake_case`.
- **Objets** : `<id_mob>_<matériau>_<pièce>`, ex. `fennec_iron_helmet`.
- **Clés de langue** : `entity.astralpack.<id_mob>`, `item.astralpack.<id_objet>`.
- **Commits** : messages en français ou en anglais (à trancher), préfixés par type : `feat:`, `fix:`, `assets:`, `docs:`.

### 5.2 Arborescence du mod

```
src/main/
├── java/<package>/
│   ├── entity/        ← classes d'entités, enregistrement (ModEntities)
│   ├── client/        ← modèles GeckoLib, renderers
│   ├── item/          ← objets liés aux mobs
│   └── ModEvents.java ← attributs, renderers, onglets créatifs
└── resources/
    ├── assets/astralpack/
    │   ├── geo/                 ← *.geo.json
    │   ├── animations/          ← *.animation.json
    │   ├── textures/entity/     ← textures des mobs
    │   ├── textures/item/       ← icônes d'objets
    │   ├── models/item/         ← modèles d'objets
    │   ├── sounds/              ← fichiers .ogg (si sons personnalisés)
    │   ├── sounds.json
    │   └── lang/                ← en_us.json, fr_fr.json
    └── data/astralpack/
        ├── loot_tables/entities/
        ├── recipes/
        ├── forge/biome_modifier/
        └── tags/
sources/
└── blockbench/<id_mob>.bbmodel  ← fichier source, versionné
```

> Le fichier `.bbmodel` est la **source de vérité** du modèle, de la texture et des animations. Les `.json` et `.png` exportés en sont dérivés : ne jamais les modifier à la main.

---

## 6. Phase 1 — Conception

### 6.1 Fiche de conception (à remplir pour chaque mob)

| Rubrique | Contenu attendu |
|---|---|
| Nom / identifiant | |
| Concept et références visuelles | Photos, croquis, proportions |
| Taille | Hitbox (largeur × hauteur en blocs) |
| Régime et comportement | Passif, neutre, hostile ; proie / prédateur |
| Apprivoisable | Oui / non ; aliment ; probabilité |
| Statistiques | Vie (sauvage / apprivoisé), dégâts, vitesse, portée de suivi |
| Biomes d'apparition | Liste, fréquence, taille des groupes |
| Butin | Objets et probabilités |
| Interactions | Clic droit, Maj + clic, objets spéciaux |
| Animations | Liste, déclencheurs, boucles |
| Sons | Liste, déclencheurs |
| Objets liés | Armure, nourriture, etc. |

### 6.2 Livrable
Fiche validée avant toute modélisation.

---

## 7. Phase 2 — Modélisation et texture (Blockbench)

### 7.1 Projet
- Format : **GeckoLib Animated Model**.
- Identifiant de modèle : `<id_mob>` (donne `geometry.<id_mob>`).
- Taille de texture : **64×64 minimum** ; augmenter (64×128, 128×128) si des variantes ou de l'armure sont intégrées. Cette taille est **définitive** dès qu'un export est livré : la changer décale les UV.

### 7.2 Hiérarchie d'os imposée

Les os suivants sont **obligatoires** pour un quadrupède :

| Os | Rôle |
|---|---|
| `body` | Os racine ; animer `body` déplace toute la créature |
| `head` | Conteneur de la tête |
| `leg_front_left`, `leg_front_right`, `leg_back_left`, `leg_back_right` | Pattes |

Os recommandés : `torso` (cube du corps), `head_base` (cubes de la tête), `ear_left`, `ear_right`, `tail`.

Règles :
- Chaque cube est **rattaché à un os** (groupe). Un cube resté à la racine ne suit aucune animation.
- Les pivots sont placés à l'articulation naturelle (épaule, hanche, base de l'oreille, base de la queue).
- Les pattes sont de préférence au même niveau que `body` afin de ne pas hériter de ses rotations.
- Pas de cubes qui se recouvrent au point de provoquer du z-fighting.

### 7.3 Texture
- Mode **Box UV**, un seul fichier PNG.
- Respecter la palette de la fiche de conception.
- Les pièces d'équipement (armure) sont **intégrées au même PNG** dans des zones UV dédiées, une zone par matériau, et masquées en jeu par le code lorsqu'elles ne sont pas équipées.

### 7.4 Contrôles avant export
- Aucune erreur dans la validation GeckoLib.
- Tous les os obligatoires présents, orthographe exacte.
- Modèle vérifié sous au moins 3 angles (face, profil, trois-quarts) à l'aide de captures d'écran.
- Texture sans pixels parasites dans les zones d'UV utilisées.

---

## 8. Phase 3 — Animations

### 8.1 Jeu minimal imposé

| Animation | Boucle | Rôle |
|---|---|---|
| `idle` | `loop` | Au repos : respiration, queue, oreilles |
| `walk` | `loop` | Déplacement |
| `sit` | `hold_on_last_frame` | Assis (animal apprivoisé) |

### 8.2 Animations spécifiques (selon la fiche)
Exemples du mob de référence : `shake` (se secouer, `once`), `attack` (posture d'attaque, `hold_on_last_frame`), `belly_scratch` (gratouille du ventre, `loop`).

### 8.3 Règles
- Les noms suivent `animation.<id_mob>.<nom>`.
- Une animation **boucle sans à-coup** : première et dernière image identiques.
- Les valeurs de rotation et de position sont des **écarts** appliqués à la pose de repos de l'os, et non des positions absolues. Une rotation de repos de −12° sur une oreille ne doit pas être répétée dans les keyframes.
- Une animation qui retourne le corps (par exemple sur le dos) doit contrôler la hauteur des os pour ne pas **s'enfoncer dans le sol**.
- Vérifier chaque pose sur au moins deux images clés et sur la boucle complète.
- Le sens des rotations de Blockbench est inversé à l'export. **Valider systématiquement en jeu** : la pose affichée dans Blockbench fait foi, mais seul un test en jeu confirme.

---

## 9. Phase 4 — Code Java

### 9.1 Classes attendues

| Classe | Rôle |
|---|---|
| `<Mob>Entity` | Entité (étend `TamableAnimal` ou `Animal` selon le cas, implémente `GeoEntity`) |
| `ModEntities` | Enregistrement du type d'entité (taille, catégorie, portée de suivi) |
| `<Mob>Model` | `GeoModel` : chemins du geo, de la texture et des animations ; masquage des os d'équipement |
| `<Mob>Renderer` | `GeoEntityRenderer` |
| `ModEvents` | Attributs, enregistrement du renderer, onglets créatifs |
| `ModItems`, objets liés | Si la fiche en prévoit |

### 9.2 Contrôleurs d'animation
- Un contrôleur **`movement`** : choisit `idle`, `walk`, `sit`, `attack` selon l'état.
- Un contrôleur **`action`** : joue les animations ponctuelles déclenchées par des événements (`shake`, `bite`, etc.).
- Les états qui doivent être connus du client passent par des données synchronisées (`SynchedEntityData`).

### 9.3 Comportement
- IA décrite par des *goals* (flottaison, assise, suivi du propriétaire, attaque, errance, regard).
- Apprivoisement : aliment défini dans la fiche, probabilité, changement de vie à l'apprivoisement.
- Interactions réservées au propriétaire (assise, soins, équipement).
- Sauvegarde complète en NBT (propriétaire, état, équipement) et rechargement sans perte.

### 9.4 Données
- **Apparition** : modificateur de biome (`forge/biome_modifier`) et règles de placement.
- **Œuf d'apparition** : couleurs primaire et secondaire cohérentes avec le mob.
- **Loot table** : butin du mob sauvage et apprivoisé.
- **Langue** : `en_us.json` et `fr_fr.json` complets.

---

## 10. Phase 5 — Sons

- Sons minimum : ambiant, dégâts, mort.
- Sons spécifiques : équipement, pas, actions (voir fiche).
- Sons vanilla acceptés pour le prototype ; sons personnalisés au format **`.ogg` mono**, déclarés dans `sounds.json`.
- Volume et hauteur relatifs aux sons du jeu de base : un son du mob ne doit pas couvrir ceux de l'environnement.
- Les sons d'équipement et d'impact varient selon le **matériau** et la **pièce**.
- Tout son personnalisé doit être **libre de droits** ou créé par l'équipe ; la source et la licence sont consignées dans le dépôt.

---

## 11. Phase 6 — Équilibrage

| Paramètre | Cible indicative (à valider par mob) |
|---|---|
| Vie sauvage / apprivoisé | Petit mob : 10 / 20 |
| Dégâts | Cohérents avec le mob vanilla le plus proche |
| Vitesse | Comparée au loup et au renard |
| Taux d'apparition | Rareté cohérente avec les autres mobs du biome |
| Armure | Somme des pièces comparable à une armure vanilla de même matériau |

Les valeurs sont regroupées à un seul endroit du code (attributs, énumérations) pour faciliter les ajustements.

---

## 12. Plan de recette

### 12.1 Modèle et rendu
- [ ] Le mob s'affiche sans erreur dans la console au chargement du monde.
- [ ] Texture correcte sur toutes les faces, pas d'inversion gauche/droite.
- [ ] Hitbox cohérente avec le modèle (pas de mob « enfoncé » ni flottant).
- [ ] L'ombre est cohérente avec la taille.

### 12.2 Animations
- [ ] `idle`, `walk`, `sit` se déclenchent dans les bons états.
- [ ] Les transitions entre animations sont sans saut visible.
- [ ] Chaque animation spécifique se déclenche à l'événement prévu et s'arrête correctement.
- [ ] Aucune partie du modèle ne traverse le sol ni ne se détache pendant les animations.

### 12.3 Comportement
- [ ] Apparition naturelle dans les biomes prévus ; pas ailleurs.
- [ ] Apprivoisement avec l'aliment prévu et probabilité conforme.
- [ ] Suivi, assise, ordres : conformes à la fiche.
- [ ] Attaque et cibles conformes ; pas d'attaque sur le propriétaire ni sur ses animaux.
- [ ] Sauvegarde, déconnexion, rechargement : l'état est conservé (propriétaire, équipement, assise).

### 12.4 Équipement et objets
- [ ] Chaque pièce s'équipe, se retire et se récupère à la mort.
- [ ] Les bonus de défense sont appliqués et se cumulent par pièce.
- [ ] Les recettes sont utilisables et les noms affichés en FR et EN.
- [ ] Les icônes et modèles d'objets s'affichent dans l'inventaire.

### 12.5 Sons
- [ ] Ambiant, dégâts, mort audibles et d'un volume correct.
- [ ] Sons d'équipement différents selon le matériau et la pièce.

### 12.6 Performance et compatibilité
- [ ] Pas de baisse notable de FPS avec 20 mobs visibles.
- [ ] Pas de conflit avec les mods tiers du modpack (liste à tenir à jour).
- [ ] Le mob fonctionne en solo **et** sur un serveur dédié (rendu côté client, logique côté serveur).

---

## 13. Critères d'acceptation (définition de « terminé »)

Un mob est considéré terminé lorsque :
1. La fiche de conception est validée et à jour.
2. Le `.bbmodel` source est versionné et la validation GeckoLib ne remonte aucune erreur.
3. Tous les points de la section 12 sont cochés, ou chaque exception est documentée.
4. Le projet se compile sans avertissement bloquant, et le `.jar` se charge dans le modpack sans erreur.
5. Les textes FR et EN sont complets.
6. Un test sur serveur dédié a été réalisé.
7. Le journal des modifications (§14) est à jour.

---

## 14. Organisation, versionnage, livrables

### 14.1 Branches Git (proposition)
- `main` : version stable, intégrable au modpack.
- `feature/<id_mob>` : développement d'un mob.
- Fusion par demande de fusion (*pull request*) avec relecture et recette de la section 12.

### 14.2 Livrables par mob
- Fiche de conception.
- `.bbmodel` source.
- Exports `geo`, `animation`, texture.
- Code Java et ressources.
- Plan de recette rempli.
- Entrée dans le journal des modifications (`CHANGELOG.md`).

### 14.3 Planning type (à adapter)

| Phase | Contenu |
|---|---|
| 1 | Conception et validation de la fiche |
| 2 | Modèle et texture |
| 3 | Animations |
| 4 | Code, données, œuf, loot |
| 5 | Sons |
| 6 | Équilibrage et recette |
| 7 | Intégration au modpack |

---

## 15. Risques et points de vigilance

| Risque | Parade |
|---|---|
| Animations qui n'agissent pas sur le modèle | Vérifier que chaque cube est rattaché à un os ; noms d'os identiques dans le modèle et les animations |
| Écarts de rotation entre Blockbench et le jeu | Tester en jeu dès la première animation |
| Texture décalée après changement de taille | Fixer la taille avant tout export ; ne plus la modifier ensuite |
| Mob invisible ou erreur de chargement | Vérifier les chemins `assets/astralpack/...` et l'identifiant de modèle |
| Conflit avec un mod tiers | Tenir la liste des mods du modpack et tester à chaque ajout |
| Code non testé en jeu | Aucune intégration au modpack sans recette complète (§12) |
| Dérive de version GeckoLib / Forge | Versions figées et documentées (§3) |

---

## Annexe A — Mob de référence : le Fennec

### A.1 Concept
Petit renard du désert, **pelage jaune sable**, très grandes oreilles, queue touffue à bout sombre. Carnivore : chasse poules et lapins. Apprivoisable.

### A.2 Statistiques retenues
- Vie : 10 (sauvage), 20 (apprivoisé).
- Dégâts : 3. Vitesse : 0,32. Portée de suivi : 16 blocs.
- Hitbox : 0,6 × 0,8 bloc.
- Apprivoisement : poulet cru ou lapin cru, 1 chance sur 3.

### A.3 Hiérarchie d'os
```
body
├─ torso
├─ head
│  ├─ head_base      (tête, museau, nez)
│  ├─ ear_left
│  └─ ear_right
└─ tail
leg_front_left · leg_front_right · leg_back_left · leg_back_right
```
Les pièces d'armure sont des os supplémentaires (`armor_<pièce>_<matériau>`), enfants des os ci-dessus.

### A.4 Animations

| Animation | Boucle | Durée | Déclencheur |
|---|---|---|---|
| `idle` | `loop` | 4 s | À l'arrêt |
| `walk` | `loop` | 1 s | En déplacement |
| `sit` | `hold_on_last_frame` | 0,5 s | Ordre d'assise |
| `shake` | `once` | 1,2 s | Sortie de l'eau ou de la pluie |
| `attack` | `hold_on_last_frame` | 0,4 s | Agressif à l'arrêt, au contact de la cible |
| `belly_scratch` | `loop` | 1,6 s | Propriétaire : Maj + clic main vide |

### A.5 Équipement (armure en trois pièces)

| Matériau | Casque | Plastron | Protège-pattes | Total |
|---|---|---|---|---|
| Fer | 1 | 2 | 1 | 4 |
| Or | 1 | 1 | 1 | 3 |
| Diamant | 2 | 3 | 1 | 6 |
| Netherite | 2 | 3 | 2 | 7 |

Texture unique 64×128 contenant les quatre matériaux. Casque → tête ; plastron → corps et queue ; protège-pattes → quatre pattes. Retrait aux cisailles ; pièces récupérées à la mort.

### A.6 Fichiers de référence
`FennecEntity`, `FennecModel`, `FennecRenderer`, `ModEntities`, `ModEvents`, `FennecArmorItem`, `FennecArmorTier`, `FennecArmorSlot`, `ModItems`, `custom_pet.geo.json`, `custom_pet.animation.json`, `custom_pet.png`.

### A.7 Reste à réaliser sur ce mob
- Intégration au projet Forge, compilation et premiers tests en jeu.
- Apparition naturelle, œuf d'apparition, table de butin.
- Sons du mob (ambiant, dégâts, mort).
- Suivi du regard, animation de morsure, version bébé.
- Variante apprivoisée (collier), ordres supplémentaires.
- Équilibrage et recette complète (§12).

---

## Annexe B — Enseignements de la production du Fennec

1. **Rattacher chaque cube à son os.** Créer le cube ne suffit pas : sans rattachement explicite à un groupe, il reste à la racine et ne bouge pas avec les animations.
2. **Les keyframes sont des écarts.** Ne pas redoubler la rotation de repos d'un os dans une animation.
3. **Tester les poses numériquement.** Pour les poses extrêmes (sur le dos, accroupi), mesurer la hauteur minimale des cubes pour éviter l'enfoncement dans le sol.
4. **Décider de la taille de texture dès le début.** Prévoir de la place pour les variantes et l'équipement.
5. **Une texture unique et des os masquables** permettent de mélanger les matériaux d'armure sans multiplier les fichiers.
6. **Séparer animations d'état et animations ponctuelles** (deux contrôleurs) évite les conflits de lecture.
7. **Valider l'export** avec l'outil de validation GeckoLib avant chaque livraison.
8. **Les captures d'écran réalisées pendant la production** servent de preuve de recette : les conserver dans le dépôt.

---

## Annexe C — Modèle de fiche d'une nouvelle créature (à copier)

```
Nom :
Identifiant :
Concept / références :
Hitbox (L × H) :
Comportement (passif / neutre / hostile) :
Apprivoisable (oui/non, aliment) :
Vie sauvage / apprivoisé :
Dégâts / vitesse :
Biomes d'apparition :
Butin :
Animations (nom, boucle, déclencheur) :
Sons :
Objets liés :
Remarques :
```
