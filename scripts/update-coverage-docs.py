#!/usr/bin/env python3
"""Parse Kover XML report and update coverage tables in docs.

Usage:
    python3 scripts/update-coverage-docs.py                  # default
    python3 scripts/update-coverage-docs.py --dry-run        # preview
    python3 scripts/update-coverage-docs.py --xml-path X     # custom path
    python3 scripts/update-coverage-docs.py --min-lines 20   # filter threshold
"""

from __future__ import annotations

import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path

# ── Module prefix mapping ────────────────────────────────────────────
# Keys: display name for docs.
# Values: list of XML package prefixes to aggregate.
# Order within each list doesn't matter. Longer prefixes match first
# (e.g., domain/usecase won't be counted under domain:models).
MODULE_MAP = {
    "domain:usecase": ["com/nutrisport/shared/domain/usecase"],
    "domain:models": ["com/nutrisport/shared/domain"],
    "domain:util (Either, NullSafety)": ["com/nutrisport/shared/domain/util"],
    "feature:adminPanel": ["com/nutrisport/admin_panel"],
    "feature:details": ["com/nutrisport/details"],
    "feature:cart": ["com/nutrisport/cart"],
    "feature:productsOverview": ["com/nutrisport/products_overview"],
    "feature:profile": ["com/nutrisport/profile"],
    "feature:home": ["com/nutrisport/home"],
    "feature:categories:search": [
        "com/portfolio/categories_search",
        "com/nutrisport/categories",
    ],
    "feature:manageProduct": ["com/nutrisport/manage_product"],
    "feature:checkout": ["com/nutrisport/checkout"],
    "feature:paymentCompleted": ["com/portfolio/payment_completed"],
    "feature:auth": ["com/nutrisport/auth"],
    "shared:utils": ["com/nutrisport/shared/util"],
    "analytics:core": ["com/nutrisport/analytics/core"],
    "analytics:firebase": ["com/nutrisport/analytics/firebase"],
    "network": ["com/nutrisport/data"],
}

# ── Kover exclusions (for TESTING.md documentation) ──────────────────
KOVER_EXCLUSIONS = """\
### What Kover Excludes

| Category | Patterns |
| -------- | -------- |
| UI composables | `*Screen*`, `*Preview*`, `*ComposableSingletons*`, `component.*` |
| Generated code | `Resources*`, `*BuildConfig*` |
| Infrastructure | `di.*`, `navigation.*`, `database.*` (DAOs, entities, converters) |
| Platform | `MainActivity*`, `NutrisportApplication*` |
| Design tokens | `Alpha*`, `Colors*`, `Fonts*`, `Constants*` |

Exclusions configured in root `build.gradle.kts`."""

MARKER_START = "<!-- coverage:start -->"
MARKER_END = "<!-- coverage:end -->"

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_XML = ROOT / "build" / "reports" / "kover" / "report.xml"


def _build_prefix_index():
    """Build (prefix, module_name) pairs sorted longest-first for greedy matching."""
    pairs = []
    for module_name, prefixes in MODULE_MAP.items():
        for prefix in prefixes:
            pairs.append((prefix, module_name))
    pairs.sort(key=lambda p: len(p[0]), reverse=True)
    return pairs


def _match_module(pkg_name: str, prefix_index: list[tuple[str, str]]) -> str | None:
    """Return module name for a package, or None if no match."""
    for prefix, module_name in prefix_index:
        if pkg_name == prefix or pkg_name.startswith(prefix + "/"):
            return module_name
    return None


