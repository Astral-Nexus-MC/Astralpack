"""Régénère entièrement le Nexus : met de côté les chunks de la dimension astralpack:nexus d'un monde.

Usage (serveur ARRÊTÉ) : python tools/regenerer_nexus.py /chemin/du/serveur/world [--oui]

Le dossier <monde>/dimensions/astralpack/nexus est déplacé en <monde>/dimensions/astralpack/nexus.avant-regeneration-AAAAMMJJ-HHMMSS
(rien n'est supprimé : pour revenir en arrière, remettre le dossier à sa place). Au prochain trajet vers le Nexus, le jeu regénère le
terrain avec les blocs actuels du mod (même relief, même graine) et le portail de retour est reconstruit au même endroit.
Perdus à la régénération : constructions, lits, coffres et objets au sol dans le Nexus. Les données du mod (portail actif, faux
unique) sont dans le monde principal et restent intactes.
"""
import os, sys, shutil, datetime

if len(sys.argv) < 2:
    sys.exit(__doc__)
world = sys.argv[1]
nexus = os.path.join(world, 'dimensions', 'astralpack', 'nexus')
if not os.path.isdir(nexus):
    sys.exit('Introuvable : %s (rien à régénérer, ou mauvais dossier de monde)' % nexus)
if os.path.exists(os.path.join(world, 'session.lock')):
    print('Attention : session.lock présent. Le serveur doit être arrêté avant de continuer.')
if '--oui' not in sys.argv:
    sys.exit('Ajouter --oui pour confirmer la mise de côté de : %s' % nexus)
backup = nexus + '.avant-regeneration-' + datetime.datetime.now().strftime('%Y%m%d-%H%M%S')
shutil.move(nexus, backup)
print('Chunks du Nexus déplacés vers :', backup)
print('Le Nexus sera regénéré au prochain trajet.')
