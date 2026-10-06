# Recette du Fennec

Suivi du plan de recette (cahier des charges §12, déroulé selon la [feuille de route](FEUILLE_DE_ROUTE_RECETTE.md)). Recette complète et validée en production pour le Fennec (mod 0.2.1, 2026-10-07).

| Domaine | État | Détail |
|---|---|---|
| Rendu | OK | Texture, orientation, hitbox, ombre |
| Animations | OK | `idle`, `walk`, `sit`, transitions, `belly_scratch`, `attack`, `bite`, `shake` |
| Comportement | OK | Apparition, apprivoisement, suivi, assise, cibles, sauvegarde |
| Équipement | OK | Testé en jeu |
| Sons | OK | Testé en jeu |
| Performance | OK | 20 Fennecs visibles |
| Compatibilité | OK | Aucun conflit avec les mods du serveur |
| Serveur dédié | OK | Testé sur le serveur |

## Liste de contrôle (tous les points validés en jeu)

### Animations
- [x] `attack` : laisser un Fennec sauvage s'en prendre à un poulet ou un lapin.
- [x] `bite` : se joue au moment où le coup touche.
- [x] `shake` : sortir un Fennec de l'eau ou de la pluie.

### Ordres (roue d'interaction Fordix, touche R maintenue en visant son Fennec)
Le clic droit main vide ne change plus l'ordre : les ordres passent par la roue du mod Fordix. Il n'y a plus d'ordre « errer ».
- [x] La roue s'ouvre sur un Fennec apprivoisé dont on est propriétaire (et pas sur un sauvage ni sur celui d'un autre joueur).
- [x] « Suis-moi » : le Fennec se lève et suit le propriétaire, le rejoint quand il s'éloigne.
- [x] « Reste ici » : il s'assoit (animation `sit`) et ne bouge plus.
- [x] « Nourrir » avec de la viande crue : il se soigne ; « Renommer » avec une étiquette renommée fonctionne.
- [x] Blessé en « Reste ici », il se relève et repasse en « Suis-moi ».
- [x] L'ordre est conservé après déconnexion et redémarrage (et un Fennec resté en « errer » avant la mise à jour repasse en « Suis-moi »).
- [x] Clic droit main vide **sans Maj** : aucun changement d'ordre ; s'il tient un objet en gueule, il le lâche.
- [x] Maj + clic droit main vide lance toujours la gratouille du ventre.
- [x] Un Fennec apprivoisé n'a aucun signe distinctif (choix voulu).

### Apparition naturelle
- [x] Des groupes de Fennecs apparaissent dans les badlands et dans le désert (sur sable, terre, herbe ou terracotta).
- [x] Aucune apparition dans les autres biomes.

### Rythme jour / nuit
- [x] Un Fennec sauvage s'endort le jour (animation `sleep`, plus de cris) et se réveille la nuit.
- [x] Il se réveille si on le frappe, ou si un joueur debout s'approche à moins de 3 blocs (un joueur accroupi ne le réveille pas).
- [x] Un Fennec apprivoisé dort le jour seulement sous « Reste ici » ; sous « Suis-moi » il reste éveillé.
- [x] La nuit : dégâts +25 % et attaque plus rapide (11 ticks). Le jour : dégâts −25 % et attaque plus lente (19 ticks).
- [x] Dans une dimension sans cycle jour/nuit (Nether, End), pas de bonus ni de malus.

