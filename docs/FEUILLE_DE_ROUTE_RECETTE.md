# Feuille de route — Recette et mise en production

Objectif : s'assurer que **chaque test est fait avant qu'une version arrive en production**, pour ne pas casser le serveur ni les joueurs.

Bases : [CAHIER_DES_CHARGES_MOB.md](CAHIER_DES_CHARGES_MOB.md) (§12 recette, §13 critères, §14 livraison) et [RECETTE_FENNEC.md](RECETTE_FENNEC.md) (liste de contrôle du Fennec). Pour un nouveau mob, la même feuille s'applique avec sa propre recette.

> **Rappel :** un push sur `main` qui change `mod_version` publie une release, et le serveur la récupère. **Tout ce qui est mergé avec une nouvelle version est en production.** La validation se fait donc *avant* le merge.

## Vue d'ensemble

| Porte | Où | Qui | Bloque le merge si… |
|---|---|---|---|
| 0. Automatique | Poste / CI sur `dev` | Claude | build ou tests en échec |
| 1. Fonctionnelle | Solo, jar de `dev` | Responsable | un point de la recette échoue |
| 2. Persistance et cas limites | Solo, monde de test | Responsable | état perdu, crash, comportement dangereux |
| 3. Serveur dédié | Serveur de test (mêmes mods) | Responsable | crash, désynchro, lag |
| 4. Publication | `main` | Claude, sur accord | une porte précédente non validée |
| 5. Après publication | Serveur de production | Responsable | anomalie → retour arrière |

Aucune porte ne se saute. Une porte qui échoue renvoie à la correction sur `dev`, puis on rejoue **la porte échouée et celles qui suivent**.

---

## Porte 0 — Automatique (avant tout test en jeu)

