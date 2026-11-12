"""Head close-ups of a .bbmodel with its own default texture. Usage: head_shots.py <model.bbmodel> <out.jpg>"""
import copy, json, os, re, subprocess, sys
from PIL import Image, ImageDraw
src, out = sys.argv[1], sys.argv[2]
RENDER = os.path.join(os.environ["LOCALAPPDATA"], "blockbench-mcp-headless", "render")
tmp = os.path.dirname(out)
m = json.load(open(src, encoding="utf-8"))
names = {g["uuid"]: g["name"] for g in m["groups"]}
best = None
def find(nodes, depth, path):
    global best
    for n in nodes:
        if isinstance(n, str):
            continue
        if re.match(r"^head$", names.get(n["uuid"], ""), re.I) and (best is None or depth < best[0]):
            best = (depth, n, path + [n])
        find(n["children"], depth + 1, path + [n])
find(m["outliner"], 0, [])
_, node, path = best
keep = set()
def collect(n):
    for c in n["children"]:
        keep.add(c) if isinstance(c, str) else collect(c)
collect(node)
h = copy.deepcopy(m)
h["elements"] = [e for e in h["elements"] if e["uuid"] in keep]
h["animations"] = []
chain = copy.deepcopy(node)
for anc in reversed(path[:-1]):
    chain = {"uuid": anc["uuid"], "isOpen": False, "children": [chain]}
h["outliner"] = [chain]
p = os.path.join(tmp, "_head.bbmodel")
json.dump(h, open(p, "w", encoding="utf-8"))
W, H = 640, 480
views = [(60, 5, "right side"), (300, 5, "left side"), (0, 10, "front"), (40, -35, "from below")]
sheet = Image.new("RGB", (W * 2, H * 2))
for i, (az, el, label) in enumerate(views):
    png = os.path.join(tmp, f"_head_{i}.png")
    subprocess.run(["node", "cli.mjs", p, "-o", png, "--width", str(W), "--height", str(H), f"--azimuth={az}",
                    f"--elevation={el}", "--lighting", "flat", "--no-shadows", "--ground", "none", "-q"], cwd=RENDER)
    im = Image.open(png).convert("RGB")
    ImageDraw.Draw(im).text((6, 4), label, fill=(255, 255, 255))
    sheet.paste(im, ((i % 2) * W, (i // 2) * H))
sheet.save(out, quality=90)
print([t["name"] for t in m["textures"] if t.get("use_as_default")], len(h["elements"]), "head cubes")
