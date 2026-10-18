# Clé à molette à spawner (`astralpack:spawner_wrench`)

Statut : sur `dev`, non testée en jeu (2026-10-18).

**Rôle** : clic droit sur un spawner à monstres avec la clé : le spawner est décroché sans être cassé et devient l'objet `astralpack:moved_spawner` (« Spawner décroché »), qui garde le monstre et tous les réglages du spawner (données `SpawnerData` dans l'objet). Poser l'objet remet un spawner identique (le spawner vanilla ne le permet pas aux joueurs qui ne sont pas opérateurs, c'est pourquoi l'objet est à part : `item/MovedSpawnerItem` recharge les données à la pose).

**Détails** : la clé a 64 utilisations (1 par spawner décroché) ; l'objet décroché brille (marqueur), ne se cumule pas et affiche le monstre dans son infobulle ; il n'est pas dans l'onglet créatif ; un spawner décroché ne donne pas d'expérience. La clé respecte les protections (`mayUseItemAt`, `mayInteract`). Casser un spawner posé reste vanilla : il ne donne rien.

**Recette** (provisoire) : 3 lingots de fer, 1 diamant, 2 bâtons de blaze (`IDI` / ` R ` / ` R `).

**Fichiers** : `item/SpawnerWrenchItem`, `item/MovedSpawnerItem`, `registry/ModItems`, modèle `models/item/spawner_wrench.json` (handheld), texture dans Blockbench (`blockbench/item/spawner_wrench.bbmodel`).
