#!/usr/bin/env python3
from pathlib import Path
import sqlite3,base64,hashlib,xml.etree.ElementTree as ET,re,tempfile,shutil,time
root=Path(__file__).resolve().parent
res=root/'app/src/main/res'
for f in res.rglob('*.xml'): ET.parse(f)
allids=set()
for f in (res/'layout').glob('*.xml'):
    allids.update(re.findall(r'android:id="@\+id/(\w+)"',f.read_text('utf-8')))
resources={'drawable':{x.stem for x in (res/'drawable').glob('*.xml')},'layout':{x.stem for x in (res/'layout').glob('*.xml')}}
for kind in ('color','style','string'):
    resources[kind]={v.get('name') for f in (res/'values').glob('*.xml') for v in ET.parse(f).getroot() if v.tag==kind}
for file in res.rglob('*.xml'):
    for kind,name in re.findall(r'@(drawable|layout|color|string|style)/(\w+)',file.read_text('utf-8')):
        assert name in resources[kind],f'missing {kind}/{name} in {file}'
for file in (root/'app/src/main/java/com/example/studentsupport').glob('*.java'):
    text=file.read_text('utf-8')
    missing=set(re.findall(r'R\.id\.(\w+)',text))-allids
    assert not missing,f'missing IDs {missing} in {file}'
    for kind,name in re.findall(r'(?<!android\.)R\.(drawable|layout|color|string|style)\.(\w+)',text):
        assert name in resources[kind],f'missing {kind}/{name} in {file}'
for path in (root/'database/student_support.db',root/'app/src/main/assets/student_support.db'):
    with sqlite3.connect(path) as db:
        assert db.execute('pragma integrity_check').fetchone()[0]=='ok'
        assert not db.execute('pragma foreign_key_check').fetchall()
        assert db.execute('pragma user_version').fetchone()[0]==4
        for table in ('student_profiles','favorites','cart_items','vouchers','package_extras','package_ratings','learning_resources','learning_progress','quiz_questions','quiz_attempts','notifications','chat_messages','user_flags'):
            assert db.execute("select count(*) from sqlite_master where type='table' and name=?",(table,)).fetchone()[0]==1,table
        for mail,password in (('admin@demo.local','Admin@123'),('sv01@demo.local','Student@123')):
            salt,hashed=db.execute('select password_salt,password_hash from users where email=?',(mail,)).fetchone()
            assert base64.b64encode(hashlib.pbkdf2_hmac('sha256',password.encode(),base64.b64decode(salt),120000,32)).decode()==hashed
with tempfile.TemporaryDirectory() as d:
    tmp=Path(d)/'copy.db';shutil.copyfile(root/'database/student_support.db',tmp)
    db=sqlite3.connect(tmp);db.execute('pragma foreign_keys=on')
    uid=db.execute("select id from users where email='sv01@demo.local'").fetchone()[0]
    pid,amount=db.execute('select id,price from packages limit 1').fetchone()
    db.execute('insert into cart_items(user_id,package_id) values(?,?)',(uid,pid))
    db.execute('insert into favorites(user_id,package_id) values(?,?)',(uid,pid))
    db.execute('insert into student_profiles(user_id,university,major,cohort) values(?,?,?,?)',(uid,'Đại học Demo','CNTT','K25'))
    oid=db.execute("insert into orders(user_id,package_id,amount,status,created_at) values(?,?,?,'PENDING',?)",(uid,pid,amount,int(time.time()*1000))).lastrowid
    db.execute('delete from cart_items where user_id=?',(uid,))
    db.execute('insert into chat_messages(user_id,role,message,created_at) values(?,?,?,?)',(uid,'USER','Xin chào',int(time.time()*1000)))
    # The notes UI was removed; validate main business CRUD with package records.
    pid_new=db.execute('insert into packages(name,category,description,price,duration_days,is_active) values(?,?,?,?,?,1)',
                       ('Khóa học thử','CNTT','Dữ liệu kiểm tra SQLite',125000,30)).lastrowid
    assert db.execute('select price from packages where id=?',(pid_new,)).fetchone()[0]==125000
    db.execute('update packages set price=? where id=?',(130000,pid_new))
    assert db.execute('select price from packages where id=?',(pid_new,)).fetchone()[0]==130000
    db.execute('delete from packages where id=?',(pid_new,))
    assert db.execute('select count(*) from packages where id=?',(pid_new,)).fetchone()[0]==0
    db.commit();assert not db.execute('pragma foreign_key_check').fetchall()
    assert db.execute('select status from orders where id=?',(oid,)).fetchone()[0]=='PENDING'
    db.close()
print('PASS: XML, resource refs, SQLite v4, demo login hashes, cart/order/chat/package CRUD')
print('NOTE: Android APK compilation and emulator tests NOT performed.')
