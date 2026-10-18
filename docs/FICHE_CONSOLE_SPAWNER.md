# Console des spawners (`astralpack:spawner_console`)

Statut : sur `dev`, non publiée (2026-10-18). Testée sur serveur de développement par commandes (niveaux fixés avec `/data merge`) : les réglages du spawner changent, puis reviennent à l'identique quand la console est cassée. Non testée avec un joueur (clic droit, notifications).

**Principe** : une console par chunk (une deuxième ne se pose pas). Elle règle **les spawners de son chunk, et eux seuls** (tous les étages). Chaque réglage a un niveau de -5 à +5 : un objet de **bonus** le monte d'un cran, un objet de **malus** le descend d'un cran ; un objet est consommé par cran (rien en créatif). Clic droit à main vide : état dans le chat (niveaux, objets demandés, nombre de spawners). Clic droit avec un objet : applique et notifie.

| Réglage | Effet par cran | Limites | Objet de bonus | Objet de malus |
|---|---|---|---|---|
| Cadence | délai x 0,75 (bonus) ou x 1,33 (malus) | délai >= 10 ticks | quartz du Nether | toile d'araignée |
| Quantité | +1 monstre par apparition | 1 à 12 | éclat de nébuleuse | barreaux de fer |
| Population max | +4 monstres autour | 1 à 64 | bloc d'os | laisse |
| Portée d'activation | +8 blocs | 4 à 64 | œil de l'Ender | boule de slime |
| Rayon d'apparition | +2 blocs | 1 à 16 | boussole | ficelle |
| Vie des monstres | bonus : +50 % de vie par cran (jusqu'à x 3,5) ; malus : -20 % par cran, et au niveau -5 un demi-cœur (1 point de vie) | min. 1 point | nourriture crue (étiquette `astralpack:raw_food` : bœuf, porc, poulet, mouton, lapin, morue, saumon, poisson tropical, pomme de terre) | spéléothème (stalactite de dripstone) |

Les objets sont dans `spawner/SpawnerStat` (un seul endroit à modifier, avec les effets). Le bonus de quantité utilise une ressource du Nexus (éclat de nébuleuse) ; la cadence utilise du quartz du Nether.

**Fonctionnement technique** : `SpawnerControl` garde les valeurs d'origine du spawner (`ForgeData.AstralBase`) à la première application et recalcule toujours à partir d'elles ; la console repasse sur les spawners du chunk toutes les 40 ticks (les spawners posés plus tard sont pris en compte) ; casser la console, ou décrocher le spawner avec la clé, remet les valeurs d'origine. Un spawner qui a quitté le chunk de la console n'est plus contrôlé.

**Recette** (provisoire) : 4 lingots de fer, 1 perle de l'Ender, 1 redstone, 3 obsidienne (`IEI` / `IRI` / `OOO`).

**Visuel** : modèle et textures faits dans Blockbench (`blockbench/block/spawner_console.bbmodel`) : socle sombre, écran incliné qui affiche cinq barres (une par réglage), deux antennes de cristal et une balise d'astralite.

**Vie des monstres** : ce réglage n'est pas écrit dans le spawner, il s'applique à chaque monstre au moment où il apparaît (`spawner/SpawnerEvents` : le spawner qui le provoque est repéré à la fin de l'initialisation du monstre, puis sa vie maximale est réglée quand il entre dans le monde, après que le jeu a fini de l'initialiser). Limite : les spawners dont les données d'apparition contiennent plus que le simple type de monstre (monstres nommés, équipement imposé) ne déclenchent pas l'événement d'initialisation du jeu : leur vie n'est pas modifiée. Non testé avec un joueur (il faut un joueur à portée pour qu'un spawner tourne).
