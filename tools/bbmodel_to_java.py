#!/usr/bin/env python3
"""
bbmodel_to_java.py  -  converte um projeto Blockbench (.bbmodel, formato Bedrock)
em codigo Java para Minecraft 1.21.11 (Fabric / Mojang mappings).

Gera:
  src/client/java/<pkg>/client/model/MonkeyModelData.java   (LayerDefinition: cubos, pivos, UVs)
  src/client/java/<pkg>/client/anim/MonkeyAnimations.java   (todas as animacoes: idle, walk, eating...)
  src/main/resources/assets/<modid>/textures/entity/<name>.png (textura embutida no .bbmodel)

Uso:
  python3 tools/bbmodel_to_java.py tools/white_monkey.bbmodel            # gera tudo
  python3 tools/bbmodel_to_java.py tools/white_monkey.bbmodel --verify   # + teste matematico

Convencao de coordenadas (Bedrock/Blockbench  ->  ModelPart do Minecraft Java):
  x_j = -x_b      y_j = 24 - y_b      z_j = z_b           (rotacao propria de 180 graus em Y)
  rotacao (graus) : (rx, ry, rz)_j = (-rx, -ry, rz)      (ordem ZYX nos dois lados)
  posicao (anim)  : (x, y, z)_j    = (-x, -y, z)
  escala          : igual
"""
import argparse, base64, json, math, os, random, re, sys

DEFAULT_PKG = "com.whitemonkey"
DEFAULT_MODID = "white_monkey"


# ----------------------------------------------------------------------------- leitura
def load_bbmodel(path):
    d = json.load(open(path, encoding="utf-8"))
    el = {e["uuid"]: e for e in d["elements"]}
    gr = {g["uuid"]: g for g in d["groups"]}
    groups = []

    def walk(node, parent):
        g = gr[node["uuid"]]
        rec = {"name": g["name"], "origin": [float(v) for v in g["origin"]],
               "rotation": [float(v) for v in g.get("rotation", [0, 0, 0])],
               "parent": parent, "cubes": []}
        groups.append(rec)
        for c in node.get("children", []):
            if isinstance(c, str):
                e = el[c]
                if e.get("type", "cube") != "cube":
                    continue
                rec["cubes"].append({
                    "from": [float(v) for v in e["from"]], "to": [float(v) for v in e["to"]],
                    "origin": [float(v) for v in e.get("origin", [0, 0, 0])],
                    "rotation": [float(v) for v in e.get("rotation", [0, 0, 0])],
                    "uv": [int(round(v)) for v in e.get("uv_offset", [0, 0])],
                    "inflate": float(e.get("inflate", 0) or 0),
                    "mirror": bool(e.get("mirror_uv", False)),
                    "box_uv": bool(e.get("box_uv", True)),
                })
            else:
                walk(c, g["name"])

    for o in d["outliner"]:
        if isinstance(o, dict):
            walk(o, None)

    anims = {}
    for a in d.get("animations", []):
        loop = str(a.get("loop", "once")).lower() == "loop"
        bones = {}
        for _, an in a.get("animators", {}).items():
            ch = {}
            for kf in an.get("keyframes", []):
                dp = kf["data_points"][0]
                ch.setdefault(kf["channel"], []).append(
                    (float(kf["time"]), [float(dp["x"]), float(dp["y"]), float(dp["z"])]))
            for k in ch:
                ch[k].sort(key=lambda t: t[0])
            if ch:
                bones[an["name"]] = ch
        anims[a["name"]] = {"length": float(a["length"]), "loop": loop, "bones": bones}

    tex = None
    if d.get("textures"):
        src = d["textures"][0].get("source", "")
        if src.startswith("data:"):
            tex = base64.b64decode(src.split(",", 1)[1])
    res = d.get("resolution", {"width": 64, "height": 64})
    return {"groups": groups, "anims": anims, "texture": tex,
            "tex_w": int(res["width"]), "tex_h": int(res["height"])}


# ----------------------------------------------------------------------------- conversao
def jpt(p):
    return (-p[0], 24.0 - p[1], p[2])


