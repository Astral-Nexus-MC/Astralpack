# Fiche — Dimension « Nexus Astral »

Dimension hub du serveur Astral Nexus. Statut : **fiche validée (2026-10-12), pas encore développée.** Méthode : même circuit que les mobs et les armes ([FEUILLE_DE_ROUTE_RECETTE.md](FEUILLE_DE_ROUTE_RECETTE.md)) ; une recette dédiée sera créée avec la v1.

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
- **Biomes** : `nexus_hub` (île centrale, aucun monstre), `astral_isles` (îles satellites), `crystal_reef` (cristaux, ressources), `void_fringe` (bordure, dangereuse).
- **Lumière** : nuit fixe, lumière ambiante relevée, couleurs de ciel et de brouillard violet nuit, particules ambiantes. **Deux variantes à comparer en jeu** : (A) effets du monde principal + minuit fixe, (B) effets de l'End. On garde celle qui garde le plus de lisibilité.
- **Vie de hub** : lits fonctionnels (pas d'ancre de réapparition). Chute dans le vide : retour sur l'île centrale sans dégâts (code, v1).
- **Accès v1** : commande uniquement, par exemple `/execute in astralpack:nexus run tp @s 0 100 0`.
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
