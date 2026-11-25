#!/usr/bin/env python3
"""Kiểm tra cơ bản mã nguồn, các tài nguyên và SQLite. Không thay thế kiểm thử build Android."""
from pathlib import Path
import base64, hashlib, re, sqlite3, xml.etree.ElementTree as ET
p=Path(__file__).resolve().parent
res=p/'app/src/main/res'
files=list(res.rglob('*.xml'))
for file in files: ET.parse(file)
assert len(files)>=30, f"Thiếu XML: {len(files)}"
resource_names={}
for kind in ('drawable','layout'):
    resource_names[kind]={f.stem for f in (res/kind).glob('*.xml')}
for kind in ('color','string','style'):
    resource_names[kind]=set()
    for file in (res/'values').glob('*.xml'):
        for item in ET.parse(file).getroot():
            if item.tag==kind and 'name' in item.attrib:resource_names[kind].add(item.attrib['name'])
all_id=set()
for f in (res/'layout').glob('*.xml'):
    all_id.update(re.findall(r'android:id="@\+id/(\w+)"',f.read_text('utf-8')))
for f in files:
    for kind,name in re.findall(r'@(drawable|layout|color|string|style)/(\w+)',f.read_text('utf-8')):
        assert name in resource_names[kind],f'{f} references missing {kind}/{name}'
for f in (p/'app/src/main/java/com/example/studentsupport').glob('*.java'):
    t=f.read_text('utf-8')
    assert not (set(re.findall(r'R\.id\.(\w+)',t))-all_id),f'Missing UI id in {f}'
    for kind,name in re.findall(r'(?<!android\.)R\.(drawable|layout|color|string|style)\.(\w+)',t):
        assert name in resource_names[kind],f'Missing {kind}/{name} in {f}'
for path in (p/'database/student_support.db',p/'app/src/main/assets/student_support.db'):
    with sqlite3.connect(path) as db:
        assert db.execute('pragma integrity_check').fetchone()[0]=='ok'
        assert db.execute('pragma foreign_key_check').fetchall()==[]
        for email,password in (('admin@demo.local','Admin@123'),('sv01@demo.local','Student@123')):
            row=db.execute('select password_salt,password_hash from users where email=?',(email,)).fetchone()
            assert row is not None,f'Missing sample user {email}'
            salt,hashed=row
            actual=base64.b64encode(hashlib.pbkdf2_hmac('sha256',password.encode(),base64.b64decode(salt),120_000,32)).decode()
            assert actual==hashed,f'Incorrect demo password {email}'
        assert db.execute('select count(*) from packages where is_active=1').fetchone()[0]>=4
print(f'PASS: {len(files)} XML, {len(all_id)} UI IDs, SQLite OK, two demo accounts OK')
print('NOTE: This does NOT perform Android APK compilation or emulator tests.')