def convert_model(m):
    """devolve lista de 'parts' em espaco Java (ordem topologica)."""
    gmap = {g["name"]: g for g in m["groups"]}
    parts = []
    for g in m["groups"]:
        pv = jpt(g["origin"])
        ppv = jpt(gmap[g["parent"]]["origin"]) if g["parent"] else (0.0, 0.0, 0.0)
        part = {"name": g["name"], "parent": g["parent"], "is_group": True,
                "offset": tuple(pv[i] - ppv[i] for i in range(3)),
                "rot": (-g["rotation"][0], -g["rotation"][1], g["rotation"][2]),
                "cubes": []}
        parts.append(part)
        counter = 0
        for c in g["cubes"]:
            f, t, o = c["from"], c["to"], c["origin"]
            rotated = any(abs(v) > 1e-9 for v in c["rotation"])
            if rotated:
                counter += 1
                cp = jpt(o)
                sub = {"name": f"{g['name']}_c{counter}", "parent": g["name"], "is_group": False,
                       "offset": tuple(cp[i] - pv[i] for i in range(3)),
                       "rot": (-c["rotation"][0], -c["rotation"][1], c["rotation"][2]),
                       "cubes": []}
                parts.append(sub)
                target, ref = sub, o
            else:
                target, ref = part, g["origin"]
            target["cubes"].append({
                "x": ref[0] - t[0], "y": ref[1] - t[1], "z": f[2] - ref[2],
                "w": t[0] - f[0], "h": t[1] - f[1], "d": t[2] - f[2],
                "u": c["uv"][0], "v": c["uv"][1], "inflate": c["inflate"], "mirror": c["mirror"]})
    return parts


def convert_anims(m):
    out = {}
    for name, a in m["anims"].items():
        bones = {}
        for bone, chans in a["bones"].items():
            conv = {}
            for ch, keys in chans.items():
                if ch == "rotation":
                    conv["rotation"] = [(t, [-v[0], -v[1], v[2]]) for t, v in keys]
                elif ch == "position":
                    conv["position"] = [(t, [-v[0], -v[1], v[2]]) for t, v in keys]
                elif ch == "scale":
                    conv["scale"] = [(t, list(v)) for t, v in keys]
            bones[bone] = conv
        out[name] = {"length": a["length"], "loop": a["loop"], "bones": bones}
    return out


# ----------------------------------------------------------------------------- amostragem (igual ao Java)
def catmull(p0, p1, p2, p3, t):
    return 0.5 * (2 * p1 + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t
                  + (-p0 + 3 * p1 - 3 * p2 + p3) * t ** 3)


def sample(keys, time):
    n = len(keys)
    if n == 1 or time <= keys[0][0]:
        return list(keys[0][1])
    if time >= keys[-1][0]:
        return list(keys[-1][1])
    i = 0
    while i < n - 2 and time >= keys[i + 1][0]:
        i += 1
    t0, t1 = keys[i][0], keys[i + 1][0]
    dlt = (time - t0) / (t1 - t0) if t1 > t0 else 0.0
    a, b = keys[max(i - 1, 0)][1], keys[i][1]
    c, d = keys[i + 1][1], keys[min(i + 2, n - 1)][1]
    return [catmull(a[k], b[k], c[k], d[k], dlt) for k in range(3)]


