# Fiche — Dimension « Nexus Astral »

Dimension hub du serveur Astral Nexus. Statut : **0.4.0 validée en jeu ; 0.5.0 (portail) développée sur `dev`, non testée en jeu** (2026-10-13), lumière choisie : ciel du monde principal à minuit fixe (variante A) ; terrain vérifié aussi sur un serveur de développement (3 600 chunks, voir « Réalisation »). Recette : [RECETTE_NEXUS_ASTRAL.md](RECETTE_NEXUS_ASTRAL.md). Méthode : même circuit que les mobs et les armes ([FEUILLE_DE_ROUTE_RECETTE.md](FEUILLE_DE_ROUTE_RECETTE.md)) ; une recette dédiée sera créée avec la v1.

## Intention

Un **lieu de vie** (hub avec des bases) au milieu de l'espace : sombre, mystique, mais lisible. Étoiles, galaxies, lumières de nébuleuses ; on voit le terrain sans lampe. Des **ressources exclusives** à cette dimension y justifient le voyage. L'accès se fait par un **portail circulaire façon Stargate**, ouvert avec la **Death Scythe**.

## Décisions prises

| Sujet | Décision |
|---|---|
| Usage | Hub avec bases, ressources exclusives |
| Ambiance | Espace sombre mais visible : étoiles, nébuleuses lumineuses, violet et bleu nuit |
| Terrain | Grande île centrale plate et constructible + îles satellites (ressources, dangers) |
| Accès | Anneau 13 × 13 + pupitre où l'on plante la Death Scythe (gratuit, une seule faux par serveur), retour par portail permanent |
| Première version | **0.4.0 : dimension seule**, accès par commande |

## Le portail (version 0.5.0)

Décisions du responsable (2026-10-13) :

- **Une seule Death Scythe par serveur.** La faux ne peut être fabriquée qu'une fois : la recette passe par un type de recette qui vérifie, côté serveur, qu'aucune faux n'a encore été fabriquée (donnée enregistrée dans le monde). Conséquence voulue : **un seul portail peut être ouvert à la fois sur tout le serveur.** Commandes d'administration : `/nexus faux` (état), `/nexus faux reset` (autoriser un nouveau craft si la faux est perdue), `/nexus faux bloquer` (marquer la faux comme déjà fabriquée, par exemple après un `/give`).
- **Coût : gratuit tant que la faux est plantée.** Pas de perte de durabilité. Le portail reste ouvert tant que la faux est dans le pupitre ; la retirer le ferme.
- **Anneau 13 × 13** (cercle de rayon 6), vertical, fait de **blocs d'anneau astral** à fabriquer. Environ 36 blocs.
- **Pupitre du Nexus** : bloc posé à moins de 10 blocs du centre de l'anneau. Clic droit avec la Death Scythe : elle est plantée, le portail s'ouvre si l'anneau est complet et dégagé. Clic droit à main vide : elle est reprise et le portail se ferme. Casser le pupitre rend la faux et ferme le portail.
- **Portail du Nexus** : surface qui remplit l'anneau (non fabricable). Entrer dedans téléporte sur l'île centrale du hub. Le portail se ferme tout seul si l'anneau est cassé.
- **Retour : portail permanent** au hub, à 14 blocs de l'arrivée, toujours ouvert (anneau de même taille, généré une seule fois). Il ramène le joueur à l'endroit d'où il est parti, côté d'où il est entré. Sans point d'origine connu, il ramène au point d'apparition du monde principal.
- Le pupitre ne fonctionne pas dans le Nexus.
- La recette de la faux reste volontairement difficile.

À fixer à l'équilibrage (provisoire en 0.5.0) : recettes des blocs d'anneau et du pupitre.

## Dimension (v1, 0.4.0)

