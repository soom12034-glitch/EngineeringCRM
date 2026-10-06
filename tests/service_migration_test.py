import sqlite3,re,json
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/'app/src/main/java/com/amalaei/engineering/Store.java').read_text()
sql=re.search(r'ALTER TABLE leads ADD COLUMN requestType[^"\n]+',s).group()
db=sqlite3.connect(':memory:');db.execute('CREATE TABLE leads(id INTEGER PRIMARY KEY,name TEXT,business TEXT,stage INTEGER,requestDetails TEXT,followUp TEXT)')
data=json.dumps({'survey':{'model':'TS','serialNumber':'123','calibrationDate':'2027-10-05'}})
db.execute('INSERT INTO leads VALUES(1,?,?,?,?,?)',('عميل محفوظ','survey',3,data,'2026-10-06'));db.execute(sql)
assert db.execute('SELECT stage,requestDetails,followUp,requestType FROM leads WHERE id=1').fetchone()==(3,data,'2026-10-06','sales')
# Restoring old rows uses the new column default without dropping original data.
db.execute('INSERT INTO leads(id,name,business,stage) VALUES(2,?,?,?)',('عميل قديم','software',2))
assert db.execute('SELECT requestType FROM leads WHERE id=2').fetchone()[0]=='sales'
for t in ('support','maintenance','calibration'):
 db.execute('UPDATE leads SET requestType=? WHERE id=1',(t,));assert db.execute('SELECT name,requestDetails FROM leads WHERE id=1').fetchone()==('عميل محفوظ',data)
try:db.execute("UPDATE leads SET requestType='invalid' WHERE id=1");raise AssertionError()
except sqlite3.IntegrityError:pass
backup=json.loads(json.dumps([dict(zip([c[0] for c in db.execute('SELECT * FROM leads').description],row)) for row in db.execute('SELECT * FROM leads')]))
assert backup[0]['requestType']=='calibration' and backup[0]['requestDetails']==data
assert 'if(oldV<6)serviceSchema(db)' in s
print('PASS: v5→v6 keeps customer/stage/device data, older backups default safely, invalid types rejected, classification backs up.')
