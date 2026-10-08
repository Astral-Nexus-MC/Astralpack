# Recette de la Death Scythe

Suivi du plan de recette ([cahier des charges des armes et outils](CAHIER_DES_CHARGES_ARME_OUTIL.md) §8), déroulé selon la [feuille de route](FEUILLE_DE_ROUTE_RECETTE.md). Fiche : [FICHE_DEATH_SCYTHE.md](FICHE_DEATH_SCYTHE.md). Statut : **validée définitivement en jeu avec la version 0.2.7, publiée en 0.3.0** (2026-10-08) : raccourci hotbar, F5, pose des deux mains, renvoi de l'objet de la main secondaire. Toutes les cases ci-dessous sont cochées sur confirmation du responsable (2026-10-08).

## Obtention
- [x] La recette (grille de la fiche) donne une Death Scythe ; l'objet apparaît dans l'onglet créatif ; `/give @s astralpack:death_scythe` fonctionne.
- [x] Nom et infobulle en FR et EN ; l'infobulle indique « Deux mains : main secondaire vide ».

## Rendu
- [x] Inventaire : icône lisible, non déformée.
- [x] Au sol et dans un cadre : taille et orientation correctes.
- [x] Première personne, main droite et main gauche : la faux est tenue par le manche, lame vers le haut et vers l'avant, sans traverser la caméra.
- [x] Troisième personne, main droite et main gauche : même tenue, sans clipping avec le corps ni les armures.
- [x] Texture sans décalage (anneaux, croix blanches, lame) ; aucun scintillement.
- [x] Pas de baisse de FPS notable avec plusieurs joueurs portant la faux.

## Lumières
- [x] Dans une grotte ou la nuit, les croix blanches, le tranchant de la lame et les bagues du manche brillent (pas d'ombrage), en main, au sol et en inventaire.
- [x] Le reste de la faux reste sombre et subit l'éclairage normal.
- [x] Les coups à pleine puissance font jaillir des particules d'âme sur la cible ; pas de particules avec la main secondaire occupée.

## Combat
- [x] Dégâts conformes : 9 à pleine puissance, 3 avec la main secondaire occupée (balayage compris).
- [x] Cadence lente (1,0 attaque/s) et allonge +1 bloc.
- [x] Frappe en arc : touche les ennemis proches de la cible, à pleine puissance seulement.
- [x] Wither I pendant 3 secondes sur la cible, sauf créatures immunisées ; aucun effet sur le porteur.
- [x] Enchantements applicables et fonctionnels : Tranchant, Affilage, Aura de feu, Pillage, Solidité, Raccommodage.
- [x] Durabilité de 1 400 ; réparation à l'enclume avec un lingot de netherite.

## Règle des deux mains
- [x] Main secondaire vide : pleins dégâts, balayage et Wither.
- [x] Bouclier, torche ou nourriture en main secondaire : dégâts (balayage compris) divisés par trois, pas de Wither, aucune erreur.
- [x] Changer d'objet de main secondaire entre deux coups : la règle suit immédiatement.
- [x] La règle s'applique contre les mobs comme contre les joueurs.

## Sons
- [x] Balayage audible au coup ; son du Wither audible à l'application de l'effet, volume cohérent.

## Persistance et serveur
- [x] Durabilité, enchantements et nom conservés après déconnexion et redémarrage.
- [x] Serveur dédié : règle et effets appliqués côté serveur, rendu côté client.
- [x] Aucun crash au chargement ni au déchargement de chunks.
- [x] Un client sans le mod est refusé proprement.
