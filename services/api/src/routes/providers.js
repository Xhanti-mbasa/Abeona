'use strict';
const express=require('express'),axios=require('axios'),cache=require('../utils/cache');
const {providers,health,coalesce}=require('@abeona/provider-connectors');const {provenance}=require('@abeona/contracts');
function createProviderRouter({request=axios.get,cacheStore=cache,workerUrl=process.env.SCRAPER_URL||'http://127.0.0.1:8081'}={}) {
 const router=express.Router();router.get('/providers',(_req,res)=>res.json({contractVersion:1,providers:Object.values(providers).map(p=>({...p,health:health.get(p.id)||{available:null,checkedAt:null}}))}));
 async function respond(req,res,provider,operation,query={}) {
  const key=`provider:v1:${provider}:${operation}:${JSON.stringify(query)}`;
  try {
   const hit=await cacheStore.get(key);if(hit) return res.json({...hit,provenance:provenance(provider||'PRASA',hit.fetchedAt,true,hit.sourceAvailable!==false)});
   const value=await coalesce(key,async()=>{ const result=await request(`${workerUrl}/${provider?provider+'/':''}${operation}`,{params:query,timeout:48000,maxContentLength:12000000,maxRedirects:0});const v=result.data;
    if(!v||typeof v!=='object'||provider&&v.service!==provider) throw new Error('Provider contract mismatch');provenance(provider||'PRASA',v.fetchedAt);await cacheStore.set(key,v,operation==='snapshot'?3600:300);return v;
   });
   health.set(provider||'PRASA',{available:value.sourceAvailable!==false,checkedAt:Date.now(),failure:null});res.json({...value,provenance:provenance(provider||'PRASA',value.fetchedAt,false,value.sourceAvailable!==false)});
  } catch { health.set(provider||'PRASA',{available:false,checkedAt:Date.now(),failure:'connector_unavailable'});const stale=await cacheStore.getStale?.(key);if(stale) return res.json({...stale,provenance:provenance(provider||'PRASA',stale.fetchedAt,true,false)});res.set('Retry-After','60').status(502).json({error:'Provider unavailable. Retry later or use a saved timetable.',provider,sourceAvailable:false}); }
 }
 router.get('/providers/:provider/:operation',async(req,res)=>{
  const {provider,operation}=req.params;if(!['MYCITI','GOLDEN_ARROW'].includes(provider)||!['snapshot','timetable'].includes(operation)) return res.status(400).json({error:'Unsupported provider operation'});
  const query={};if(operation==='timetable') {
   for(const name of ['url','id','title','day','pdf']) { const v=req.query[name];if(typeof v!=='string'||v.length>1500) return res.status(400).json({error:'Choose a published timetable'});query[name]=v; }
   let url;try { url=new URL(query.url); } catch { return res.status(400).json({error:'Invalid source URL'}); }
   const domains=provider==='MYCITI'?['myciti.org.za','www.myciti.org.za']:['gabs.co.za','www.gabs.co.za'];if(url.protocol!=='https:'||!domains.includes(url.hostname)||url.username||url.password||!['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'].includes(query.day)) return res.status(400).json({error:'Choose an official source and valid day'});
  }
  await respond(req,res,provider,operation,query);
 });router.get('/alerts',(req,res)=>respond(req,res,'','alerts'));return router;
}
module.exports={createProviderRouter};
