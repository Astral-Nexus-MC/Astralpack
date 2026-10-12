# Recette — Dimension « Nexus Astral » (v1, 0.4.0)

Suivi du plan de recette, déroulé selon la [feuille de route](FEUILLE_DE_ROUTE_RECETTE.md). Fiche : [FICHE_NEXUS_ASTRAL.md](FICHE_NEXUS_ASTRAL.md). Statut : **développée sur `dev`, non testée en jeu** (2026-10-12) ; toutes les cases sont ouvertes sauf la porte 0.

## Porte 0 — Automatique
- [x] `./gradlew build` réussit (tests inclus, dont `nexusDimensionFilesArePresent`).
- [x] Les fichiers de la dimension se chargent sans erreur sur un serveur de développement, génération de 3 600 chunks sans erreur.
- [x] FR et EN complets pour les messages et les commandes.
- [x] `docs/` à jour (fiche, recette).

## Porte 1 — En jeu (solo)
Accès et déplacements
- [ ] `/nexus` te place sur l'île centrale, à la surface, sans dégâts.
- [ ] `/nexus retour` te ramène dans le monde principal.
- [ ] `/nexus` et les variantes sont refusées à un joueur non opérateur.
- [ ] Tomber dans le vide ramène sur l'île centrale, sans dégâts, avec le message du Nexus.

Lumière et ambiance (comparer `/nexus` et `/nexus variante_b`)
- [ ] On voit le terrain sans lampe ; on a bien une nuit galactique avec des étoiles.
- [ ] Les couleurs du ciel et du brouillard sont violet et bleu nuit, les particules ambiantes sont visibles sans gêner.
- [ ] Variante retenue : ____ (A = ciel du monde principal, B = ciel de l'End). L'autre sera retirée.

Terrain
- [ ] L'île centrale est assez grande et plate pour construire ; un grand bâtiment tient dessus.
- [ ] Des îles satellites sont visibles et atteignables (pont, ender pearl, élytres).
- [ ] Aucun monstre sur l'île centrale ; des endermen sur les îles satellites.
- [ ] Un lit fonctionne pour définir son point d'apparition dans le Nexus.

## Porte 2 — Persistance et cas limites
- [ ] Construire, quitter le monde et le rouvrir : constructions conservées.
- [ ] Mourir dans le Nexus : réapparition correcte (lit ou monde principal).
- [ ] Plusieurs joueurs en même temps dans le Nexus.
- [ ] Un monde existant n'est pas modifié par l'ajout de la dimension.

## Porte 3 — Serveur dédié (mêmes mods)
- [ ] Le serveur démarre sans erreur ni avertissement `astralpack`, avec Biomes O' Plenty et Oh The Biomes We've Gone.
- [ ] Un client connecté voit le Nexus (ciel, couleurs, particules) et y circule.
- [ ] Génération des chunks sans lag anormal, TPS stable.
- [ ] Redémarrage du serveur : constructions conservées.
