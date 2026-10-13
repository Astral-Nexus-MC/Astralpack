# Recette — Dimension « Nexus Astral » (v1, 0.4.0)

Suivi du plan de recette, déroulé selon la [feuille de route](FEUILLE_DE_ROUTE_RECETTE.md). Fiche : [FICHE_NEXUS_ASTRAL.md](FICHE_NEXUS_ASTRAL.md). Statut : **validée en jeu par le responsable avec la 0.4.0** (2026-10-13) ; toutes les cases sont cochées sur sa confirmation (« tout fonctionne »).

## Porte 0 — Automatique
- [x] `./gradlew build` réussit (tests inclus, dont `nexusDimensionFilesArePresent`).
- [x] Les fichiers de la dimension se chargent sans erreur sur un serveur de développement, génération de 3 600 chunks sans erreur.
- [x] FR et EN complets pour les messages et les commandes.
- [x] `docs/` à jour (fiche, recette).

## Porte 1 — En jeu (solo)
Accès et déplacements
- [x] `/nexus` te place sur l'île centrale, à la surface, sans dégâts.
- [x] `/nexus retour` te ramène dans le monde principal.
- [x] `/nexus` est refusé à un joueur non opérateur.
- [x] Tomber dans le vide ramène sur l'île centrale, sans dégâts, avec le message du Nexus.

Lumière et ambiance (variantes comparées)
- [x] On voit le terrain sans lampe ; on a bien une nuit galactique avec des étoiles.
- [x] Les couleurs du ciel et du brouillard sont violet et bleu nuit, les particules ambiantes sont visibles sans gêner.
- [x] Variante retenue : A (ciel du monde principal, minuit fixe) ; la B (ciel de l'End) est retirée en 0.4.1.

Terrain
- [x] L'île centrale est assez grande et plate pour construire ; un grand bâtiment tient dessus.
- [x] Des îles satellites sont visibles et atteignables (pont, ender pearl, élytres).
- [x] Aucun monstre sur l'île centrale ; des endermen sur les îles satellites.
- [x] Un lit fonctionne pour définir son point d'apparition dans le Nexus.

## Porte 2 — Persistance et cas limites
- [x] Construire, quitter le monde et le rouvrir : constructions conservées.
- [x] Mourir dans le Nexus : réapparition correcte (lit ou monde principal).
- [x] Plusieurs joueurs en même temps dans le Nexus.
- [x] Un monde existant n'est pas modifié par l'ajout de la dimension.

## Porte 3 — Serveur dédié (mêmes mods)
- [x] Le serveur démarre sans erreur ni avertissement `astralpack`, avec Biomes O' Plenty et Oh The Biomes We've Gone.
- [x] Un client connecté voit le Nexus (ciel, couleurs, particules) et y circule.
- [x] Génération des chunks sans lag anormal, TPS stable.
- [x] Redémarrage du serveur : constructions conservées.
