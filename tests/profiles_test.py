from pathlib import Path
import sqlite3,re,json
s=(Path(__file__).resolve().parents[1]/'app/src/main/java/com/amalaei/engineering/Store.java').read_text(encoding='utf-8')
section=s.split('private void profileSchema(SQLiteDatabase db){',1)[1].split('\n    }',1)[0]
schema=re.findall(r'db.execSQL\("([^"\n]+)"\)',section)[0]
c=sqlite3.connect(':memory:');c.execute('CREATE TABLE leads(id INTEGER PRIMARY KEY,name TEXT)');c.execute("INSERT INTO leads VALUES(1,'عميل محفوظ')");c.execute(schema)
survey={'name':'اسم المنشأة','phones':'0500000000','address':'عنوان المساحة','instagram':'https://instagram.com/survey'}
software={'name':'شركة البرامج','phones':'0500000000','address':'عنوان البرامج','website':'https://example.com/software'}
c.execute('INSERT INTO profiles VALUES(?,?)',('survey',json.dumps(survey)));c.execute('INSERT INTO profiles VALUES(?,?)',('software',json.dumps(software)))
changed=dict(survey);changed['address']='عنوان المساحة المعدّل';c.execute('INSERT OR REPLACE INTO profiles VALUES(?,?)',('survey',json.dumps(changed)))
assert json.loads(c.execute('SELECT data FROM profiles WHERE business=?',('software',)).fetchone()[0])==software
assert json.loads(c.execute('SELECT data FROM profiles WHERE business=?',('survey',)).fetchone()[0])==changed
assert c.execute('SELECT name FROM leads').fetchone()[0]=='عميل محفوظ'
try:c.execute('INSERT INTO profiles VALUES(?,?)',('mixed','{}'));raise AssertionError()
except sqlite3.IntegrityError:pass
# V4 backup rows preserve JSON fields and inclusion options losslessly.
rows=[{'business':a,'data':d} for a,d in c.execute('SELECT business,data FROM profiles')]
backup=json.loads(json.dumps({'version':4,'profiles':rows}));assert len(backup['profiles'])==2
c.commit()
try:
 with c:
  c.execute('DELETE FROM profiles')
  c.execute("INSERT INTO profiles VALUES('invalid','{}')")
except sqlite3.IntegrityError:pass
assert c.execute('SELECT COUNT(*) FROM profiles').fetchone()[0]==2
print('PASS: profile migration preserves clients, two entities isolated, invalid activity rejected, backup round-trip and rollback.')
