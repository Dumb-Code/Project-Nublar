"""Print each in-game model's adult size in blocks at SIZE gene 0: the rest pose in world space, times the species'
ADULT_SCALE_OFFSET read from its entity class.  Usage: ingame_size.py <repo-root>"""
import glob, os, re, sys
import numpy as np

sys.argv, repo = ["x"], sys.argv[1]
ns = {}
exec(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "ingame_vs_artist.py")).read().split("\ng, a = corners")[0], ns)
TARGET = {"triceratops": 3.0, "brachiosaurus": 12.5, "dilophosaurus": 3.0}
for path in sorted(glob.glob(os.path.join(repo, "common/src/main/resources/assets/projectnublar/geckolib/models/entity/*.geo.json"))):
    sp = os.path.basename(path)[:-9]
    cls = "".join(w.capitalize() for w in sp.split("_")) + "Entity.java"
    src = [p for p in glob.glob(os.path.join(repo, "common/src/main/java/**", cls), recursive=True)]
    if not src:
        continue
    off = float(re.search(r"ADULT_SCALE_OFFSET = ([0-9.]+)F", open(src[0], encoding="utf-8").read()).group(1))
    pts = np.vstack([v[0] for v in ns["corners"](path).values() if len(v[0])])
    k = off / 16
    top, low = pts[:, 1].max() * k, pts[:, 1].min() * k
    width, length = (pts[:, 0].max() - pts[:, 0].min()) * k, (pts[:, 2].max() - pts[:, 2].min()) * k
    t = TARGET.get(sp)
    need = f" | target {t}: offset {off * t / top:.3f}" if t else ""
    print(f"{sp}: offset {off} -> top {top:.2f}, feet at {low:.2f}, width {width:.2f}, length {length:.2f} blocks{need}")
