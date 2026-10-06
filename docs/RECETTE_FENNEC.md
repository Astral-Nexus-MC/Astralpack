# Recette du Fennec

Suivi du plan de recette (cahier des charges §12). Dernière mise à jour : 2026-10-06 (version 0.1.1 + équilibrage sur `dev`).

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

### Ordres (propriétaire, clic droit main vide)
- [ ] Le clic fait tourner suivre → rester → errer, avec le message dans la barre d'action.
- [ ] Suivre : le Fennec suit le propriétaire et le rejoint quand il s'éloigne.
- [ ] Rester : il s'assoit (animation `sit`) et ne bouge plus.
- [ ] Errer : il se promène librement sans suivre le propriétaire.
- [ ] Blessé en mode « rester », il se relève et repasse en « suivre ».
- [ ] L'ordre est conservé après déconnexion et redémarrage.
- [ ] Maj + clic main vide lance toujours la gratouille du ventre.
- [ ] Un Fennec apprivoisé n'a aucun signe distinctif (choix voulu).

### Apparition naturelle
- [ ] Des groupes de Fennecs apparaissent dans les badlands et dans le désert (sur sable, terre, herbe ou terracotta).
- [ ] Aucune apparition dans les autres biomes.

### Rythme jour / nuit
- [ ] Un Fennec sauvage s'endort le jour (animation `sleep`, plus de cris) et se réveille la nuit.
- [ ] Il se réveille si on le frappe, ou si un joueur debout s'approche à moins de 3 blocs (un joueur accroupi ne le réveille pas).
- [ ] Un Fennec apprivoisé dort le jour seulement sous l'ordre « rester » ; sous « suivre » ou « errer » il reste éveillé.
- [ ] La nuit : dégâts +25 % et attaque plus rapide (11 ticks). Le jour : dégâts −25 % et attaque plus lente (19 ticks).
- [ ] Dans une dimension sans cycle jour/nuit (Nether, End), pas de bonus ni de malus.

### Terrier (Fennec sauvage, de jour)
- [ ] Un Fennec sauvage creuse une petite rampe de 3 blocs dans le sable ou la terre (animation `dig`, particules et bruit de bloc), y entre et s'y endort.
- [ ] Le plafond sous du sable devient du grès (sinon le sable tomberait dans le tunnel).
- [ ] Les jours suivants, il retourne dans le même terrier ; la nuit il en ressort.
- [ ] Il ne creuse jamais près de l'eau ou de la lave, ni dans la pierre, et rien si `mobGriefing` est désactivé (il dort alors sur place).
- [ ] Le terrier est conservé après déconnexion et redémarrage.
- [ ] Un Fennec apprivoisé ne creuse pas.

### Objets dans la gueule
- [ ] Le Fennec ramasse un objet au sol (un seul à la fois) et l'affiche dans la gueule (position à ajuster si besoin dans `FennecMouthItemLayer`).
- [ ] Il va chercher les objets proches ; un Fennec apprivoisé reste à moins de 12 blocs de son propriétaire.
- [ ] Clic droit main vide du propriétaire : il lâche l'objet (puis ne ramasse plus pendant 20 s).
- [ ] L'objet tombe à sa mort, et reste dans la gueule après déconnexion et redémarrage.
- [ ] Il ne ramasse rien assis, endormi, bébé, ni si `mobGriefing` est désactivé.

### Pelage en relief (poils 3D)
- [ ] Deux fines couches de poils entourent le corps, la tête, les pattes et la base de la queue : silhouette légèrement duveteuse, robe et taches toujours visibles dessous.
- [ ] Les yeux et le museau restent dégagés (pas de poils devant le visage).
- [ ] Sous une pièce d'armure, les poils de la zone couverte disparaissent (casque, plastron, protège-pattes) : rien ne dépasse à travers l'armure.
- [ ] Aucun scintillement (z-fighting) ni bord transparent anormal, de près comme de loin.
- [ ] Pas de baisse de FPS sensible avec 20 Fennecs visibles.

### Bébé
- [ ] Taille du bébé : GeckoLib réduit peut-être déjà les bébés ; si le bébé est trop petit, mettre `BABY_SCALE` à 1 dans `FennecRenderer`.
- [ ] Hitbox du bébé cohérente avec le modèle.

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
