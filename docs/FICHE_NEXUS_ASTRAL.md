# Fiche — Dimension « Astral Nexus »

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
- **Accès v1** : commandes (niveau opérateur) `/nexus` (arrivée sur l'île centrale), `/nexus retour` (point d'apparition du monde principal). La variante de comparaison à ciel de l'End a été retirée (voir « Compatibilité des mondes »).
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

**Une dimension déjà publiée ne se supprime pas à la légère.** Un monde enregistre ses dimensions dans `level.dat` ; si l'une d'elles disparaît du mod, le serveur refuse de charger le monde (erreur « Failed to get element … dimension_type »). C'est ce qui est arrivé avec `astralpack:nexus_b`, la variante de lumière de la 0.4.0 abandonnée : reproduit sur un serveur de développement, puis corrigé.

**Retrait de `astralpack:nexus_b` (0.4.2)** : la dimension n'existe plus dans le mod. Le déploiement étant automatique (le VPS installe la dernière release tout seul), le mod **nettoie lui-même le monde au démarrage d'un serveur dédié** (`world/LegacyWorldMigration`, appelé depuis le constructeur du mod, avant que le jeu ne lise le monde) : il lit `level-name` dans `server.properties`, et si `level.dat` enregistre `astralpack:nexus_b`, il en fait une copie (`level.dat.avant-retrait-nexus_b`) puis retire cette seule entrée. Aucune manipulation n'est nécessaire. Vérifié sur un serveur de développement avec un monde de 0.4.0 : le serveur démarre, la migration est journalisée (`[Astralpack] Dimension astralpack:nexus_b retirée…`), `nexus_b` est inconnue, `nexus` intacte, et sans la migration le serveur ne démarrait pas.