- [x] `./gradlew build` réussit (tests inclus, dont `ResourcesConsistencyTest`).
- [x] Toute nouvelle ressource (modèle, texture, son, langue, recette, loot table) est couverte par le test de cohérence ; sinon, le test est étendu.
- [x] Validation GeckoLib du modèle sans erreur (l'avertissement de taille de texture ×4 est connu et volontaire).
- [x] `blockbench/entity/<mob>.bbmodel` enregistré et identique à ce qui est exporté (os, pivots, UV).
- [x] FR et EN complets pour chaque nouvelle clé.
- [x] `docs/` à jour (fiche, recette, crédits des sons si besoin, README).
- [x] Commit poussé sur `dev` (pas de branche annexe) et CI `build.yml` verte.

## Porte 1 — Recette fonctionnelle en jeu (solo, jar de `dev`)

Préparer : `./gradlew build`, copier `build/libs/*.jar` dans le dossier `mods` d'un client avec GeckoLib 4.8.4, monde de test en créatif (puis survie pour les cas réels).

Rejouer les blocs de [RECETTE_FENNEC.md](RECETTE_FENNEC.md). État au 2026-10-06 :

| Bloc de la recette | État | Remarque |
|---|---|---|
| Animations (`attack`, `bite`, `shake`) | Validé | |
| Équipement (4 matériaux) | Validé | |
| Sons | Validé | |
| Terrier sauvage et apprivoisé | Validé | recette 2026-10-06 |
| Rythme jour/nuit | Validé | recette 2026-10-06 |
| Pelage en relief, texture ×4, FPS | Validé | recette 2026-10-06 |
| Bébé | Validé | recette 2026-10-06 |
| Objets dans la gueule (comportement) | Validé | ramassage, lâcher, mort, persistance, interdits |
| Objets dans la gueule (rendu) | Validé | mâchoire ouverte, objet devant les dents |
| Ordres (roue Fordix : Suis-moi / Reste ici) | **À revalider** | remplace le clic droit et l'ordre « errer » (0.2.1) |
| Apparition naturelle | Validé | non-régression 2026-10-06 |

Règle : une case ne se coche que si le test a été fait **sur la version candidate**. Une modification du comportement concerné décoche la case correspondante.

## Porte 2 — Persistance et cas limites (solo)

À jouer pour toute version qui touche l'entité, les blocs ou les données :

**Persistance**
- [x] Quitter le monde et le rouvrir : propriétaire, ordre, armure, objet en gueule, terrier posé conservés.
- [x] Redémarrer le jeu (pas seulement le monde) : même résultat.
- [x] Éloigner le Fennec (déchargement du chunk) puis revenir : aucun crash, état conservé.

**Cas limites du comportement**
- [x] `/gamerule mobGriefing false` : ni creusage ni ramassage.
- [x] Nether et End : pas de bonus/malus jour/nuit.
- [x] Terrier : sable, terre, pierre, eau, lave proches ; un seul Fennec par terrier ; terrier cassé pendant que le Fennec dort.
- [x] Fennec blessé pendant qu'il dort, creuse ou ramasse : se réveille et annule proprement.
- [x] Joueur accroupi vs debout près d'un Fennec endormi.
- [x] Bébé : ne ramasse pas, ne creuse pas, taille correcte.
- [x] Mort du Fennec : armure et objet en gueule lâchés, rien de dupliqué.
- [x] Commandes : `/summon astralpack:fennec`, `/give` de l'œuf, du terrier et des armures.

**Rendu**
- [x] Armures (4 matériaux) et pelage : pas de clipping, de scintillement ni de bord transparent, de près et de loin.
- [x] 20 Fennecs visibles : pas de chute de FPS notable.

## Porte 3 — Serveur dédié (version candidate)

À faire sur un serveur de test identique à la production (même Forge, mêmes mods) avec **le jar candidat** :
- [x] Le serveur démarre sans erreur ni avertissement lié à `astralpack` dans les logs.
- [x] Un client connecté voit le Fennec (modèle, texture, animations) : logique serveur, rendu client.
- [x] Les états synchronisés se voient côté client : sommeil, creusage, gratouille, armure, objet en gueule.
- [x] Apparition naturelle dans les badlands et le désert ; aucune ailleurs.
- [x] Plusieurs joueurs en même temps : ordres et terriers sans conflit.
- [x] Redémarrage du serveur : état conservé.
- [x] 30 minutes de jeu : pas de lag anormal, pas de fuite visible (TPS stable).
- [x] Un client **sans** le mod est refusé proprement (mod obligatoire des deux côtés).

## Porte 4 — Publication (sur accord explicite du responsable)

Ne démarre que si les portes 0 à 3 sont validées pour cette version.

1. [x] Le responsable donne son accord de merge.
2. [x] `dev` → `main` (fast-forward ou merge).
3. [x] `mod_version` incrémentée dans `gradle.properties` (0.1.4 → 0.1.5) — **jamais de version réutilisée**.
4. [x] Push sur `main` ; ramener le commit de version sur `dev`.
5. [x] Le workflow `release.yml` est vert ; le tag `vX.Y.Z` existe.
6. [x] La release GitHub contient le jar **et** son `.sha256` ; la somme correspond au jar.
7. [x] Le serveur a récupéré la bonne version (nom du jar dans `mods`).

## Porte 5 — Après publication

- [x] Démarrage du serveur propre (logs sans erreur `astralpack`).
- [x] Test rapide en jeu : apparition d'un Fennec (`/summon`), un ordre, une armure, le terrier.
- [x] Surveillance pendant la première session de jeu (crashs, lag, remarques des joueurs).

### Retour arrière
Si une anomalie bloquante apparaît :
1. Remettre sur le serveur le jar de la **version précédente** (release GitHub précédente) pour rétablir le service.
2. Corriger sur `dev`, rejouer les portes 0 à 3.
3. Republier avec une **nouvelle** version (ex. 0.1.6), sans réécrire un tag existant.

---

## Matrice : que rejouer selon le changement

| Changement | Portes à rejouer |
|---|---|
| Texte, doc, crédits | 0 |
| Texture, modèle, animation | 0, 1 (rendu et animations concernés), 2 (rendu), 3 (affichage client) |
| IA / comportement | 0, 1, 2 (cas limites + persistance), 3 |
| Nouvel objet, bloc ou recette | 0, 1 (objet/bloc), 2 (persistance), 3 |
| Attributs, équilibrage | 0, 1 (comportement), 3 |
| Son | 0, 1 (sons), 3 |
| Version de GeckoLib/Forge | **tout**, tous les mobs |

## Feuille de suivi d'une version (à copier dans le message de release)

```
Version : 0.1.x            Date :
Changements :
Porte 0  build + tests + docs        [ ]
Porte 1  recette en jeu              [ ]  (blocs rejoués : …)
Porte 2  persistance + cas limites   [ ]
Porte 3  serveur dédié               [ ]
Accord de merge du responsable       [ ]
Porte 4  release vérifiée            [ ]
Porte 5  contrôle post-déploiement   [ ]
Anomalies connues :
```

## Statut du Fennec

**Fennec v1 validé en production : mod Astralpack 0.2.0 (2026-10-07).** Toutes les portes 0 à 5 sont cochées pour cette version ; aucune case de [RECETTE_FENNEC.md](RECETTE_FENNEC.md) n'est ouverte. Les versions suivantes repartent de cette feuille : toute modification du Fennec rejoue les portes indiquées dans la matrice ci-dessus.
