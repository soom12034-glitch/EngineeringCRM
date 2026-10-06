from pathlib import Path
import sqlite3,re,json
root=Path(__file__).resolve().parents[1]
s=(root/'app/src/main/java/com/amalaei/engineering/Store.java').read_text(encoding='utf-8')
base=s.split('public void onCreate(SQLiteDatabase db) {',1)[1].split('extend(db);',1)[0]
extend=s.split('private void extend(SQLiteDatabase db) {',1)[1].split('\n    }',1)[0]
ads=s.split('private void advertisingSchema(SQLiteDatabase db){',1)[1].split('\n    }',1)[0]
def sql(text):return re.findall(r'db.execSQL\("([^"\n]+)"\)',text)
c=sqlite3.connect(':memory:')
for q in sql(base)+sql(extend):c.execute(q)
c.execute("INSERT INTO leads(name,business,notes) VALUES('عميل قديم','survey','ملاحظة')")
for q in sql(ads):c.execute(q)
assert c.execute('SELECT name,notes FROM leads').fetchone()==('عميل قديم','ملاحظة')
c.execute("INSERT INTO posts(business,title,body,createdAt) VALUES('survey','M25','إعلان جهاز',1)")
c.execute("INSERT INTO posts(business,title,body,createdAt,plannedDate) VALUES('software','ENGINEX','إعلان برنامج',2,'2026-10-05')")
assert c.execute('SELECT title FROM posts WHERE business=?',('survey',)).fetchall()==[('M25',)]
assert c.execute("SELECT title FROM posts WHERE business=? AND plannedDate<>'' AND plannedDate<=?",('software','2026-10-05')).fetchall()==[('ENGINEX',)]
try:c.execute("INSERT INTO posts(business,title,createdAt) VALUES('mixed','invalid',1)");raise AssertionError('invalid activity accepted')
except sqlite3.IntegrityError:pass
c.execute("INSERT INTO postShares(postId,at) VALUES(1,3)");c.execute('UPDATE posts SET lastPreparedAt=3 WHERE id=1')
assert c.execute('SELECT title FROM posts WHERE lastPreparedAt=0').fetchall()==[('ENGINEX',)]
# Duplicating content does not reuse the opening history.
c.execute("INSERT INTO posts(business,title,body,image,createdAt) SELECT business,title||' copy',body,image,4 FROM posts WHERE id=1")
assert c.execute('SELECT lastPreparedAt FROM posts WHERE id=3').fetchone()[0]==0
c.commit()
try:
 with c:
  c.execute('DELETE FROM postShares');c.execute('DELETE FROM posts')
  c.execute("INSERT INTO posts(business,title,createdAt) VALUES('wrong','bad',1)")
except sqlite3.IntegrityError:pass
assert c.execute('SELECT COUNT(*) FROM posts').fetchone()[0]==3
assert c.execute('SELECT COUNT(*) FROM postShares').fetchone()[0]==1
# Real path rule used by the image reader accepts only content-addressed image basenames.
media=(root/'app/src/main/java/com/amalaei/engineering/PostMedia.java').read_text(encoding='utf-8')
pattern=re.search(r'name.matches\("([^"\n]+)"\)',media).group(1).replace('\\\\','\\')
assert re.fullmatch(pattern,'a'*64+'.png')
for name in ['../clients.db','a'*64+'.svg','/etc/passwd','a'*64+'.png/../clients.db','a'*64+'.png%2F..']:
 assert not re.fullmatch(pattern,name)
print('PASS: v2-v3 preserves clients, activity isolation, planning filter, invalid activity rejected, duplicate history reset, transactional rollback and image path confinement.')
