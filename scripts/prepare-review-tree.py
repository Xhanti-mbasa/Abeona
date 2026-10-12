"""Prepare a GitHub tree delta locally; this does not publish or send credentials."""
import subprocess,json,pathlib
base={};known=set()
for row in subprocess.check_output(['git','ls-tree','-r','HEAD']).decode().splitlines():
 meta,path=row.split('\t',1);mode,kind,sha=meta.split();base[path]=(mode,sha);known.add(sha)
current={}
for row in subprocess.check_output(['git','ls-files','--stage']).decode().splitlines():
 meta,path=row.split('\t',1);mode,sha,stage=meta.split();current[path]=(mode,sha)
entries=[]
for path,(mode,sha) in base.items():
 if path not in current: entries.append({'path':path,'mode':mode,'type':'blob','sha':None})
for path,(mode,sha) in current.items():
 if base.get(path)==(mode,sha): continue
 value={'path':path,'mode':mode,'type':'blob'}
 if sha in known: value['sha']=sha
 else: value['content']=pathlib.Path(path).read_text()
 entries.append(value)
pathlib.Path('/tmp/abeona-tree.json').write_text(json.dumps({'base':subprocess.check_output(['git','rev-parse','HEAD^{tree}']).decode().strip(),'parent':subprocess.check_output(['git','rev-parse','HEAD']).decode().strip(),'entries':entries}))
print(len(entries),'tree changes prepared')
