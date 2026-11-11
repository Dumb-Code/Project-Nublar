"""Build the task rows for the plan tracker page.

Reads docs/plan/engineering-backlog.md and the detailed task plans in
docs/superpowers/plans/, merges status.json, and writes tasks.json and
packages.json into the output folder.

Run from the repo root:
    python tools/plan-tracker/build_tasks.py <output-folder>

Status rules, in order:
  1. status.json "tasks" names the row id.
  2. The backlog line is ticked "- [x]".
  3. status.json "packages" gives a status for the package and row kind.
  4. Otherwise "To do".
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BACKLOG = ROOT / "docs/plan/engineering-backlog.md"
STATUS = Path(__file__).with_name("status.json")

PKG_RE = re.compile(r"^### ((?:M0\.\w+|F\d\.\d+|BH\.\d|R0\d)): (.+)$")
MS_RE = re.compile(r"^## (M\d|Beta hardening|Release 1)\b\.? ?(.*)$")
CHECK_RE = re.compile(r"^- \[( |x|X)\] (.+)$")
TASK_RE = re.compile(r"^### Task (\d+)[^:]*: (.+)$")
MS_ID = {"Beta hardening": "BH", "Release 1": "R1"}
MS_NAME = {
    "M0": "M0 Port and foundation", "M1": "M1 Fossil to egg", "M2": "M2 Dinosaurs",
    "M3": "M3 Park", "M4": "M4 World and vehicles", "BH": "Beta hardening", "R1": "Release 1",
}


def clean(text):
    text = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", text)
    text = text.replace("**", "").replace("`", "")
    return re.sub(r"\s+", " ", text).strip()


def cap(text, limit=320):
    return text if len(text) <= limit else text[: limit - 1].rstrip() + "…"


def expand_deps(raw, pkg_ids, ms_of, me):
    """Turn a 'Depends on' line into package ids and open decision ids.

    Only the first sentence counts: later sentences describe parts of a package.
    A milestone name means all of its packages and its exit gate.
    """
    deps, blocked = [], []
    blocked += re.findall(r"(D\d+) \(open\)", raw)
    raw = re.split(r"\.\s", raw)[0]
    for a, b in re.findall(r"(F\d\.\d+) to (F\d\.\d+)", raw):
        m, lo, hi = a[1], int(a.split(".")[1]), int(b.split(".")[1])
        deps += [f"F{m}.{n}" for n in range(lo, hi + 1)]
    deps += re.findall(r"M0\.\w+|F\d\.\d+|BH\.\d|R0\d", raw)
    for ms in re.findall(r"\bM\d\b(?!\.\w)", raw):
        deps += [p for p in pkg_ids if ms_of[p] == ms] + [f"{ms}.GATE"]
    if "Beta released" in raw:
        deps += ["BH.2", "BH.GATE"]
    seen = []
    for d in deps:
        if (d in pkg_ids or d.endswith(".GATE")) and d != me and d not in seen:
            seen.append(d)
    return seen, blocked


def parse_backlog():
    lines = BACKLOG.read_text(encoding="utf-8").splitlines()
    packages, rows = [], []
    ms = pkg = None
    section = ""
    in_gate = False
    counters = {}
    scope = []
    i = 0

    def add(owner, kind, text, line_no, ticked=False):
        key = (owner, kind)
        counters[key] = counters.get(key, 0) + 1
        letter = {"Step": "S", "Acceptance": "A", "Gate": "G"}[kind]
        rows.append({
            "id": f"{owner}-{letter}{counters[key]:02d}",
            "milestone": MS_NAME[ms], "package": owner, "kind": kind,
            "section": section, "task": cap(clean(text)), "ticked": ticked,
            "source": f"docs/plan/engineering-backlog.md:{line_no}",
        })

    while i < len(lines):
        line = lines[i]
        n = i + 1
        m = MS_RE.match(line)
        if line.startswith("## "):
            pkg, in_gate, section = None, False, ""
            ms = (MS_ID.get(m.group(1), m.group(1))) if m else None
        elif ms and line.startswith("### "):
            in_gate = line.startswith("### Exit gate")
            section = "Exit gate" if in_gate else ""
            p = PKG_RE.match(line)
            pkg = None
            if p:
                pkg = {"id": p.group(1), "title": clean(p.group(2)), "milestone": MS_NAME[ms],
                       "ms": ms, "size": "", "depends_raw": "", "depends_first": "", "line": n}
                packages.append(pkg)
        elif ms and line.startswith("#### "):
            section = clean(line[5:])
            in_gate = False
        elif ms and line.startswith("**Exit gate"):
            in_gate, section = True, "Exit gate"
            rest = clean(line.split(":**", 1)[1]) if ":**" in line else ""
            if rest and not rest.endswith(":"):
                add(f"{ms}.GATE", "Gate", rest[0].upper() + rest[1:], n)
        elif ms and in_gate and line.startswith(("**", "Work packages")):
            in_gate = False
        elif ms and in_gate and re.match(r"^\d+\. ", line):
            add(f"{ms}.GATE", "Gate", line.split(". ", 1)[1], n)
        elif ms and pkg and re.match(r"^- \*\*Acceptance[^*]*:\*\* ", line):
            add(pkg["id"], "Acceptance", line.split(":** ", 1)[1], n)
        elif ms and pkg and line.startswith("- **Size:**"):
            pkg["size"] = clean(line.split(":**", 1)[1]).rstrip(".")
        elif ms and pkg and line.startswith("- **Depends on:**"):
            raw = clean(line.split(":**", 1)[1])
            j = i + 1
            first = raw
            while j < len(lines) and lines[j].startswith("  - "):
                first = first or clean(lines[j][4:])
                raw += " " + clean(lines[j][4:])
                j += 1
            pkg["depends_raw"], pkg["depends_first"] = raw.strip(), first
        elif ms and line.startswith("- "):
            c = CHECK_RE.match(line)
            text = c.group(2) if c else line[2:]
            if clean(text).endswith(":"):
                kids, j = [], i + 1
                while j < len(lines) and lines[j].startswith("  "):
                    if lines[j].strip().startswith("- "):
                        kids.append(clean(lines[j].strip()[2:]))
                    j += 1
                text = clean(text) + " " + " · ".join(kids)
            if in_gate:
                add(f"{ms}.GATE", "Gate", text, n)
            elif pkg and section.startswith("Acceptance"):
                add(pkg["id"], "Acceptance", text, n, bool(c and c.group(1) != " "))
            elif pkg and c:
                add(pkg["id"], "Step", text, n, c.group(1) != " ")
            elif pkg and section == "Scope":
                scope.append((pkg["id"], text, n))
        i += 1
    # A package with no checkbox steps takes its Scope bullets as steps.
    with_steps = {r["package"] for r in rows if r["kind"] == "Step"}
    by_id = {p["id"]: p for p in packages}
    for owner, text, n in scope:
        if owner not in with_steps:
            counters[(owner, "Step")] = counters.get((owner, "Step"), 0) + 1
            rows.append({"id": f'{owner}-S{counters[(owner, "Step")]:02d}', "milestone": by_id[owner]["milestone"],
                         "package": owner, "kind": "Step", "section": "Scope", "task": cap(clean(text)),
                         "ticked": False, "source": f"docs/plan/engineering-backlog.md:{n}"})
    have = {r["package"] for r in rows}
    for p in packages:
        if p["id"] not in have:
            rows.append({"id": f'{p["id"]}-S01', "milestone": p["milestone"], "package": p["id"], "kind": "Step",
                         "section": "", "task": "Write this package's scope and task plan. The plan has none yet.",
                         "ticked": False, "source": f'docs/plan/engineering-backlog.md:{p["line"]}'})
    return packages, rows


def parse_plan(pkg, ms, rel_path, label):
    out = []
    for n, line in enumerate((ROOT / rel_path).read_text(encoding="utf-8").splitlines(), 1):
        t = TASK_RE.match(line)
        if t:
            out.append({
                "id": f"{pkg}-{label}{int(t.group(1)):02d}", "milestone": MS_NAME[ms], "package": pkg,
                "kind": "Plan task", "section": f"Task {t.group(1)}", "task": cap(clean(t.group(2))),
                "ticked": False, "source": f"{rel_path}:{n}", "num": int(t.group(1)),
            })
    return out


def main(out_dir):
    cfg = json.loads(STATUS.read_text(encoding="utf-8"))
    packages, rows = parse_backlog()
    ids = [p["id"] for p in packages]
    ms_of = {p["id"]: p["ms"] for p in packages}
    for plan in cfg["plans"]:
        rows += parse_plan(plan["package"], ms_of[plan["package"]], plan["path"], plan["label"])

    pk = cfg["packages"]
    for r in rows:
        st = cfg["tasks"].get(r["id"])
        note = ""
        if isinstance(st, dict):
            st, note = st["status"], st.get("note", "")
        if not st and r["ticked"]:
            st = "Done"
        if not st and r["kind"] == "Plan task":
            for plan in cfg["plans"]:
                if r["id"].startswith(f'{plan["package"]}-{plan["label"]}'):
                    num = r["num"]
                    st = plan["default"]
                    for rule in plan.get("ranges", []):
                        if rule["from"] <= num <= rule["to"]:
                            st = rule["status"]
        if not st:
            rule = pk.get(r["package"], {})
            st = rule.get(r["kind"], rule.get("all", "To do"))
        r["status"], r["note"] = st, note
        r.pop("ticked", None)
        r.pop("num", None)

    for p in packages:
        deps, blocked = expand_deps(p.pop("depends_first"), ids, ms_of, p["id"])
        p["depends_on"] = ", ".join(deps)
        p["blocked_by"] = ", ".join(blocked)
        p["note"] = pk.get(p["id"], {}).get("note", "")
        p["source"] = f'docs/plan/engineering-backlog.md:{p.pop("line")}'
        p.pop("ms")
    gates = sorted({r["package"] for r in rows if r["kind"] == "Gate"})
    order = {name: k for k, name in enumerate(MS_NAME.values())}
    for g in gates:
        ms = g.split(".")[0]
        members = [p["id"] for p in packages if p["milestone"] == MS_NAME[ms]]
        packages.append({"id": g, "title": "Exit gate (playtest)", "milestone": MS_NAME[ms], "size": "",
                         "depends_raw": "Every package of the milestone", "depends_on": ", ".join(members),
                         "blocked_by": "", "note": "", "source": ""})
    packages.sort(key=lambda p: order[p["milestone"]])

    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    (out / "tasks.json").write_text(json.dumps(rows, ensure_ascii=False), encoding="utf-8")
    (out / "packages.json").write_text(json.dumps(packages, ensure_ascii=False), encoding="utf-8")
    by = {}
    for r in rows:
        by[r["status"]] = by.get(r["status"], 0) + 1
    print(len(packages), "packages,", len(rows), "tasks", by)
    for p in packages:
        mine = [r for r in rows if r["package"] == p["id"]]
        print(f'{p["id"]:9} {len(mine):4}  deps: {p["depends_on"]}  {("blocked: " + p["blocked_by"]) if p["blocked_by"] else ""}')


if __name__ == "__main__":
    main(sys.argv[1])
