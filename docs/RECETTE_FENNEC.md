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
Vie 8/20, dégâts 2/4, vitesse 0,38 (loup 0,30), attaque toutes les 14 ticks (loup 20), apparition poids 8 en groupes de 4 dans les badlands. Pas de loot table : le Fennec ne lâche que son armure.
