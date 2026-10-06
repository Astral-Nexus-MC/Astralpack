# Recette du Fennec

Suivi du plan de recette (cahier des charges §12). Dernière mise à jour : 2026-10-06 (version 0.1.1 + équilibrage sur `dev`).

| Domaine | État | Détail |
|---|---|---|
| Rendu | OK | Texture, orientation, hitbox, ombre |
| Animations | Partiel | OK : `idle`, `walk`, `sit`, transitions, `belly_scratch`. **À tester** : `attack`, `bite`, `shake` |
| Comportement | OK | Apparition, apprivoisement, suivi, assise, cibles, sauvegarde |
| Équipement | **À tester** | Voir ci-dessous |
| Sons | **À tester** | Voir ci-dessous |
| Performance | OK | 20 Fennecs visibles |
| Compatibilité | OK | Aucun conflit avec les mods du serveur |
| Serveur dédié | OK | Testé sur le serveur |

## À tester

### Animations
- [ ] `attack` : laisser un Fennec sauvage s'en prendre à un poulet ou un lapin.
- [ ] `bite` : se joue au moment où le coup touche.
- [ ] `shake` : sortir un Fennec de l'eau ou de la pluie.

### Équipement
- [ ] Équiper chaque pièce (casque, plastron, protège-pattes) : clic droit avec l'objet.
- [ ] Les 4 matériaux (fer, or, diamant, netherite) s'affichent sur le modèle.
- [ ] Les bonus de défense se cumulent par pièce.
- [ ] Retirer une pièce et la récupérer ; récupération à la mort.
- [ ] Recettes utilisables, noms FR et EN, icônes dans l'inventaire.
- [ ] L'armure est conservée après déconnexion et redémarrage.

### Sons
- [ ] Ambiant, dégâts et mort audibles, volume correct.
- [ ] Sons d'équipement différents selon le matériau et la pièce.
- [ ] Sons de pas et de coup avec armure.

## Équilibrage (référence : loup 1.20.1)
Vie 8/20, dégâts 2/4, vitesse 0,38 (loup 0,30), attaque toutes les 14 ticks (loup 20), apparition poids 8 en groupes de 4 dans les badlands. Pas de loot table : le Fennec ne lâche que son armure.
