from pathlib import Path
import sqlite3,re,json
s=(Path(__file__).resolve().parents[1]/'app/src/main/java/com/amalaei/engineering/Store.java').read_text(encoding='utf-8')
create=s.split('public void onCreate(SQLiteDatabase db) {',1)[1].split('extend(db);',1)[0]
extend=s.split('private void extend(SQLiteDatabase db) {',1)[1].split('\n    }',1)[0]
sql=lambda x:re.findall(r'db.execSQL\("([^"\n]+)"\)',x)
db=sqlite3.connect(':memory:')
for q in sql(create):db.execute(q)
db.execute("INSERT INTO leads(name,phone,business,notes,followUp) VALUES(?,?,?,?,?)",('عميل قديم','+966541234567','survey','ملاحظات قديمة','2026-10-06'))
db.execute("INSERT INTO messages(fingerprint,sender,body,at,source) VALUES('x','عميل','نص قديم',1,'مشاركة')")
for q in sql(extend):db.execute(q)
row=db.execute('SELECT name,notes,followTime,requestDetails FROM leads').fetchone()
assert row==('عميل قديم','ملاحظات قديمة','09:00','{}')
assert db.execute('SELECT COUNT(*) FROM messages').fetchone()[0]==1
# Legacy JSON restores omit new fields; schema defaults apply.
db.execute("INSERT INTO leads(name,business) VALUES('استعادة قديمة','software')")
assert db.execute("SELECT followTime,requestDetails FROM leads WHERE name='استعادة قديمة'").fetchone()==('09:00','{}')
# Separate request detail objects survive switching activities.
details={'survey':{'model':'M25'},'software':{'program':'ENGINEX'}}
db.execute('UPDATE leads SET requestDetails=? WHERE id=1',(json.dumps(details),))
assert json.loads(db.execute('SELECT requestDetails FROM leads WHERE id=1').fetchone()[0])==details
# Duplicate imported events do not multiply the timeline.
for _ in range(2):db.execute("INSERT OR IGNORE INTO events(leadId,kind,body,at,externalKey) VALUES(1,'call','اتصال',10,'call|1|10')")
assert db.execute('SELECT COUNT(*) FROM events').fetchone()[0]==1
# A corrupt restore rolls back the preceding deletes.
db.commit()
try:
 with db:
  db.execute('DELETE FROM events');db.execute('DELETE FROM leads')
  db.execute('INSERT INTO leads(name) VALUES(NULL)')
except sqlite3.IntegrityError:pass
assert db.execute('SELECT COUNT(*) FROM leads').fetchone()[0]==2
assert db.execute('SELECT COUNT(*) FROM events').fetchone()[0]==1
print('PASS: v1-to-v2 schema preserves customers/messages, legacy restore defaults, activity details, event deduplication and rollback.')
