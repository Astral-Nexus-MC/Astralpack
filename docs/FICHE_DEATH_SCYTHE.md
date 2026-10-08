# Fiche de conception — Death Scythe

Arme de référence du mod. Méthode générale (armes et outils) : [CAHIER_DES_CHARGES_ARME_OUTIL.md](CAHIER_DES_CHARGES_ARME_OUTIL.md) · Recette : [RECETTE_DEATH_SCYTHE.md](RECETTE_DEATH_SCYTHE.md). Statut : **fiche validée, arme développée sur `dev`, à tester en jeu** (2026-10-08). Les valeurs chiffrées sont des points de départ, à régler en jeu.

## Identité
- Identifiant : `death_scythe` (`astralpack:death_scythe`). Nom FR : « Faux de la Mort » ; nom EN : « Death Scythe ».
- Concept : grande faux gothique noire, arme à deux mains.
- Référence visuelle : image fournie par le responsable (faux noire, longue lame courbe vers la droite, deux anneaux ornés avec une croix blanche, manche segmenté et épineux, pointe recourbée en bas).
- Source : première version générée par `blockbench/item/generate_death_scythe.py` (géométrie et texture) ; importée dans Blockbench le 2026-10-08 (`blockbench/item/death_scythe.bbmodel`, 84 cubes, validation GeckoLib sans erreur) pour le polissage.

## Silhouette du modèle
| Pièce | Description |
|---|---|
| Lame | Longue lame courbe, effilée, avec une arête dentelée près de l'anneau, noire à reflets sombres |
| Anneau haut | Disque noir cerclé, croix blanche (4 branches) au centre, entouré de 4 à 5 épines courbes |
| Manche haut | Section articulée, noueuse, avec épines |
| Anneau milieu | Petit disque noir à croix blanche, avec 3 petites épines pointant vers l'avant |
| Manche bas | Long manche droit rainuré (poignée) |
| Pointe | Extrémité recourbée vers l'extérieur, en crochet |

Palette : noir, gris très sombre, blanc cassé pour les croix. Aucune animation obligatoire ; légère oscillation au repos facultative.

## Mains
**Deux mains.** Pleins dégâts uniquement si la **main secondaire est vide** ; sinon tous les dégâts de l'arme (balayage compris) sont divisés par trois et le Wither est désactivé. L'infobulle indique « Deux mains : main secondaire vide ». Aucun blocage de la main secondaire.

## Statistiques (référence : épée en netherite)

| | Death Scythe | Épée en netherite |
|---|---|---|
| Dégâts (pleine puissance) | 9 | 8 |
| Dégâts (main secondaire occupée) | 3 | — |
| Cadence d'attaque | 1,0 attaque/s (modificateur −3,0) | 1,6 |
| Allonge d'attaque | +1,0 bloc | normale |
| Durabilité | 1 400 | 2 031 |
| Enchantabilité | 15 | 15 |
| Réparation | lingot de netherite | lingot de netherite |

Ces valeurs font de la faux une arme **lente, longue et puissante**, plus risquée que l'épée.

## Effets spéciaux
Appliqués **côté serveur**, seulement à pleine puissance (main secondaire vide).
1. **Attaque en arc** : le coup balaye comme l'épée (action `SWORD_SWEEP`), touchant les ennemis proches de la cible. Compatible avec l'enchantement Affilage.
2. **Wither léger** : la cible touchée reçoit **Wither I pendant 3 secondes** (60 ticks). Ignoré par les créatures immunisées (Wither, Wither Squelette, etc.). Aucun effet sur le porteur.

## Recette (provisoire, à équilibrer)

```
N N .
S R .
. R .
```
- `N` : lingot de netherite (2)
- `S` : crâne de Wither Squelette (1)
- `R` : bâton de blaze (2)

Résultat : 1 Death Scythe. Pas de variante de matériau pour la première version.

## Obtention
Fabrication uniquement ; aucune présence dans les coffres ou les butins pour l'instant.

## Sons (jeu de base)
- Coup : balayage de l'épée (`player.attack.sweep`).
- Effet Wither : son sourd du Wither (`entity.wither.shoot`, volume 0,4, hauteur 1,4) à l'application de l'effet.
- Aucun fichier personnalisé : aucune licence à créditer.

## Rendu et présentation
- Objet GeckoLib (`GeoItem`), modèle trop grand pour le format vanilla.
- Tenue à deux mains : lame vers le haut et vers l'avant, tenue par le manche ; première et troisième personne réglées pour la main droite et la main gauche.
- Icône d'inventaire : icône dédiée lisible (faux en diagonale), pas le modèle plein format.
- Texture : 128×128 (à confirmer à la modélisation) ; la taille est définitive dès le premier export.

## Fichiers prévus
`DeathScytheItem`, `ModTiers`, `DeathScytheModel`, `DeathScytheRenderer`, `TwoHandedEvents`, `ModItems`, `ModSounds` (si besoin), `geo/death_scythe.geo.json`, `textures/item/death_scythe.png`, `models/item/death_scythe.json`, `data/astralpack/recipes/death_scythe.json`, langues FR et EN.

## Réalisation
- Code : `DeathScytheItem` (SwordItem + GeoItem + `TwoHanded`), `ModTiers.DEATH`, `TwoHandedEvents` (malus côté serveur), `DeathScytheModel` et `DeathScytheRenderer`, enregistrement dans `ModItems`.
- Ressources : `geo/death_scythe.geo.json` (9 os, 84 cubes), `textures/item/death_scythe.png` (128×128), `animations/death_scythe.animation.json` (vide), `models/item/death_scythe.json` (réglages d'affichage provisoires), recette, langues FR et EN, test de cohérence.
- À régler en jeu : la tenue en main (échelle, rotation, position) pour la première et la troisième personne, la taille de l'icône d'inventaire.
- À reprendre dans Blockbench : import de la géométrie, polissage de la lame, tenue des deux mains.
