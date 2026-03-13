#!/usr/bin/env python3
"""
JCT Stack Formatter
-------------------
Reads a raw JCT stack capture (the array value of the "stack" field) and
formats it as a readable, aligned call sequence grouped by class.

Usage:
  # pipe directly
  echo '[A.foo(), B.bar()]' | python3 format_stack.py

  # from a file
  python3 format_stack.py stack.txt

  # copy the raw bracket string and pipe via xclip/xsel
  xclip -o | python3 format_stack.py

Output example:
   #   package                               class                           method
  ───────────────────────────────────────────────────────────────────────────────────
   1   o.b.s.u.ldap                          LdapConnectionFactory         .inititialize(LdapConnectionConfigurationDTO)
   2                                                                        .connect()
   3   o.b.s.u.ldap                          LdapConnectionConfigurationDTO .getLdapServer1()
  ...
"""

import re
import sys


# ---------------------------------------------------------------------------
# Parsing
# ---------------------------------------------------------------------------

def parse_stack(raw: str) -> list[str]:
    """Parse '[a.B.foo(x.Y), ...]' into a list of FQN method strings."""
    raw = raw.strip()
    if raw.startswith("["):
        raw = raw[1:]
    if raw.endswith("]"):
        raw = raw[:-1]

    entries: list[str] = []
    depth = 0
    buf: list[str] = []

    for ch in raw:
        if ch == "(":
            depth += 1
            buf.append(ch)
        elif ch == ")":
            depth -= 1
            buf.append(ch)
        elif ch == "," and depth == 0:
            entry = "".join(buf).strip()
            if entry:
                entries.append(entry)
            buf = []
        else:
            buf.append(ch)

    if buf:
        entry = "".join(buf).strip()
        if entry:
            entries.append(entry)

    return entries


def split_entry(entry: str) -> tuple[str, str, str, str]:
    """
    Split 'a.b.c.ClassName.method(args)' into (pkg, cls, method, raw_args).
    Handles inner classes like 'Outer$Inner'.
    """
    paren = entry.index("(") if "(" in entry else len(entry)
    raw_args = entry[paren:]
    fqn = entry[:paren]

    # last segment = method name, second-to-last = class, rest = package
    parts = fqn.rsplit(".", 2)
    if len(parts) == 3:
        pkg, cls, method = parts
    elif len(parts) == 2:
        pkg, cls, method = "", parts[0], parts[1]
    else:
        pkg, cls, method = "", "", parts[0]

    return pkg, cls, method, raw_args


# ---------------------------------------------------------------------------
# Formatting helpers
# ---------------------------------------------------------------------------

def abbreviate_pkg(pkg: str) -> str:
    """'otto.b2b.security.utils.ldap' -> 'o.b.s.u.ldap'"""
    parts = pkg.split(".")
    if len(parts) <= 2:
        return pkg
    return ".".join(p[0] for p in parts[:-1]) + "." + parts[-1]


def shorten_args(raw_args: str) -> str:
    """'(a.b.c.Foo, x.y.Bar)' -> '(Foo, Bar)'"""
    return re.sub(r"[\w]+\.(\w+)", r"\1", raw_args)


# ---------------------------------------------------------------------------
# Rendering
# ---------------------------------------------------------------------------

def render(entries: list[str]) -> str:
    # Collect rows: (num, short_pkg, cls, method_with_args, same_cls_as_prev)
    rows = []
    prev_cls = None
    prev_pkg = None

    for i, entry in enumerate(entries, 1):
        pkg, cls, method, raw_args = split_entry(entry)
        short_pkg = abbreviate_pkg(pkg)
        short_args = shorten_args(raw_args)
        same = (cls == prev_cls and pkg == prev_pkg)
        rows.append((i, short_pkg, cls, f".{method}{short_args}", same))
        prev_cls = cls
        prev_pkg = pkg

    # Column widths
    w_pkg = max(len(r[1]) for r in rows)
    w_cls = max(len(r[2]) for r in rows)
    w_num = len(str(len(rows)))

    header_pkg   = "package"
    header_cls   = "class"
    header_meth  = "method"

    w_pkg = max(w_pkg, len(header_pkg))
    w_cls = max(w_cls, len(header_cls))

    sep = "  "
    rule_len = w_num + 2 + w_pkg + len(sep) + w_cls + len(sep) + 40

    lines = []
    # Header
    lines.append(
        f"  {'#':>{w_num}}  "
        f"{header_pkg:<{w_pkg}}{sep}"
        f"{header_cls:<{w_cls}}{sep}"
        f"{header_meth}"
    )
    lines.append("  " + "\u2500" * (rule_len - 2))

    for num, short_pkg, cls, meth, same in rows:
        if same:
            pkg_col = ""
            cls_col = ""
        else:
            pkg_col = short_pkg
            cls_col = cls

        lines.append(
            f"  {num:>{w_num}}  "
            f"{pkg_col:<{w_pkg}}{sep}"
            f"{cls_col:<{w_cls}}{sep}"
            f"{meth}"
        )

    return "\n".join(lines)


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    if len(sys.argv) > 1:
        with open(sys.argv[1]) as f:
            raw = f.read()
    else:
        raw = sys.stdin.read()

    if not raw.strip():
        print("No input. Pipe a stack string or pass a filename.", file=sys.stderr)
        sys.exit(1)

    entries = parse_stack(raw)
    print(render(entries))


if __name__ == "__main__":
    main()

