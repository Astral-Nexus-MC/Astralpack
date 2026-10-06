# Fiche de conception — Fennec

Mob de référence du mod. Méthode générale : [CAHIER_DES_CHARGES_MOB.md](CAHIER_DES_CHARGES_MOB.md) · Recette : [RECETTE_FENNEC.md](RECETTE_FENNEC.md). État au 2026-10-06 (version 0.1.4).

## Identité
- Identifiant : `fennec` (`astralpack:fennec`). Source : `blockbench/entity/fennec.bbmodel`.
- Concept : petit renard du désert, pelage sable tacheté de brun, très grandes oreilles, queue touffue à bout sombre, pelage en relief (poils 3D).
- Hitbox : 0,6 × 0,7 bloc. Bébé : rendu à l'échelle 0,5 (`BABY_SCALE`).

## Comportement
- **Carnivore** : chasse poules et lapins ; neutre envers le joueur.
- **Apprivoisable** : poulet cru ou lapin cru, 1 chance sur 3. Aucun signe distinctif visuel (choix voulu).
- **Ordres** (clic droit main vide, propriétaire) : suivre → rester → errer. Maj + clic main vide : gratouille du ventre.
- **Rythme jour/nuit** : dort le jour, actif la nuit. Actif le jour : dégâts −25 % et attaque lente (19 ticks) ; la nuit : dégâts +25 % et attaque rapide (11 ticks). Aucun effet dans une dimension sans cycle.
- **Objets en gueule** : ramasse un objet à la fois, l'affiche dans la gueule (mâchoire ouverte), le lâche sur clic droit main vide (puis pause de 20 s) ou à sa mort.
- **Terrier** : bloc en dôme (`fennec_burrow`).
  - *Sauvage* : de jour, sans terrier à moins de 8 blocs, il creuse 2 blocs (entrée puis chambre) dans du sable ou de la terre tendre, le bloc terrier apparaît dans le trou, il s'y couche. Respecte `mobGriefing`, évite fluides et blocs-entités.
  - *Apprivoisé* : le joueur fabrique le terrier ; sous l'ordre « errer », à moins de 8 blocs, il s'y couche le jour.

## Statistiques (référence : loup 1.20.1)

| | Sauvage | Apprivoisé |
|---|---|---|
| Vie | 8 | 20 |
| Dégâts | 2 | 4 |
| Vitesse | 0,38 (loup 0,30) | idem |
| Cadence d'attaque | 14 ticks (loup 20) | idem |
| Portée de suivi | 16 blocs | 16 blocs |

Apparition : badlands et désert, poids 8, groupes de 4 (`forge/biome_modifier/fennec_spawns*.json`). Pas de loot table : il ne lâche que son armure.

## Armure (casque, plastron, protège-pattes)

| Matériau | Casque | Plastron | Protège-pattes | Total | Robustesse |
|---|---|---|---|---|---|
| Fer | 1 | 2 | 1 | 4 | — |
| Or | 1 | 1 | 1 | 3 | — |
| Diamant | 2 | 3 | 1 | 6 | 0,5 / 1 / 0,5 |
| Netherite | 2 | 3 | 2 | 7 | 1 / 1 / 1 + résistance au recul |

Casque → tête ; plastron → corps + 4 plaques de queue ; protège-pattes → 4 pattes. Retrait aux cisailles, pièces récupérées à la mort. Sons d'équipement, de pas et de coup selon le matériau.

## Modèle et texture
- GeckoLib, `geo/fennec.geo.json` (espace UV 64×160), texture **256×640 (×4)** : l'avertissement du validateur sur l'écart de taille est volontaire.
- Os : `body` › `torso`, `head` › (`head_base`, `jaw`, `ear_left`, `ear_right`, `mouth_item`), `tail` (4 plaques d'armure), 4 pattes ; os de pelage `fur_torso`, `fur_head`, `fur_leg_*`, `fur_tail` (3 couches) ; os d'armure `armor_<pièce>_<matériau>`.

## Animations
`idle` (loop), `walk` (loop), `sit` (hold), `shake` (once), `attack` (hold), `bite` (action), `belly_scratch` (loop), `sleep` (loop), `dig` (loop).
Priorité du contrôleur `movement` : gratouille → creuser → dormir → assis → marche → attaque → repos. `bite` et `shake` passent par le contrôleur `action`.

## Sons
Ambiant, dégâts, mort (fichier libre de droits, voir [CREDITS_SONS.md](CREDITS_SONS.md)) ; équipement, pas et coup avec armure selon le matériau.

## Fichiers clés
`FennecEntity` (entité et buts d'IA : sommeil, terrier, ramassage, suivi, gratouille, mêlée), `FennecModel`, `FennecRenderer`, `FennecMouthItemLayer`, `FennecArmorItem|Tier|Slot`, `FennecBurrowBlock`, `ModEntities`, `ModItems`, `ModBlocks`, `ModSounds`, `ModEvents`.
