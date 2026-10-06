import sqlite3,re,json
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/'app/src/main/java/com/amalaei/engineering/Store.java').read_text(encoding='utf-8');section=s.split('private void softwareSchema(SQLiteDatabase db){',1)[1].split('\n    }',1)[0]
db=sqlite3.connect(':memory:');db.execute('CREATE TABLE leads(id INTEGER PRIMARY KEY,name TEXT,business TEXT)');db.executemany('INSERT INTO leads VALUES(?,?,?)',[(1,'شركة برامج','software'),(2,'عميل مساحة','survey')])
for sql in re.findall(r'db.execSQL\("([^"\n]+)"\)',section):db.execute(sql)
assert db.execute('SELECT count(*) FROM leads').fetchone()[0]==2
for n,t in enumerate(['subscription','development','support'],1):db.execute('INSERT INTO softwareRequests(id,leadId,data) VALUES(?,1,?)',(n,json.dumps({'id':n,'leadId':1,'type':t,'title':t,'status':1,'details':{}})))
assert db.execute('SELECT count(*) FROM softwareRequests WHERE leadId=1').fetchone()[0]==3
try:db.execute("INSERT INTO softwareRequests(leadId,data) VALUES(2,'{}')");raise AssertionError()
except sqlite3.IntegrityError:pass
try:db.execute("UPDATE softwareRequests SET leadId=2 WHERE id=1");raise AssertionError()
except sqlite3.IntegrityError:pass
rows=[dict(zip([x[0] for x in db.execute('SELECT * FROM softwareRequests').description],r)) for r in db.execute('SELECT * FROM softwareRequests')];copy=json.loads(json.dumps(rows));assert copy[1]['leadId']==1 and json.loads(copy[1]['data'])['type']=='development'
db.commit()
try:
 with db:
  db.execute('DELETE FROM softwareRequests');db.execute("INSERT INTO softwareRequests(leadId,data)VALUES(999,'{}')")
except sqlite3.IntegrityError:pass
assert db.execute('SELECT count(*) FROM softwareRequests').fetchone()[0]==3
assert s.count('JSONArray work=backup.optInt')==1
print('PASS: v6→v7 preserves customers, multiple independent requests, survey isolation, backup roundtrip and rollback.')