- Identifiant : `astralpack:nexus`. Fichiers de données : type de dimension, réglages de bruit, biomes.
- **Terrain** : partir des réglages de bruit de l'End (île centrale + îles autour) avec nos propres biomes et blocs de surface. Blocs vanilla en attendant les blocs du mod (0.5.0).
- **Biomes** : `nexus_hub` (île centrale, aucun monstre), `astral_isles` (îles satellites, endermen), `crystal_reef` (récif de cristal, endermen plus rares). `void_fringe` (bordure) est reporté : le terrain n'a pas de repère de distance utilisable en données seules.
- **Lumière** : nuit fixe, lumière ambiante relevée, couleurs de ciel et de brouillard violet nuit, particules ambiantes. **Choix validé en jeu : effets du monde principal + minuit fixe** (la variante à ciel de l'End a été comparée puis abandonnée).
- **Vie de hub** : lits fonctionnels (pas d'ancre de réapparition). Chute dans le vide : retour sur l'île centrale sans dégâts (code, v1).
- **Accès v1** : commandes (niveau opérateur) `/nexus` (arrivée sur l'île centrale), `/nexus retour` (point d'apparition du monde principal). La variante de comparaison à ciel de l'End n'a plus de commande ; sa dimension héritée `astralpack:nexus_b` est conservée (voir « Compatibilité des mondes »).
- Hors v1 : ciel avec galaxie et nébuleuses dessinées (code client), blocs du mod, minerai, équipement, créatures, boss, structures.

## Ressources exclusives (v2)

Minerai « fragment astral », cristaux de nébuleuse, poussière stellaire. Servent à des blocs de construction, des recettes et un palier d'équipement au-dessus de la faux. Noms et valeurs à fixer dans une fiche dédiée avant le développement.

## Feuille de route

| Version | Contenu |
|---|---|
| 0.4.0 | Dimension seule, accès par commande, retour du vide |
| 0.5.0 | Blocs du mod, anneau 13 × 13, pupitre à la Death Scythe, portail de retour, une seule faux par serveur |
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

## Compatibilité des mondes

**Ne jamais supprimer une dimension déjà publiée.** Un monde enregistre ses dimensions ; si l'une d'elles disparaît d'une version à l'autre, le serveur refuse de charger le monde (erreur « Failed to get element … dimension_type »). C'est pourquoi `astralpack:nexus_b` (variante de lumière de la 0.4.0, abandonnée) reste dans le mod : sans commande, sans usage, mais présente. Découvert pendant la préparation de la 0.5.0 sur un serveur de développement. Même règle pour `astralpack:nexus`, ses types et ses biomes.

## Réalisation du portail (0.5.0)

- **Blocs** : `astral_ring_block` (segment ; recette : 4 par fabrication avec obsidienne, éclats d'améthyste, perle de l'Ender), `nexus_console` (pupitre ; recette : blocs d'anneau, œil de l'Ender, dalles d'ardoise, lingot de netherite), `nexus_portal` (surface, sans objet, indestructible). Recettes **provisoires**, à équilibrer.
- **Anneau** : cercle de rayon 6 (13 × 13), 36 cases pour l'anneau et 97 pour la surface (`world/RingGeometry`, `world/NexusRing`). Le pupitre cherche l'anneau complet et dégagé le plus proche dans un rayon de 10 blocs, dans les deux orientations. Si l'anneau est cassé, le portail se ferme tout seul ; réparé, il se rouvre (contrôle toutes les 2 secondes tant que la faux est plantée).
- **Pupitre** (`block/NexusConsoleBlock`, `block/entity/NexusConsoleBlockEntity`) : clic droit avec la faux pour la planter, à main vide pour la reprendre. La faux est conservée telle quelle (durabilité, enchantements). Casser le pupitre rend la faux. Inactif dans le Nexus.
- **Trajets** (`world/NexusTravel`) : entrer dans la surface mène au hub et mémorise l'origine (dimension, position, côté d'entrée) dans les données persistantes du joueur ; le portail de retour ramène à ce point, sinon au point d'apparition du monde principal. Délai de 5 secondes entre deux trajets.
- **Portail de retour** : généré une seule fois à 14 blocs au sud de l'arrivée, reconstruit au même endroit s'il est abîmé ; sa position est enregistrée (`NexusData`).
- **Une seule faux par serveur** : type de recette `astralpack:unique_shaped` (`recipe/UniqueShapedRecipe`) ; la première fabrication enregistre le verrou dans le monde. Une faux obtenue par `/give` ne verrouille rien : utiliser `/nexus faux bloquer`.
- **Vérifié sur un serveur de développement** : chargement du type de recette ; détection de l'anneau dans les deux orientations (centre exact), ouverture de 97 cases sur 97, fermeture complète, anneau cassé ou surface obstruée non détectés ; génération du portail de retour (97 cases sur 97) ; recette de la faux acceptée avant la première fabrication, refusée après, acceptée après un `reset`.
- **Modèles et textures créés dans Blockbench** (`blockbench/block/astral_ring_block.bbmodel`, `nexus_console.bbmodel`, `nexus_portal.bbmodel`), exportés en modèles Java : bloc d'anneau (cube de pierre taillée avec plaques de rune en relief sur les quatre côtés, 5 éléments), pupitre (socle à deux niveaux, colonne à pilastres, panneau de rune, plateau incliné à rune, quatre cristaux, collerette ; 15 éléments, 25 avec la faux plantée dont le manche à bagues et la lame en croissant d'acier-violet), surface du portail (panneau mince, texture animée de 16 images qui se raccorde d'un bloc à l'autre). Textures de 16 × 16 peintes pixel par pixel : `astral_ring_block`, `astral_ring_rune`, `nexus_console_glow`, `nexus_console_scythe`, `nexus_portal` (16 × 256, `.mcmeta`).
- **Non vérifié** (demande un joueur ou un client) : rendu en jeu des blocs, animation et transparence du portail, particules, faux plantée dans le pupitre, trajets aller et retour, messages.