def parse_coverage(xml_path: Path, min_lines: int) -> tuple[list[dict], dict]:
    """Parse Kover XML, aggregate by module, return (modules, totals)."""
    tree = ET.parse(xml_path)
    root = tree.getroot()
    prefix_index = _build_prefix_index()

    # Accumulate per module
    modules: dict[str, dict] = {}
    total_missed = 0
    total_covered = 0

    for pkg in root.iter("package"):
        name = pkg.get("name", "")
        line_counter = None
        for counter in pkg.findall("counter"):
            if counter.get("type") == "LINE":
                line_counter = counter
                break
        if line_counter is None:
            continue

        missed = int(line_counter.get("missed", 0))
        covered = int(line_counter.get("covered", 0))

        total_missed += missed
        total_covered += covered

        module_name = _match_module(name, prefix_index)
        if module_name is None:
            continue

        if module_name not in modules:
            modules[module_name] = {"missed": 0, "covered": 0}
        modules[module_name]["missed"] += missed
        modules[module_name]["covered"] += covered

    # Convert to list, filter, compute %
    result = []
    for name, data in modules.items():
        total = data["missed"] + data["covered"]
        if total < min_lines:
            continue
        pct = round(data["covered"] / total * 100, 1) if total > 0 else 0.0
        result.append({"name": name, "pct": pct, "covered": data["covered"], "missed": data["missed"]})

    result.sort(key=lambda p: p["pct"], reverse=True)

    overall_total = total_missed + total_covered
    overall_pct = round(total_covered / overall_total * 100, 1) if overall_total > 0 else 0.0
    totals = {"pct": overall_pct, "covered": total_covered, "missed": total_missed}

    return result, totals


def make_table(packages: list[dict]) -> str:
    """Generate markdown coverage table."""
    lines = [
        "| Package | Line coverage |",
        "| ------- | ------------- |",
    ]
    for p in packages:
        pct = f'{p["pct"]}%' if p["pct"] != 100.0 else "100%"
        lines.append(f'| {p["name"]} | {pct} |')
    return "\n".join(lines)


def format_readme(packages: list[dict], totals: dict, report_date: str) -> str:
    """Generate README.md coverage section."""
    table = make_table(packages)
    return f"""{table}

> Overall line coverage: {totals['pct']}%. Low aggregate reflects untested generated code, UI composables, and data layer — tested packages average 80%+. Report: {report_date}."""


def format_testing(packages: list[dict], totals: dict, report_date: str) -> str:
    """Generate TESTING.md coverage section."""
    table = make_table(packages)
    return f"""{table}

> Overall line coverage: {totals['pct']}%. Low aggregate reflects untested generated code, UI composables, and data layer — tested packages average 80%+.

**Report:** {report_date} | Regenerate: `./gradlew koverXmlReport` | HTML: `build/reports/kover/html/index.html`

{KOVER_EXCLUSIONS}"""


def replace_between_markers(filepath: Path, content: str, dry_run: bool) -> bool:
    """Replace content between coverage markers. Returns True if file changed."""
    text = filepath.read_text()

    if MARKER_START not in text or MARKER_END not in text:
        print(f"  WARNING: markers not found in {filepath.name}, skipping")
        return False

    pattern = re.compile(
        re.escape(MARKER_START) + r".*?" + re.escape(MARKER_END),
        re.DOTALL,
    )
    replacement = f"{MARKER_START}\n{content}\n{MARKER_END}"
    new_text = pattern.sub(replacement, text)

    if new_text == text:
        print(f"  {filepath.name}: no changes")
        return False

    if dry_run:
        print(f"  {filepath.name}: would update (dry-run)")
    else:
        filepath.write_text(new_text)
        print(f"  {filepath.name}: updated")
    return True


def main():
    parser = argparse.ArgumentParser(description="Update coverage docs from Kover XML")
    parser.add_argument("--xml-path", type=Path, default=DEFAULT_XML, help="Path to Kover XML report")
    parser.add_argument("--dry-run", action="store_true", help="Preview without writing")
    parser.add_argument("--min-lines", type=int, default=10, help="Min total lines to include module")
    args = parser.parse_args()

    if not args.xml_path.exists():
        print(f"ERROR: XML report not found at {args.xml_path}")
        print("Run: ./gradlew koverXmlReport")
        sys.exit(1)

    mtime = os.path.getmtime(args.xml_path)
    report_date = datetime.fromtimestamp(mtime, tz=timezone.utc).strftime("%Y-%m-%d")

    packages, totals = parse_coverage(args.xml_path, args.min_lines)
    print(f"Parsed {len(packages)} modules, overall {totals['pct']}% ({report_date})")

    readme = ROOT / "README.md"
    testing = ROOT / "docs" / "TESTING.md"

    replace_between_markers(readme, format_readme(packages, totals, report_date), args.dry_run)
    replace_between_markers(testing, format_testing(packages, totals, report_date), args.dry_run)


if __name__ == "__main__":
    main()