### Terrier (bloc en forme de dôme)
Le terrier est un **bloc** : abri de sable en dôme (intérieur 0,75 × 0,75 bloc), entrée à l'avant. Un Fennec **sauvage** le construit lui-même ; pour un Fennec **apprivoisé**, le joueur le fabrique.
- [x] Recette : sable (3) en haut, sable à gauche et à droite avec un poulet cru au centre, terre stérile (3) en bas → `Terrier de fennec` (onglet créatif ; `/give @s astralpack:fennec_burrow`).
- [x] Le bloc se pose (l'entrée regarde le joueur), se casse plus vite à la pelle et se récupère.
- [x] Le Fennec **entre** dans le bloc et s'y couche sans rester coincé (marge de hauteur faible : 0,75 pour 0,7). Sinon, élargir l'entrée ou relever le toit.
- [x] **Sauvage, de jour**, sans terrier à moins de 8 blocs : il choisit du sable ou de la terre tendre à moins de 6 blocs, **creuse d'abord l'entrée** (un bloc, animation `dig`, particules, bruit), y descend, **creuse la chambre** (le bloc d'à côté), puis le bloc terrier apparaît dans ce trou de deux blocs, entrée tournée vers la fosse, et il s'y couche (animation `sleep`).
- [x] Le trou est bien de 2 blocs alignés (entrée devant, chambre derrière), sans eau ni lave adjacente, sans gravats ni objets au sol, et le Fennec peut ressortir de la fosse (une marche d'un bloc).
- [x] Un sauvage réutilise un terrier existant à portée (le sien, ou un terrier fabriqué par un joueur) au lieu d'en construire un autre ; un seul Fennec par terrier.
- [x] Avec `/gamerule mobGriefing false`, il ne creuse rien et dort sur place. Sur de la pierre, de l'eau ou près de la lave, il ne creuse pas non plus (il dort sur place).
- [x] **Apprivoisé, de jour**, sous « Reste ici », à moins de 8 blocs d'un terrier : il se lève, va s'y coucher et y dort. Sans terrier à portée il dort sur place, assis. Sous « Suis-moi » il suit son propriétaire.
- [x] La nuit il se réveille et sort ; un joueur debout à moins de 3 blocs réveille un sauvage (pas un apprivoisé).
- [x] Les terriers construits restent dans le monde après déconnexion et redémarrage (ce sont des blocs).

### Objets dans la gueule
- [x] Le Fennec ramasse un objet au sol (un seul à la fois) et l'affiche dans la gueule (position à ajuster si besoin dans `FennecMouthItemLayer`).
- [x] Il va chercher les objets proches ; un Fennec apprivoisé reste à moins de 12 blocs de son propriétaire.
- [x] Clic droit main vide du propriétaire : il lâche l'objet (puis ne ramasse plus pendant 20 s).
- [x] L'objet tombe à sa mort, et reste dans la gueule après déconnexion et redémarrage.
- [x] Il ne ramasse rien assis, endormi, bébé, ni si `mobGriefing` est désactivé.

### Pelage en relief (poils 3D)
- [x] **Texture haute résolution (×4)** : `fennec.png` fait 256×640 alors que le modèle déclare un espace UV de 64×160 (voir `geo/fennec.geo.json`). Vérifier que la robe, les yeux, les dents et l'armure s'affichent aux bons endroits en jeu. En cas de décalage, revenir au commit `c8cd5c1` (texture 64×160) ou me le signaler.
- [x] Deux fines couches de poils entourent le corps, la tête, les pattes et la base de la queue : silhouette légèrement duveteuse, robe et taches toujours visibles dessous.
- [x] Les yeux et le museau restent dégagés (pas de poils devant le visage).
- [x] Armures retravaillées (texture ×4 : biseaux, reflets, nervures, doublure en cuir ; ceintures en relief sur le plastron ; rivets en 3D sur le plastron, le casque et les protège-pattes) : elles paraissent moins « blocs », sans poils qui les traversent, pour les 4 matériaux.
- [x] L'armure de queue (plastron) est faite de 4 plaques articulées qui couvrent presque toute la queue (le bout sombre reste dégagé) et suivent ses mouvements, sans clipping ni poils qui la traversent.
- [x] L'armure (casque, plastron, protège-pattes, plaques de queue) passe au-dessus des poils : aucun poil ne traverse la plaque, et les poils restent visibles autour et sous les pièces.
- [x] La tête est moins fournie que le corps (poils plus clairsemés), le visage reste dégagé.
- [x] Aucun scintillement (z-fighting) ni bord transparent anormal, de près comme de loin.
- [x] Pas de baisse de FPS sensible avec 20 Fennecs visibles.

### Bébé
- [x] Taille du bébé : GeckoLib réduit peut-être déjà les bébés ; si le bébé est trop petit, mettre `BABY_SCALE` à 1 dans `FennecRenderer`.
- [x] Hitbox du bébé cohérente avec le modèle.

### Équipement
- [x] Équiper chaque pièce (casque, plastron, protège-pattes) : clic droit avec l'objet.
- [x] Les 4 matériaux (fer, or, diamant, netherite) s'affichent sur le modèle.
- [x] Les bonus de défense se cumulent par pièce.
- [x] Retirer une pièce et la récupérer ; récupération à la mort.
- [x] Recettes utilisables, noms FR et EN, icônes dans l'inventaire.
- [x] L'armure est conservée après déconnexion et redémarrage.

### Sons
- [x] Ambiant, dégâts et mort audibles, volume correct.
- [x] Sons d'équipement différents selon le matériau et la pièce.
- [x] Sons de pas et de coup avec armure.

## Équilibrage (référence : loup 1.20.1)
Vie 8/20, dégâts 2/4, vitesse 0,38 (loup 0,30), attaque toutes les 14 ticks (loup 20), apparition poids 8 en groupes de 4 dans les badlands et le désert. Pas de loot table : le Fennec ne lâche que son armure.
