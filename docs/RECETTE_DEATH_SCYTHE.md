# Recette de la Death Scythe

Suivi du plan de recette ([cahier des charges des armes et outils](CAHIER_DES_CHARGES_ARME_OUTIL.md) §8), déroulé selon la [feuille de route](FEUILLE_DE_ROUTE_RECETTE.md). Fiche : [FICHE_DEATH_SCYTHE.md](FICHE_DEATH_SCYTHE.md). Statut : **développée sur `dev`, non testée en jeu** (2026-10-08) ; toutes les cases sont ouvertes.

## Obtention
- [ ] La recette (grille de la fiche) donne une Death Scythe ; l'objet apparaît dans l'onglet créatif ; `/give @s astralpack:death_scythe` fonctionne.
- [ ] Nom et infobulle en FR et EN ; l'infobulle indique « Deux mains : main secondaire vide ».

## Rendu
- [ ] Inventaire : icône lisible, non déformée.
- [ ] Au sol et dans un cadre : taille et orientation correctes.
- [ ] Première personne, main droite et main gauche : la faux est tenue par le manche, lame vers le haut et vers l'avant, sans traverser la caméra.
- [ ] Troisième personne, main droite et main gauche : même tenue, sans clipping avec le corps ni les armures.
- [ ] Texture sans décalage (anneaux, croix blanches, lame) ; aucun scintillement.
- [ ] Pas de baisse de FPS notable avec plusieurs joueurs portant la faux.

## Combat
- [ ] Dégâts conformes : 9 à pleine puissance, 3 avec la main secondaire occupée (balayage compris).
- [ ] Cadence lente (1,0 attaque/s) et allonge +1 bloc.
- [ ] Frappe en arc : touche les ennemis proches de la cible, à pleine puissance seulement.
- [ ] Wither I pendant 3 secondes sur la cible, sauf créatures immunisées ; aucun effet sur le porteur.
- [ ] Enchantements applicables et fonctionnels : Tranchant, Affilage, Aura de feu, Pillage, Solidité, Raccommodage.
- [ ] Durabilité de 1 400 ; réparation à l'enclume avec un lingot de netherite.

## Règle des deux mains
- [ ] Main secondaire vide : pleins dégâts, balayage et Wither.
- [ ] Bouclier, torche ou nourriture en main secondaire : dégâts (balayage compris) divisés par trois, pas de Wither, aucune erreur.
- [ ] Changer d'objet de main secondaire entre deux coups : la règle suit immédiatement.
- [ ] La règle s'applique contre les mobs comme contre les joueurs.

## Sons
- [ ] Balayage audible au coup ; son du Wither audible à l'application de l'effet, volume cohérent.

## Persistance et serveur
- [ ] Durabilité, enchantements et nom conservés après déconnexion et redémarrage.
- [ ] Serveur dédié : règle et effets appliqués côté serveur, rendu côté client.
- [ ] Aucun crash au chargement ni au déchargement de chunks.
- [ ] Un client sans le mod est refusé proprement.
