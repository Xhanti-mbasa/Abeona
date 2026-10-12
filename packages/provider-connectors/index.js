'use strict';
const axios=require('axios');
const providers=Object.freeze({
 PRASA:{id:'PRASA',name:'PRASA rail via CT Trains',official:false,capabilities:['routes','stops','schedules','fares','disruptions'],liveVehicles:false,source:'https://cttrains.co.za'},
 MYCITI:{id:'MYCITI',name:'MyCiTi',official:true,capabilities:['routes','stops','schedules','fares'],liveVehicles:false,source:'https://www.myciti.org.za'},
 GOLDEN_ARROW:{id:'GOLDEN_ARROW',name:'Golden Arrow',official:true,capabilities:['routes','schedules','fares','surveyedStops'],liveVehicles:false,source:'https://www.gabs.co.za',limitations:['Surveyed stop route membership is unverified','Published timed stops must be confirmed']}
});
const pending=new Map(),hostGates=new Map(),health=new Map();
function coalesce(key,fn) { if(pending.has(key)) return pending.get(key);if(pending.size>=128) return Promise.reject(new Error('Provider queue is full'));const p=Promise.resolve().then(fn).finally(()=>pending.delete(key));pending.set(key,p);return p; }
const wait=ms=>new Promise(r=>setTimeout(r,ms));
async function readPage(url,{request=axios.get,minimumIntervalMs=1100,retryDelayMs=1100}={}) {
 let current=new URL(url);if(current.protocol!=='https:' || current.hostname!=='cttrains.co.za' || current.username || current.password) throw new Error('Unapproved source URL');
 return coalesce('page:'+url,async()=>{
  let last;
  for(let attempt=0;attempt<2;attempt++) try {
   for(let redirect=0;redirect<4;redirect++) {
    const old=hostGates.get(current.hostname)||Promise.resolve();let release;hostGates.set(current.hostname,new Promise(r=>{release=r;}));await old;
    let response;try { response=await request(current.href,{timeout:10000,maxContentLength:2000000,maxBodyLength:2000000,maxRedirects:0,validateStatus:s=>s>=200&&s<400,headers:{'User-Agent':'Abeona-Connector/1.11 (https://github.com/Xhanti-mbasa/Abeona; community tool)',Accept:'text/html'}}); } finally { await wait(minimumIntervalMs);release(); }
    if(response.status>=300) { const target=new URL(response.headers.location,current);if(target.protocol!=='https:' || target.hostname!=='cttrains.co.za' || target.username || target.password) throw new Error('Unapproved redirect');current=target;continue; }
    health.set('PRASA',{available:true,checkedAt:Date.now(),failure:null});return response.data;
   }
   throw new Error('Too many source redirects');
  } catch(e) { last=e;const status=e.response?.status;if(attempt || status && ![502,503,504].includes(status) || !status && !['ECONNRESET','ETIMEDOUT','ECONNABORTED'].includes(e.code)) break;await wait(retryDelayMs); }
  health.set('PRASA',{available:false,checkedAt:Date.now(),failure:last.response?.status||last.code||'source_unavailable'});throw last;
 });
}
module.exports={providers,health,coalesce,readPage};
