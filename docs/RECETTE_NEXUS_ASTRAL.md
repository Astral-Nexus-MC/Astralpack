# Recette — Dimension « Astral Nexus » (v1, 0.4.0)

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

---

# Portail de la Death Scythe (0.5.0)

Statut : **développé sur `dev`, non testé en jeu** (2026-10-13). Vérifications automatiques déjà faites : voir « Réalisation du portail » dans la fiche.

## Porte 0 — Automatique
- [x] `./gradlew build` réussit (tests inclus : `RingGeometryTest`, `nexusPortalResourcesArePresent`).
- [x] Le serveur de développement démarre sans erreur ; détection de l'anneau, ouverture et fermeture, portail de retour et règle d'unicité vérifiés sans joueur.
- [x] FR et EN complets.

## Porte 1 — En jeu (solo)
Fabrication
- [ ] Les recettes du bloc d'anneau (par 4) et du pupitre sont fabricables ; les objets apparaissent dans l'onglet créatif ; les blocs se cassent à la pioche (fer minimum) et rendent leur objet.
- [ ] Le modèle et les textures (créés dans Blockbench) s'affichent correctement en jeu : bloc d'anneau (plaques de rune sur les quatre côtés), pupitre (vide, puis faux plantée avec sa lame), surface du portail (translucide, animée, sans ligne de raccord entre les blocs). Aucun damier violet et noir (texture manquante), de près comme de loin.
- [ ] La faux paraît plantée et non posée : son manche entre dans l'ouverture sombre de la collerette (aucun jour visible dessous), elle penche légèrement, des fissures lumineuses rayonnent sur le plateau et des étincelles montent. C'est bien la vraie Death Scythe (modèle complet, lueur), d'environ 2,7 blocs de haut, dans les quatre orientations du pupitre ; elle reste visible quand on s'éloigne, apparaît pour les autres joueurs et disparaît quand on la reprend.
- [ ] Le bloc d'anneau, le pupitre et leurs objets sont bien proportionnés dans l'inventaire, la main et au sol.

Portail
- [ ] Un anneau 13 × 13 complet, avec le pupitre à moins de 10 blocs : clic droit avec la faux la plante, le portail s'ouvre (surface translucide animée, particules).
- [ ] Fonctionne dans les deux orientations de l'anneau (est-ouest et nord-sud) et à différentes hauteurs.
- [ ] Anneau incomplet ou obstrué : message « aucun anneau complet et dégagé », la faux n'est pas plantée.
- [ ] Clic droit à main vide sur le pupitre : la faux est reprise (durabilité et enchantements intacts), le portail se ferme.
- [ ] Casser un bloc de l'anneau ferme le portail ; le réparer le rouvre.
- [ ] Casser le pupitre rend la faux et ferme le portail.
- [ ] Entrer dans le portail mène sur l'île centrale du Nexus ; un délai évite les allers-retours en boucle.
- [ ] Le pupitre ne fonctionne pas dans le Nexus (message).

Retour
- [ ] Traversée façon Stargate : en entrant dans le portail, on est immobilisé environ 1 seconde, le champ de vision s'élargit, un disque de lumière bleue et blanche grandit depuis le centre de l'écran avec des ondes, tout devient blanc ; à l'arrivée le blanc se dissipe en ondes. Les sons sont audibles et ni trop forts ni gênants ; l'effet n'est pas pénible pour les yeux (sinon, réduire `FOV_KICK` ou la durée).
- [ ] À l'ouverture du portail, une gerbe de lumière jaillit de chaque côté de l'anneau, avec un son.
- [ ] On ne peut pas se déplacer ni sauter pendant l'absorption, et on le peut de nouveau juste après l'arrivée (effets retirés).
- [ ] Le retour dépose devant le pupitre, du côté visible, tourné vers lui, à chaque fois, même en arrivant en courant, dans les deux orientations de l'anneau, et quel que soit l'endroit du pupitre par rapport à l'anneau.
- [ ] En marchant dans le portail (et non avec `/nexus`), l'arrivée se fait exactement sur l'île centrale, au point d'arrivée du hub, et le retour ramène devant l'anneau de départ : le défaut de la 0.4.2 (coordonnées du monde d'origine appliquées dans la nouvelle dimension) a disparu. Les journaux `[Nexus]` donnent les positions.
- [ ] L'arrivée par le portail se fait sur l'île centrale (point d'arrivée libre, jamais dans des blocs), y compris pour un anneau posé en hauteur ou sur une pente.
- [ ] Les notifications (pupitre, chute dans le vide, retour sans origine) s'affichent par Fordix, en Voidframe 2 (verre sombre, en haut à droite, au-dessus de tout), avec le titre « Astral Nexus » et la bonne couleur (vert quand le portail s'ouvre, orange pour un problème). Sans Fordix, elles s'affichent comme les notifications du jeu.
- [ ] Le retour ramène devant l'anneau de départ, sur un sol libre, même si l'anneau est contre une pente ou un mur.
- [ ] Le portail de retour existe au hub (14 blocs au sud de l'arrivée), toujours ouvert, bien posé sur le sol.
- [ ] Le prendre ramène à l'endroit d'où on est parti, du côté d'où on est entré, sans retraverser le portail en boucle.
- [ ] Sans origine connue (arrivé par `/nexus`), il ramène au point d'apparition du monde principal.

Une seule faux
- [ ] Après la première fabrication, la grille ne donne plus de Death Scythe (pour tous les joueurs).
- [ ] `/nexus faux` indique l'état ; `/nexus faux reset` autorise un nouveau craft ; `/nexus faux bloquer` verrouille (après un `/give`).

## Porte 2 — Persistance et cas limites
- [ ] Quitter et rouvrir le monde, faux plantée : portail toujours ouvert, faux toujours dans le pupitre.
- [ ] Redémarrer le serveur : portail et pupitre conservés.
- [ ] Un deuxième pupitre près du premier ne duplique pas la faux (il n'y en a qu'une).
- [ ] Le verrou de la faux survit au redémarrage du serveur.
- [ ] Mort ou déconnexion dans le Nexus : l'origine est conservée, le retour fonctionne.

## Porte 3 — Serveur dédié (mêmes mods)
- [ ] Le serveur démarre sans erreur ni avertissement `astralpack` avec les mods du serveur.
- [ ] **Un monde déjà utilisé en 0.4.0 ou 0.4.1 démarre sans erreur avec la 0.4.2, sans manipulation** : le journal contient « Dimension astralpack:nexus_b retirée », `level.dat.avant-retrait-nexus_b` existe, le Nexus et ses constructions sont intacts. (Faire une copie du dossier du monde avant, par prudence.)
- [ ] Un client connecté voit le portail, les blocs et leurs textures, sans erreur de modèle dans les logs.
- [ ] Plusieurs joueurs : un seul portail ouvrable (une seule faux) ; passages simultanés sans conflit.
