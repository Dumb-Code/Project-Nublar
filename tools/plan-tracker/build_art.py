"""Read docs/plan/art-list.md into one row per art asset.

build_tasks.py calls parse_art() and writes art.json beside tasks.json.
Run this file alone to print the counts per thing, for a check against the
Summary table of the Art list:
    python tools/plan-tracker/build_art.py
"""
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ART = ROOT / "docs/plan/art-list.md"
FEATURE_RE = re.compile(r"M0\.\w+|F\d\.\d+|BH\.\d|R0\d")
SPLIT_RE = re.compile(r"(?<!\\)\|")

# Status groups use the column names of the Art list Summary table.
DONE, CHECK, IMPORT, EXPORT, MISSING = (
    "Done", "To check in game", "Ready to import", "Needs export or conversion", "Placeholder or missing")
SHARED, LEAD, NO_ART, NOT_BETA, BLOCKED = (
    "Shared, to confirm", "Only if the lead approves", "No art needed", "Not in Beta", "Blocked")
COUNTED = {DONE, CHECK, IMPORT, EXPORT, MISSING, SHARED}

# Diet per Beta species, from the feature specs (F2.0 and F2.4). The Gallimimus is an omnivore.
DIET = {"Tyrannosaurus": "Carnivore", "Velociraptor": "Carnivore", "Dilophosaurus": "Carnivore",
        "Triceratops": "Herbivore", "Brachiosaurus": "Herbivore", "Gallimimus": "Omnivore"}


def sex_of(asset, used_for):
    """Sex that a species asset is for: Male, Female, Both, or empty when the row names none."""
    male = bool(re.search(r"\bmales?\b", asset, re.I))
    female = bool(re.search(r"\bfemales?\b", asset, re.I))
    if (male and female) or re.search(r"both (genders|sexes)", asset + " " + used_for, re.I):
        return "Both"
    return "Male" if male else "Female" if female else ""


def eras_of(art_set):
    """Marks that a skin or model set heading names, in film order: JP, Site B, JP3, JW.

    Site B is Isla Sorna, so a Sorna set takes the Site B mark beside its film mark.
    """
    eras = []
    if re.search(r"\bJP\b|Nublar", art_set):
        eras.append("JP")
    if "TLW" in art_set or "Sorna" in art_set:
        eras.append("Site B")
    if re.search(r"JP3|Sorna", art_set):
        eras.append("JP3")
    if re.search(r"\bJWD?\b", art_set):
        eras.append("JW")
    return ", ".join(eras)


def clean(text):
    text = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", text)
    text = text.replace("**", "").replace("`", "").replace("\\|", "|")
    return re.sub(r"\s+", " ", text).strip()


def cap(text, limit):
    return text if len(text) <= limit else text[: limit - 1].rstrip() + "…"


def group_of(status, thing):
    s = status.lower()
    if thing.startswith("Guns"):
        return BLOCKED
    if s.startswith("not in beta"):
        return NOT_BETA
    if s.startswith("no art needed"):
        return NO_ART
    if "only if" in s or "lead approves" in s or "lead decides" in s:
        return LEAD
    if s.startswith("shared"):
        return SHARED
    if s.startswith("done"):
        return DONE
    if s.startswith("to check"):
        return CHECK
    if s.startswith("ready to import"):
        return IMPORT
    if s.startswith(("needs export", "needs conversion")):
        return EXPORT
    if s.startswith(("missing", "placeholder")):
        return MISSING
    return "Other"


def code_of(thing, used):
    words = re.findall(r"[A-Za-z0-9]+", thing)
    code = (words[0][:4] if len(words) == 1 else "".join(w[0] for w in words[:4])).upper()
    base, n = code, 2
    while code in used and used[code] != thing:
        code, n = f"{base}{n}", n + 1
    used[code] = thing
    return code


def parse_art():
    rows, used, counters = [], {}, Counter()
    thing = art_type = art_set = ""
    in_tables = False
    for n, line in enumerate(ART.read_text(encoding="utf-8").splitlines(), 1):
        if line.startswith("## "):
            thing, art_type, art_set = clean(line[3:]), "", ""
            in_tables = in_tables or thing == "Tyrannosaurus"
        elif line.startswith("### "):
            head = clean(line[4:])
            art_type, _, art_set = (part.strip() for part in head.partition(":"))
        elif in_tables and line.startswith("|"):
            cells = [clean(c) for c in SPLIT_RE.split(line.strip().strip("|"))]
            if len(cells) != 5 or cells[0] in ("Asset", "Asset group") or set(cells[0]) <= set("-: "):
                continue
            asset, used_for, status, feature, where = cells
            name = re.sub(r" \(.*$", "", thing)
            code = code_of(name, used)
            counters[code] += 1
            features = FEATURE_RE.findall(feature)
            group = group_of(status, thing)
            rows.append({
                "id": f"{code}-{counters[code]:03d}", "thing": name, "art_type": art_type or "Assets",
                "set": art_set, "era": eras_of(art_set), "diet": DIET.get(name, ""),
                "sex": sex_of(asset, used_for) if name in DIET else "", "asset": cap(asset, 200), "used_for": cap(used_for, 240),
                "status": group, "status_text": cap(status, 160), "counted": group in COUNTED,
                "package": features[0] if features else "", "features": ", ".join(features) or feature,
                "maker": "Agent" if "Agent makes it" in where else "Artist",
                "where": cap(where, 240), "source": f"docs/plan/art-list.md:{n}",
            })
    return rows


if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8")
    art = parse_art()
    per = defaultdict(Counter)
    for r in art:
        per[r["thing"]][r["status"]] += 1
    cols = [DONE, CHECK, IMPORT, EXPORT, MISSING, SHARED, LEAD, NO_ART, NOT_BETA, BLOCKED, "Other"]
    print(len(art), "rows;", sum(r["counted"] for r in art), "counted")
    print("thing".ljust(24), *[c[:9].rjust(9) for c in cols])
    for thing, c in per.items():
        print(thing[:24].ljust(24), *[str(c[k]).rjust(9) for k in cols])
    total = Counter(r["status"] for r in art)
    print("TOTAL".ljust(24), *[str(total[k]).rjust(9) for k in cols])
    for r in art:
        if r["status"] == "Other" or not r["package"]:
            print("CHECK", r["id"], "|", r["status_text"][:60], "|", r["features"], "|", r["source"])
    print(Counter(r["package"] for r in art).most_common())
    print(Counter(r["maker"] for r in art))