Limites : seul un serveur dédié est migré ; un monde solo créé avec 0.4.0 ou 0.4.1 doit être nettoyé à la main. Les chunks de nexus_b restent sur le disque (inoffensifs ; `--supprimer-donnees` de l'outil les efface). Les joueurs déconnectés dans nexus_b réapparaissent dans le monde principal.

**Outil de secours** : `tools/retirer_nexus_b.py` fait la même chose à la main (serveur arrêté ; `pip install nbtlib` puis `python tools/retirer_nexus_b.py /chemin/du/serveur/world`), si la migration automatique échoue (message d'erreur dans les journaux).

Règle pour la suite : même procédure pour tout type de dimension, biome ou réglage de bruit déjà publié que l'on voudrait retirer. `astralpack:nexus` ne se retire pas.

## Réalisation du portail (0.5.0)

- **Blocs** : `astral_ring_block` (segment ; recette : 4 par fabrication : obsidienne pleureuse aux coins, éclats d'améthyste sur les côtés, œil de l'Ender au centre ; niveau de difficulté « moyen » choisi par le responsable le 2026-10-15, soit 36 obsidiennes pleureuses, 36 éclats d'améthyste et 9 yeux de l'Ender pour un anneau de 36 blocs), `nexus_console` (pupitre ; recette : blocs d'anneau, œil de l'Ender, dalles d'ardoise, lingot de netherite), `nexus_portal` (surface, sans objet, indestructible). Recette de l'anneau fixée au niveau moyen (passage obligé par le Nether et l'End) ; recette du pupitre inchangée (lingot de netherite).
- **Anneau** : cercle de rayon 6 (13 × 13), 36 cases pour l'anneau et 97 pour la surface (`world/RingGeometry`, `world/NexusRing`). Le pupitre cherche l'anneau complet et dégagé le plus proche dans un rayon de 10 blocs, dans les deux orientations. Si l'anneau est cassé, le portail se ferme tout seul ; réparé, il se rouvre (contrôle toutes les 2 secondes tant que la faux est plantée).
- **Pupitre** (`block/NexusConsoleBlock`, `block/entity/NexusConsoleBlockEntity`) : clic droit avec la faux pour la planter, à main vide pour la reprendre. La faux est conservée telle quelle (durabilité, enchantements). Casser le pupitre rend la faux. Inactif dans le Nexus.
- **Trajets** (`world/NexusTravel`) : entrer dans la surface mène au hub et mémorise l'origine (dimension, position, côté d'entrée) dans les données persistantes du joueur ; le portail de retour ramène à ce point, sinon au point d'apparition du monde principal. Délai de 5 secondes entre deux trajets.
- **Traversée plus lente** (0.4.5, retour du responsable : « parfait mais trop court ») : absorption de 2,5 s (`ABSORB_TICKS` 50) et fondu d'arrivée de 3 s (`ARRIVAL_TICKS` 60), délai avant un nouveau trajet 12 s ; l'ouverture et le retour devant le pupitre sont validés.
- **Traversée façon Stargate** (0.4.4, `NexusTravel`, `client/ClientPortalFx`, `network/PortalFxPacket`) : un trajet n'est plus instantané. Le joueur est happé pendant 1,2 s : immobilisé (lenteur et saut bloqués, sans icône), champ de vision qui s'élargit de 45°, disque de lumière bleue et blanche qui grandit depuis le centre de l'écran avec trois ondes qui s'en détachent, jusqu'à un plein blanc ; sons du portail du Nether puis de l'Enderman, gerbe de particules. Il est alors téléporté (dans le jeu, jamais pendant un paquet de déplacement) et arrive dans une gerbe de lumière : le blanc se dissipe en 1,5 s en ondes qui s'éloignent et le champ de vision se resserre. Côté ouverture : une gerbe de lumière jaillit de la surface, de chaque côté de l'anneau, gonfle puis retombe en 0,8 s (`NexusConsoleBlockEntity.kawoosh`), avec un son d'ouverture. Tout est réglable par des constantes (`ABSORB_TICKS`, `ARRIVAL_TICKS`, `FOV_KICK`, `KAWOOSH_TICKS`).
- **Retour devant le pupitre** (0.4.4) : le portail actif (pupitre, orientation, anneau) est enregistré dans `NexusData` à l'ouverture ; à l'entrée, le joueur en retient le pupitre, et le retour le dépose **devant le pupitre, du côté où il regarde**, à un puis deux blocs, puis sur ses autres côtés, sur un sol libre qui n'est pas dans la surface du portail, tourné vers lui. Le côté d'entrée déduit de la position reste le secours si le pupitre a disparu. Corrige un retour qui, « parfois », ne ramenait pas côté pupitre : le côté déduit de la position était faux quand le joueur courait, car il avait déjà dépassé le plan du portail quand le serveur le voyait.
- **Trajets différés à la fin du tick** (0.4.3, `NexusTravel.runPending`, déclenché par `NexusEvents.onServerTick`) : une téléportation demandée par `entityInside` pendant le traitement du paquet de déplacement du joueur est écrasée par le jeu, qui remet ensuite le joueur aux coordonnées envoyées par son client, mais dans la nouvelle dimension. Symptômes observés en jeu avec la 0.4.2 : arrivée dans des blocs ou dans le vide, et retour très loin du portail d'origine. `/nexus` n'avait pas ce défaut (il ne s'exécute pas pendant un paquet de déplacement), et un joueur simulé non plus (il n'envoie pas de paquets) : c'est pourquoi les tests sur serveur de développement ne le montraient pas. Le trajet est maintenant mis en file et exécuté à la fin du tick ; vérifié : rien ne bouge juste après la demande, le joueur est au hub à la fin du tick.
- **Arrivée et retour sûrs** (0.4.2, `world/SafeSpot`) : le point d'arrivée au hub et le point de retour sont vérifiés (sol plein dessous, deux blocs libres au-dessus) et corrigés au besoin, au lieu d'être pris tels quels. Le retour se fait devant le portail, du côté d'où le joueur est entré, à 3 blocs (puis 2, puis 4, puis de l'autre côté), à l'endroit debout le plus proche de sa hauteur d'origine ; sans origine connue, au point d'apparition du monde principal, avec une notification. Côté serveur, la vitesse d'un joueur est presque nulle : le côté d'entrée se déduit de sa position par rapport au plan du portail. Chaque trajet est tracé dans les journaux (`[Nexus] …`).
- **Notifications** (`network/Notify`, `network/FordixBridge`, `client/ClientToasts`) : les messages du pupitre, de la chute dans le vide et du retour sans origine ne sont plus dans la barre d'action. Ils passent par **Fordix** (`FordixNotifications.send`, interface Voidframe 2 : verre sombre, liseré d'accent, en haut à droite, au-dessus de tout) quand il est installé, avec le style INFO, SUCCESS ou WARNING selon le message et le titre « Astral Nexus » ; sinon, par les notifications du jeu (toasts, via le canal réseau `astralpack:main`). Fordix est une dépendance facultative (`mods.toml`), appelée par réflexion : s'il est absent ou si son API change, le mod se rabat sur les toasts et le journal le dit. Signatures vérifiées dans le jar Fordix 0.1.85 du modpack. Les retours de commandes restent dans le chat.
- **Portail de retour** : généré une seule fois à 14 blocs au sud de l'arrivée, reconstruit au même endroit s'il est abîmé ; sa position est enregistrée (`NexusData`).
- **Une seule faux par serveur** : type de recette `astralpack:unique_shaped` (`recipe/UniqueShapedRecipe`) ; la première fabrication enregistre le verrou dans le monde. Une faux obtenue par `/give` ne verrouille rien : utiliser `/nexus faux bloquer`.
- **Vérifié sur un serveur de développement** : chargement du type de recette ; détection de l'anneau dans les deux orientations (centre exact), ouverture de 97 cases sur 97, fermeture complète, anneau cassé ou surface obstruée non détectés ; génération du portail de retour (97 cases sur 97) ; recette de la faux acceptée avant la première fabrication, refusée après, acceptée après un `reset`.
- **Modèles et textures créés dans Blockbench** (`blockbench/block/astral_ring_block.bbmodel`, `nexus_console.bbmodel`, `nexus_portal.bbmodel`), exportés en modèles Java : bloc d'anneau (cube de pierre taillée avec plaques de rune en relief sur les quatre côtés, 5 éléments), pupitre (socle à deux niveaux, colonne à pilastres, panneau de rune, plateau incliné à rune, quatre cristaux, collerette ; 15 éléments, 17 avec la collerette lumineuse quand la faux est plantée), surface du portail (panneau mince, texture animée de 16 images qui se raccorde d'un bloc à l'autre). Textures de 16 × 16 peintes pixel par pixel : `astral_ring_block`, `astral_ring_rune`, `nexus_console_glow`, `nexus_console_scythe`, `nexus_portal` (16 × 256, `.mcmeta`).
- **La faux est plantée par la pointe de sa lame, dans l'encoche du plateau** (0.4.5, retours du responsable : « le côté lame planté dans le socle », « je veux que la pointe de la lame soit plantée », puis « sur le réceptable il y a une encoche, plante la pointe de la lame dessus ») : `NexusConsoleRenderer` fait pivoter la vraie faux de 105° autour de la pointe de sa lame, qui devient le point le plus bas (vérifié sur la géométrie : c'est le cas entre 65° et 125°), et la pose exactement sur l'encoche sombre cerclée de violet du plateau, dont la position a été **mesurée dans Blockbench** (0,5 ; 0,89 ; 0,44 : le plateau étant incliné de 22,5°, l'encoche est à 0,06 bloc vers l'avant du centre). La lame monte de l'encoche en s'incurvant, le manche part en diagonale vers le haut ; échelle 0,6 (environ 2,7 blocs de haut) ; réglages : `SCALE`, `TIP_HEIGHT`, `TIP_FRONT_OFFSET`, `ROLL_DEGREES`. Une faux ne peut pas être plantée pointe vers le bas, manche en haut : sa pointe se recourbe du côté du manche ; c'est le choix de 105° qui donne le meilleur compromis (90° : manche horizontal ; 120° : tête trop loin). **Sens** (0.4.6) : la faux est retournée de 180° autour de la verticale de sa pointe, pour que sa tête soit du côté opposé à celui de la version 0.4.5 (retour du responsable : « la faux n'est pas tournée dans le bon sens »). **Piège** : GeckoLib inverse l'axe horizontal du modèle à l'affichage ; les angles calculés sur le fichier du modèle s'appliquent donc dans le sens inverse (rotation de +105° dans le rendu pour 105° horaire vus de face). Essais refusés : manche enfoncé, lame en haut (0.4.4) ; faux retournée, tête dans le socle (0.4.5) ; pointe cachée dans la colonne (0.4.5). Autres éléments :
- **La faux plantée est la vraie Death Scythe** : un modèle de bloc plafonne à environ 1,1 bloc de haut avec des rotations simples, alors que la faux compte 88 éléments sur 5 blocs à l'échelle 1. Elle est donc dessinée par un afficheur de bloc (`client/renderer/NexusConsoleRenderer`) avec le rendu GeckoLib de l'objet lui-même, à l'échelle 0,4 (environ 2 blocs de haut), manche enfoncé dans la collerette, lame du même côté quelle que soit l'orientation du pupitre, avec sa lueur. Le pupitre envoie la faux aux clients (`NexusConsoleBlockEntity`).
- **Non vérifié** (demande un joueur ou un client) : rendu en jeu des blocs, animation et transparence du portail, particules, faux plantée dans le pupitre, trajets aller et retour, messages.
