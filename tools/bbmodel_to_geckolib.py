#!/usr/bin/env python3
"""Convertit un .bbmodel (Blockbench, format GeckoLib) en geo.json / animation.json / png.

Usage: python3 tools/bbmodel_to_geckolib.py blockbench/entity/fennec.bbmodel fennec

Reproduit l'export Blockbench (conventions Bedrock : axe X inversé).
Équivalent à "File > Export > Export GeckoLib Model/Animations" quand Blockbench n'est pas sous la main.
"""
import base64, json, os, re, sys

src, name = sys.argv[1], sys.argv[2]
res = os.path.join(os.path.dirname(__file__), '..', 'src/main/resources/assets/astralpack')
d = json.load(open(src, encoding='utf-8'))


def num(v):
    try:
        f = float(v)
        return int(f) if f == int(f) else round(f, 5)
    except (TypeError, ValueError):
        return v


groups = {g['uuid']: g for g in d['groups']}
elements = {e['uuid']: e for e in d['elements']}
bones, cube_of = [], {}


def walk(children, parent):
    for c in children:
        if isinstance(c, dict):
            g = groups[c['uuid']]
            b = {'name': g['name'], 'pivot': [-num(g['origin'][0]), num(g['origin'][1]), num(g['origin'][2])]}
            if parent:
                b['parent'] = parent
            r = g.get('rotation', [0, 0, 0])
            if any(r):
                b['rotation'] = [-num(r[0]), -num(r[1]), num(r[2])]
            b['cubes'] = []
            bones.append(b)
            walk(c['children'], g['name'])
        else:
            cube_of.setdefault(parent, []).append(elements[c])


walk(d['outliner'], None)
for b in bones:
    for e in cube_of.get(b['name'], []):
        size = [round(e['to'][i] - e['from'][i], 5) for i in range(3)]
        cube = {'origin': [-num(e['to'][0]), num(e['from'][1]), num(e['from'][2])],
                'size': [num(s) for s in size]}
        r = e.get('rotation', [0, 0, 0])
        if any(r):
            o = e['origin']
            cube['pivot'] = [-num(o[0]), num(o[1]), num(o[2])]
            cube['rotation'] = [-num(r[0]), -num(r[1]), num(r[2])]
        if e.get('box_uv'):
            if 'uv_offset' in e:
                cube['uv'] = [num(x) for x in e['uv_offset']]
            else:  # offset déduit de la face nord : (u, v) = (offset_x + profondeur, offset_y + profondeur)
                n = e['faces']['north']['uv']
                cube['uv'] = [num(n[0] - size[2]), num(n[1] - size[2])]
        else:
            cube['uv'] = {f: {'uv': [num(v['uv'][0]), num(v['uv'][1])],
                              'uv_size': [num(v['uv'][2] - v['uv'][0]), num(v['uv'][3] - v['uv'][1])]}
                          for f, v in e['faces'].items() if v.get('uv') and v.get('texture') is not None}
        b['cubes'].append(cube)
    if not b['cubes']:
        del b['cubes']

vb = d.get('visible_box', [1, 1, 0])
geo = {'format_version': '1.12.0', 'minecraft:geometry': [{
    'description': {'identifier': 'geometry.' + d['model_identifier'],
                    'texture_width': d['resolution']['width'], 'texture_height': d['resolution']['height'],
                    'visible_bounds_width': vb[0], 'visible_bounds_height': vb[1],
                    'visible_bounds_offset': [0, vb[2], 0]},
    'bones': bones}]}

anims = {}
for a in d.get('animations', []):
    out = {}
    out['loop'] = {'loop': True, 'hold': 'hold_on_last_frame'}.get(a['loop'], False)
    out['animation_length'] = a['length']
    bones_a = {}
    for an in a.get('animators', {}).values():
        if an.get('type') != 'bone':
            continue
        chans = {}
        for k in an.get('keyframes', []):
            ch = k['channel']
            if ch not in ('rotation', 'position', 'scale'):
                continue
            p = k['data_points'][0]
            v = [num(p['x']), num(p['y']), num(p['z'])]
            if ch == 'rotation':
                v = [-v[0] if not isinstance(v[0], str) else v[0], -v[1] if not isinstance(v[1], str) else v[1], v[2]]
            elif ch == 'position' and not isinstance(v[0], str):
                v[0] = -v[0]
            if k.get('interpolation') in ('catmullrom', 'step'):
                v = {'post': v, 'lerp_mode': k['interpolation']}
            chans.setdefault(ch, []).append((k['time'], v))
        bd = {}
        for ch, kfs in chans.items():
            kfs.sort(key=lambda t: t[0])
            if len(kfs) == 1 and not isinstance(kfs[0][1], dict):
                bd[ch] = kfs[0][1]
            else:
                bd[ch] = {('%g' % t): v for t, v in kfs}
        if bd:
            bones_a[an['name']] = bd
    out['bones'] = bones_a
    anims[a['name']] = out

for sub, fname, data in (('geo', name + '.geo.json', geo),
                         ('animations', name + '.animation.json', {'format_version': '1.8.0', 'animations': anims})):
    with open(os.path.join(res, sub, fname), 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write('\n')

for t in d.get('textures', []):
    m = re.match(r'data:image/png;base64,(.*)', t.get('source', ''))
    if m:
        with open(os.path.join(res, 'textures/entity', name + '.png'), 'wb') as f:
            f.write(base64.b64decode(m.group(1)))
        break
print('bones:', len(bones), 'animations:', list(anims))