# ----------------------------------------------------------------------------- verificacao (FK)
def rz_ry_rx(rx, ry, rz):
    rx, ry, rz = map(math.radians, (rx, ry, rz))
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    Rx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    Ry = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    Rz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    mm = lambda A, B: [[sum(A[i][k] * B[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    return mm(Rz, mm(Ry, Rx))


def mv(R, v):
    return [sum(R[i][k] * v[k] for k in range(3)) for i in range(3)]


def add(a, b):
    return [a[i] + b[i] for i in range(3)]


def sub(a, b):
    return [a[i] - b[i] for i in range(3)]


def corners(lo, hi):
    return [[x, y, z] for x in (lo[0], hi[0]) for y in (lo[1], hi[1]) for z in (lo[2], hi[2])]


def pose_at(anim, time, bone):
    pos, rot, scl = [0, 0, 0], [0, 0, 0], [1, 1, 1]
    if anim and bone in anim["bones"]:
        ch = anim["bones"][bone]
        tt = time % anim["length"] if anim["loop"] else min(time, anim["length"])
        if "position" in ch: pos = sample(ch["position"], tt)
        if "rotation" in ch: rot = sample(ch["rotation"], tt)
        if "scale" in ch: scl = sample(ch["scale"], tt)
    return pos, rot, scl


def verify(m, parts, anims_b, anims_j):
    gmap = {g["name"]: g for g in m["groups"]}
    pmap = {p["name"]: p for p in parts}
    worst = 0.0
    tests = [(None, 0.0)] + [(n, t) for n in anims_b for t in (0.0, 0.17, 0.5, 0.83, 1.31, 1.9)]
    for an, time in tests:
        ab = anims_b.get(an) if an else None
        aj = anims_j.get(an) if an else None
        for g in m["groups"]:
            for ci, c in enumerate(g["cubes"]):
                # ---- Blockbench (espaco B)
                for corner in corners(c["from"], c["to"]):
                    p = list(corner)
                    if any(abs(v) > 1e-9 for v in c["rotation"]):
                        o = c["origin"]
                        p = add(o, mv(rz_ry_rx(*c["rotation"]), sub(p, o)))
                    name = g["name"]
                    while name:
                        gg = gmap[name]
                        pos, rot, scl = pose_at(ab, time, name)
                        rr = [gg["rotation"][i] + rot[i] for i in range(3)]
                        piv = gg["origin"]
                        q = [(p[i] - piv[i]) * scl[i] for i in range(3)]
                        p = add(add(piv, pos), mv(rz_ry_rx(*rr), q))
                        name = gg["parent"]
                    pb = p
                    # ---- Java (espaco J) - recria os cubos a partir de 'parts'
                    # localiza o part que contem este cubo
                    rotated = any(abs(v) > 1e-9 for v in c["rotation"])
                    if rotated:
                        k = sum(1 for cc in g["cubes"][:ci + 1] if any(abs(v) > 1e-9 for v in cc["rotation"]))
                        part = pmap[f"{g['name']}_c{k}"]
                        cube = part["cubes"][0]
                    else:
                        part = pmap[g["name"]]
                        k = sum(1 for cc in g["cubes"][:ci] if not any(abs(v) > 1e-9 for v in cc["rotation"]))
                        cube = part["cubes"][k]
                    lo = [cube["x"], cube["y"], cube["z"]]
                    hi = [cube["x"] + cube["w"], cube["y"] + cube["h"], cube["z"] + cube["d"]]
                    # cantos em espaco local do part (mesmo indice de canto que no lado B)
                    # mapeamento de canto B -> J: x espelhado, y espelhado
                    bx = 1 if corner[0] == c["to"][0] and c["to"][0] != c["from"][0] else 0
                    by = 1 if corner[1] == c["to"][1] and c["to"][1] != c["from"][1] else 0
                    bz = 1 if corner[2] == c["to"][2] and c["to"][2] != c["from"][2] else 0
                    q = [hi[0] if bx == 0 else lo[0], hi[1] if by == 0 else lo[1], lo[2] if bz == 0 else hi[2]]
                    cur = part
                    while cur:
                        pos, rot, scl = pose_at(aj, time, cur["name"]) if cur["is_group"] else ([0, 0, 0], [0, 0, 0], [1, 1, 1])
                        rr = [cur["rot"][i] + rot[i] for i in range(3)]
                        off = add(list(cur["offset"]), pos)
                        q = add(off, mv(rz_ry_rx(*rr), [q[i] * scl[i] for i in range(3)]))
                        cur = pmap[cur["parent"]] if cur["parent"] else None
                    back = [-q[0], 24 - q[1], q[2]]
                    err = max(abs(back[i] - pb[i]) for i in range(3))
                    worst = max(worst, err)
    return worst


# ----------------------------------------------------------------------------- emissao Java
def f(v):
    s = f"{v:.5f}".rstrip("0").rstrip(".")
    if s in ("", "-0"): s = "0"
    return s + "F"


def emit_model(parts, pkg, tex_w, tex_h):
    L = []
    L.append(f"package {pkg}.client.model;\n")
    L.append("import net.minecraft.client.model.geom.PartPose;")
    L.append("import net.minecraft.client.model.geom.builders.CubeDeformation;")
    L.append("import net.minecraft.client.model.geom.builders.CubeListBuilder;")
    L.append("import net.minecraft.client.model.geom.builders.LayerDefinition;")
    L.append("import net.minecraft.client.model.geom.builders.MeshDefinition;")
    L.append("import net.minecraft.client.model.geom.builders.PartDefinition;\n")
    L.append("/** ARQUIVO GERADO por tools/bbmodel_to_java.py - nao edite a mao; reexporte o .bbmodel. */")
    L.append("public final class MonkeyModelData {")
    L.append("    private MonkeyModelData() {}\n")
    # caminhos dos grupos
    pmap = {p["name"]: p for p in parts}
    L.append("    /** Caminho (hierarquia) de cada osso/grupo do Blockbench dentro do ModelPart raiz. */")
    L.append("    public static final String[][] BONE_PATHS = {")
    for p in parts:
        if not p["is_group"]:
            continue
        path, cur = [], p
        while cur:
            path.append(cur["name"]); cur = pmap[cur["parent"]] if cur["parent"] else None
        path.reverse()
        L.append("        {" + ", ".join(f'"{x}"' for x in path) + "},")
    L.append("    };\n")
    L.append("    public static LayerDefinition createBodyLayer() {")
    L.append("        MeshDefinition mesh = new MeshDefinition();")
    L.append("        PartDefinition root = mesh.getRoot();")
    for p in parts:
        var = "p_" + p["name"]
        parent = "root" if not p["parent"] else "p_" + p["parent"]
        cb = ["CubeListBuilder.create()"]
        for c in p["cubes"]:
            s = f".texOffs({c['u']}, {c['v']})"
            if c["mirror"]: s += ".mirror()"
            args = f"{f(c['x'])}, {f(c['y'])}, {f(c['z'])}, {f(c['w'])}, {f(c['h'])}, {f(c['d'])}"
            if abs(c["inflate"]) > 1e-9:
                args += f", new CubeDeformation({f(c['inflate'])})"
            s += f".addBox({args})"
            if c["mirror"]: s += ".mirror(false)"
            cb.append(s)
        ox, oy, oz = p["offset"]
        rx, ry, rz = [math.radians(v) for v in p["rot"]]
        if any(abs(v) > 1e-9 for v in p["rot"]):
            pose = f"PartPose.offsetAndRotation({f(ox)}, {f(oy)}, {f(oz)}, {f(rx)}, {f(ry)}, {f(rz)})"
        else:
            pose = f"PartPose.offset({f(ox)}, {f(oy)}, {f(oz)})"
        L.append(f"        PartDefinition {var} = {parent}.addOrReplaceChild(\"{p['name']}\",")
        L.append("                " + "".join(cb) + ",")
        L.append(f"                {pose});")
    L.append(f"        return LayerDefinition.create(mesh, {tex_w}, {tex_h});")
    L.append("    }")
    L.append("}")
    return "\n".join(L) + "\n"


def emit_anims(anims_j, pkg):
    L = []
    L.append(f"package {pkg}.client.anim;\n")
    L.append("import java.util.Map;\n")
    L.append("/** ARQUIVO GERADO por tools/bbmodel_to_java.py - nao edite a mao; reexporte o .bbmodel. */")
    L.append("public final class MonkeyAnimations {")
    L.append("    private MonkeyAnimations() {}\n")
    names = []
    for name, a in anims_j.items():
        const = re.sub(r"[^A-Za-z0-9]", "_", name).upper()
        names.append((name, const))
        L.append(f"    public static final MonkeyClip {const} = new MonkeyClip(\"{name}\", {f(a['length'])}, "
                 f"{'true' if a['loop'] else 'false'}, new MonkeyClip.Track[] {{")
        for bone, chans in a["bones"].items():
            for ch, cid in (("position", "POSITION"), ("rotation", "ROTATION"), ("scale", "SCALE")):
                if ch not in chans: continue
                keys = chans[ch]
                times = ", ".join(f(t) for t, _ in keys)
                vals = ", ".join("{" + f(v[0]) + ", " + f(v[1]) + ", " + f(v[2]) + "}" for _, v in keys)
                L.append(f"        new MonkeyClip.Track(\"{bone}\", MonkeyClip.{cid},")
                L.append(f"                new float[] {{{times}}},")
                L.append(f"                new float[][] {{{vals}}}),")
        L.append("    });\n")
    L.append("    public static final Map<String, MonkeyClip> ALL = Map.of(")
    L.append(",\n".join(f'        "{n}", {c}' for n, c in names))
    L.append("    );")
    L.append("}")
    return "\n".join(L) + "\n"


# ----------------------------------------------------------------------------- main
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("bbmodel")
    ap.add_argument("--root", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
    ap.add_argument("--package", default=DEFAULT_PKG)
    ap.add_argument("--modid", default=DEFAULT_MODID)
    ap.add_argument("--texture-name", default="white_monkey")
    ap.add_argument("--verify", action="store_true")
    a = ap.parse_args()

    m = load_bbmodel(a.bbmodel)
    parts = convert_model(m)
    anims_j = convert_anims(m)

    if a.verify:
        err = verify(m, parts, m["anims"], anims_j)
        print(f"[verify] erro maximo (Blockbench x Java) = {err:.3e} pixels")
        if err > 1e-3:
            print("[verify] FALHOU"); sys.exit(1)
        print("[verify] OK")

    pkgdir = a.package.replace(".", "/")
    paths = {
        "model": os.path.join(a.root, "src/client/java", pkgdir, "client/model/MonkeyModelData.java"),
        "anim": os.path.join(a.root, "src/client/java", pkgdir, "client/anim/MonkeyAnimations.java"),
        "tex": os.path.join(a.root, "src/main/resources/assets", a.modid, "textures/entity", a.texture_name + ".png"),
    }
    for p in paths.values():
        os.makedirs(os.path.dirname(p), exist_ok=True)
    open(paths["model"], "w", encoding="utf-8").write(emit_model(parts, a.package, m["tex_w"], m["tex_h"]))
    open(paths["anim"], "w", encoding="utf-8").write(emit_anims(anims_j, a.package))
    if m["texture"]:
        open(paths["tex"], "wb").write(m["texture"])
    print("gerado:", *[os.path.relpath(p, a.root) for p in paths.values()], sep="\n  ")
    print("animacoes:", ", ".join(anims_j))


if __name__ == "__main__":
    main()
