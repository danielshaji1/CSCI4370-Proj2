#!/usr/bin/env python3
"""Build an explicitly labeled draft or a final archive after team artifacts are supplied."""
import argparse
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

root = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser()
parser.add_argument('--draft', action='store_true')
args = parser.parse_args()
required = ['pom.xml', 'Readme.txt', 'database_setup.sql', 'dml.sql', 'ER_diagram.png']
missing = [name for name in required if not (root / name).is_file()]
if not args.draft:
    for name in ['deliverables/demo.mp4', 'deliverables/contribution_form.pdf']:
        if not (root / name).is_file() or (root / name).stat().st_size == 0:
            missing.append(name)
    if 'PENDING: Replace this section' in (root / 'Readme.txt').read_text():
        missing.append('team names and actual contributions in Readme.txt')
if missing:
    raise SystemExit('Missing required deliverables: ' + ', '.join(missing))

items = required + ['README.md', 'schema.sql', 'update_database.sql']
for folder in ['src', 'scripts', 'docs', 'deliverables']:
    if (root / folder).exists():
        items.extend(str(p.relative_to(root)) for p in (root / folder).rglob('*') if p.is_file()
                     and '__pycache__' not in p.parts and p.suffix != '.class' and p.name != '.DS_Store')
archive = root / ('submission-draft.zip' if args.draft else 'submission.zip')
with ZipFile(archive, 'w', ZIP_DEFLATED) as output:
    for name in sorted(set(items)):
        output.write(root / name, 'microblogging/' + name)
    if args.draft:
        output.writestr('microblogging/DRAFT_NOTICE.txt',
                        'SOURCE AND ARTIFACT DRAFT. Add the narrated demo, signed official form, team contributions,\n'
                        'and export the actual demo database before creating the final submission.\n')
print(f'Created {archive.name} ({archive.stat().st_size:,} bytes)')
