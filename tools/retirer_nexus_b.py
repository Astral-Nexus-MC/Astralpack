"""Retire la dimension héritée `astralpack:nexus_b` d'un monde, AVANT de passer à une version du mod qui ne la contient plus.

Pourquoi : un monde Minecraft enregistre la liste de ses dimensions dans level.dat. Si une dimension enregistrée n'existe
plus dans le mod, le serveur refuse de charger le monde (« Failed to get element ... dimension_type / astralpack:nexus_b »).
Cet outil retire l'entrée de level.dat ; le reste du monde n'est pas touché.

Utilisation (serveur ARRÊTÉ) :
    pip install nbtlib
    python tools/retirer_nexus_b.py /chemin/du/serveur/world
    python tools/retirer_nexus_b.py /chemin/du/serveur/world --supprimer-donnees

Une copie de level.dat est faite à côté (level.dat.avant-retrait-nexus_b) ; pour annuler, il suffit de la remettre à la
place de level.dat. Les joueurs qui s'étaient déconnectés dans nexus_b réapparaissent dans le monde principal.
"""
import argparse
import os
import shutil
import sys

DIMENSION = 'astralpack:nexus_b'


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('monde', help="dossier du monde (celui qui contient level.dat)")
    parser.add_argument('--supprimer-donnees', action='store_true',
                        help="supprime aussi les chunks de nexus_b (dimensions/astralpack/nexus_b)")
    args = parser.parse_args()

    try:
        import nbtlib
    except ImportError:
        sys.exit("nbtlib est nécessaire : pip install nbtlib")

    level = os.path.join(args.monde, 'level.dat')
    if not os.path.isfile(level):
        sys.exit(f"level.dat introuvable dans {args.monde}")

    data = nbtlib.load(level)
    dimensions = data['Data']['WorldGenSettings']['dimensions']
    print('Dimensions enregistrées :', ', '.join(dimensions.keys()))
    if DIMENSION not in dimensions:
        print(f"{DIMENSION} n'est pas enregistrée : rien à faire.")
    else:
        backup = level + '.avant-retrait-nexus_b'
        if os.path.exists(backup):
            sys.exit(f"{backup} existe déjà : je n'écrase pas une sauvegarde. Déplacez-la, puis relancez.")
        shutil.copy2(level, backup)
        del dimensions[DIMENSION]
        data.save(level, gzipped=True)
        print(f"{DIMENSION} retirée de level.dat (sauvegarde : {backup}).")
        print('Dimensions restantes :', ', '.join(nbtlib.load(level)['Data']['WorldGenSettings']['dimensions'].keys()))

    folder = os.path.join(args.monde, 'dimensions', 'astralpack', 'nexus_b')
    if os.path.isdir(folder):
        if args.supprimer_donnees:
            shutil.rmtree(folder)
            print(f"Données supprimées : {folder}")
        else:
            print(f"Les chunks restent sur le disque ({folder}) ; --supprimer-donnees les efface.")

    players = os.path.join(args.monde, 'playerdata')
    if os.path.isdir(players):
        concernes = 0
        for name in os.listdir(players):
            if name.endswith('.dat'):
                try:
                    dim = nbtlib.load(os.path.join(players, name)).get('Dimension')
                except Exception:
                    continue
                if dim is not None and str(dim).strip('"') == DIMENSION:
                    concernes += 1
        if concernes:
            print(f"{concernes} joueur(s) s'étaient déconnectés dans nexus_b : ils réapparaîtront dans le monde principal.")


if __name__ == '__main__':
    main()
