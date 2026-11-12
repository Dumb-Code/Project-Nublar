"""Write a geo.json in the layout the mod's files use (tabs, one cube per line), so a tool edit gives a small diff."""
import json


def num(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, float) and v.is_integer():
        return str(int(v))
    return json.dumps(v)


def inline(v):
    if isinstance(v, dict):
        return "{" + ", ".join(f"{json.dumps(k)}: {inline(x)}" for k, x in v.items()) + "}"
    if isinstance(v, list):
        return "[" + ", ".join(inline(x) for x in v) + "]"
    return num(v) if isinstance(v, (int, float)) else json.dumps(v)


def block(v, depth):
    t = "\t" * depth
    if isinstance(v, dict):
        rows = []
        for k, x in v.items():
            if k in ("cubes", "bones", "minecraft:geometry", "locators") and isinstance(x, (list, dict)) and x:
                if k == "cubes":
                    body = ",\n".join(t + "\t\t" + inline(c) for c in x)
                    rows.append(f'{t}\t"{k}": [\n{body}\n{t}\t]')
                elif isinstance(x, list):
                    body = ",\n".join(block(c, depth + 2) for c in x)
                    rows.append(f'{t}\t"{k}": [\n{body}\n{t}\t]')
                else:
                    rows.append(f'{t}\t"{k}": ' + block(x, depth + 1).lstrip())
            elif k == "description":
                rows.append(f'{t}\t"{k}": ' + block(x, depth + 1).lstrip())
            else:
                rows.append(f'{t}\t"{k}": {inline(x)}')
        return t + "{\n" + ",\n".join(rows) + "\n" + t + "}"
    return t + inline(v)


def dumps(geo):
    return block(geo, 0)
