#!/usr/bin/env python3
"""Inspect the exact AAB: identity, API, updater exclusion and 64-bit ELF alignment."""
import argparse,hashlib,json,struct,subprocess,zipfile,xml.etree.ElementTree as ET
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--bundle',required=True);p.add_argument('--bundletool',required=True);p.add_argument('--output',required=True);a=p.parse_args()
bundle=Path(a.bundle);subprocess.run(['java','-jar',a.bundletool,'validate','--bundle='+str(bundle)],check=True)
manifest=subprocess.check_output(['java','-jar',a.bundletool,'dump','manifest','--bundle='+str(bundle),'--module=base']).decode();root=ET.fromstring(manifest);ns='{http://schemas.android.com/apk/res/android}'
assert root.get('package')=='za.co.cttrains.app','Package identity changed'
assert int(root.find('uses-sdk').get(ns+'targetSdkVersion'))>=36,'Target SDK is too old'
assert root.find('application').get(ns+'debuggable','false')!='true','Release is debuggable'
permissions=[x.get(ns+'name') for x in root.findall('uses-permission')]
assert 'android.permission.REQUEST_INSTALL_PACKAGES' not in permissions,'Play bundle contains installer permission'
assert 'android.permission.ACCESS_BACKGROUND_LOCATION' not in permissions,'Unexpected background location permission'
libs=[]
with zipfile.ZipFile(bundle) as z:
 for name in z.namelist():
  if name.endswith('.dex'):
   raw=z.read(name)
   for forbidden in (b'Lza/co/cttrains/app/updates/UpdateRepository;',b'Lza/co/cttrains/app/updates/UpdateDownloadWorker;'): assert forbidden not in raw,'GitHub installer code present'
  if name.endswith('.so') and ('/arm64-v8a/' in name or '/x86_64/' in name):
   data=z.read(name);assert data[:4]==b'\x7fELF' and data[4]==2 and data[5]==1,'Invalid 64-bit library'
   phoff=struct.unpack_from('<Q',data,32)[0];size,count=struct.unpack_from('<HH',data,54);alignments=[]
   for i in range(count):
    off=phoff+i*size;kind=struct.unpack_from('<I',data,off)[0]
    if kind==1: alignments.append(struct.unpack_from('<Q',data,off+48)[0])
   assert alignments and min(alignments)>=16384,f'16 KB ELF alignment failed: {name}'
   libs.append({'file':name,'load_alignment':min(alignments)})
assert libs,'Expected MapLibre native libraries were not found'
report={'bundle':bundle.name,'sha256':hashlib.sha256(bundle.read_bytes()).hexdigest(),'package':'za.co.cttrains.app','targetSdk':36,'permission_check':'passed','installer_exclusion':'passed','elf_16kb_check':'passed','libraries':libs,'runtime_16kb_test':'pending','play_signing_and_console_upload':'pending'}
Path(a.output).parent.mkdir(parents=True,exist_ok=True);Path(a.output).write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
