# Fiche — Dimension « Nexus Astral »

Dimension hub du serveur Astral Nexus. Statut : **v1 (0.4.0) validée en jeu** (2026-10-13), lumière choisie : ciel du monde principal à minuit fixe (variante A) ; terrain vérifié aussi sur un serveur de développement (3 600 chunks, voir « Réalisation »). Recette : [RECETTE_NEXUS_ASTRAL.md](RECETTE_NEXUS_ASTRAL.md). Méthode : même circuit que les mobs et les armes ([FEUILLE_DE_ROUTE_RECETTE.md](FEUILLE_DE_ROUTE_RECETTE.md)) ; une recette dédiée sera créée avec la v1.

## Intention

Un **lieu de vie** (hub avec des bases) au milieu de l'espace : sombre, mystique, mais lisible. Étoiles, galaxies, lumières de nébuleuses ; on voit le terrain sans lampe. Des **ressources exclusives** à cette dimension y justifient le voyage. L'accès se fait par un **portail circulaire façon Stargate**, ouvert avec la **Death Scythe**.

## Décisions prises

| Sujet | Décision |
|---|---|
| Usage | Hub avec bases, ressources exclusives |
| Ambiance | Espace sombre mais visible : étoiles, nébuleuses lumineuses, violet et bleu nuit |
| Terrain | Grande île centrale plate et constructible + îles satellites (ressources, dangers) |
| Accès | Portail en anneau + pupitre de commande où l'on plante la Death Scythe |
| Première version | **0.4.0 : dimension seule**, accès par commande |

## Le portail (version 0.5.0, hors v1)

- **Anneau** : grand cercle vertical en blocs du mod, à construire. Une fois activé, la surface s'ouvre.
- **Pupitre** : bloc de commande à côté de l'anneau, où l'on **plante la Death Scythe**. La faux fournit l'énergie ; retirée, le portail se referme.
- À trancher en phase portail : la faux perd-elle de la durabilité à chaque ouverture (proposition : oui, modérée), durée d'ouverture, retour depuis le hub (un anneau côté hub avec son propre pupitre, ou une faux plantée aussi).
- La recette de la faux reste volontairement difficile : le portail en hérite.

## Dimension (v1, 0.4.0)

- Identifiant : `astralpack:nexus`. Fichiers de données : type de dimension, réglages de bruit, biomes.
- **Terrain** : partir des réglages de bruit de l'End (île centrale + îles autour) avec nos propres biomes et blocs de surface. Blocs vanilla en attendant les blocs du mod (0.5.0).
- **Biomes** : `nexus_hub` (île centrale, aucun monstre), `astral_isles` (îles satellites, endermen), `crystal_reef` (récif de cristal, endermen plus rares). `void_fringe` (bordure) est reporté : le terrain n'a pas de repère de distance utilisable en données seules.
- **Lumière** : nuit fixe, lumière ambiante relevée, couleurs de ciel et de brouillard violet nuit, particules ambiantes. **Choix validé en jeu : effets du monde principal + minuit fixe** (la variante à ciel de l'End a été comparée puis retirée).
- **Vie de hub** : lits fonctionnels (pas d'ancre de réapparition). Chute dans le vide : retour sur l'île centrale sans dégâts (code, v1).
- **Accès v1** : commandes (niveau opérateur) `/nexus` (arrivée sur l'île centrale), `/nexus retour` (point d'apparition du monde principal). La variante de comparaison à ciel de l'End a été retirée en 0.4.1.
- Hors v1 : ciel avec galaxie et nébuleuses dessinées (code client), blocs du mod, minerai, équipement, créatures, boss, structures.

## Ressources exclusives (v2)

Minerai « fragment astral », cristaux de nébuleuse, poussière stellaire. Servent à des blocs de construction, des recettes et un palier d'équipement au-dessus de la faux. Noms et valeurs à fixer dans une fiche dédiée avant le développement.

## Feuille de route

| Version | Contenu |
|---|---|
| 0.4.0 | Dimension seule, accès par commande, retour du vide |
| 0.5.0 | Blocs du mod, portail en anneau et pupitre à la Death Scythe |
| 0.6.0 | Ressources exclusives, équipement |
| 0.7.0 | Créatures, structures, boss |
| Plus tard | Ciel sur mesure (galaxie, nébuleuses) |

## Recette à prévoir (v1)

Portes 0 à 3 de la feuille de route, plus :
- La dimension se charge sans erreur, avec et sans Biomes O' Plenty et Oh The Biomes We've Gone.
- Génération des chunks fluide (pas de lag), île centrale constructible, satellites atteignables.
- Lisibilité : on voit le terrain sans lampe ; comparaison des variantes A et B.
- Lits, retour du vide, mort et réapparition, persistance après redémarrage du serveur.
- Aucun monstre sur l'île centrale ; monstres uniquement sur les satellites.
- Un monde existant n'est pas modifié par l'ajout de la dimension.

## Réalisation (0.4.0)

- Dimension `astralpack:nexus` ; types de dimension, réglages de bruit et biomes dans `data/astralpack/` ; code dans `world/NexusDimension.java` et `world/NexusEvents.java`. Les fichiers de données sont générés par `tools/generate_nexus_dimension.py` (réglages : `4.0 2.5 -1.0 70 -0.15`).
- **Relief** : base de l'End vanilla (île centrale) fusionnée avec des îles satellites faites d'un bruit large, à dessus plat et dessous effilé. Blocs vanilla provisoires : mousse et tuf (hub), calcite et tuf (îles), basalte lisse et ardoise des abysses (récif).
- **Mesures sur 960 × 960 blocs** (serveur de développement, 3 600 chunks) : île centrale d'environ 220 blocs de large (≈ 37 500 blocs² de surface), sol à y 62–63 (médiane) avec des bosses jusqu'à y 70 ; une centaine d'îles satellites de 20 à 120 blocs de large, 12,5 % de colonnes pleines, dessus à y ≤ 74 ; le biome `nexus_hub` couvre toute l'île centrale. La génération de masse a provoqué un seul retard de 2 s côté serveur.
- **Validé en jeu par le responsable (2026-10-13)** : accès, rendu, île centrale, îles satellites, retour du vide. Intention : « une vraie nuit galactique ».
