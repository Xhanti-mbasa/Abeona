'use strict';
const {createHash}=require('node:crypto');
const providers=new Set(['PRASA','MYCITI','GOLDEN_ARROW']);
const key=(provider,type,id)=>`${provider}:${type}:${id}`;
const digest=value=>createHash('sha256').update(JSON.stringify(value)).digest('hex').slice(0,24);
function timestamp(value) { if(!Number.isSafeInteger(value)||value<0||value>Date.now()+60000) throw new Error('Invalid source timestamp');return value; }
function provenance(provider,at,cached=false,available=true,kind='scheduled',ttl=3600000) {
 return {provider,kind,fetchedAt:timestamp(at),publishedAt:null,expiresAt:at===0?null:at+ttl,cached,sourceAvailable:available&&at>0};
}
function normalizeResponse(value,provider,metadata={}) {
 if(!providers.has(provider)||!value||typeof value!=='object'||value.service&&value.service!==provider) throw new Error('Provider contract mismatch');
 const p=provenance(provider,value.fetchedAt,metadata.cached||false,metadata.sourceAvailable!==false);
 const result={contractVersion:1,provider,timeZone:'Africa/Johannesburg',provenance:p,routes:[],stops:[],schedules:[],fares:[],disruptions:[],providerDetails:{...value}};
 const scoped=(at,kind='scheduled')=>provenance(provider,at??value.fetchedAt,p.cached,p.sourceAvailable,kind,86400000);
 const stop=(row,at,kind='scheduled')=>({id:key(provider,'stop',row.id??row.name),providerId:String(row.id??row.name),name:row.name,point:row.point??null,routeIds:provider==='GOLDEN_ARROW'?[]:(row.routes||[]).map(id=>key(provider,'route',id)),provenance:scoped(at,kind),providerDetails:row});
 if(Array.isArray(value.journeys)) {
  const stops=new Map(),routes=new Map();
  result.schedules=value.journeys.map(journey=>({id:key(provider,'journey',digest([value.query,journey])),serviceDate:value.query.date,day:null,legs:journey.legs.map(leg=>{
   const routeId=key(provider,'route',leg.line);routes.set(routeId,{id:routeId,providerId:leg.line,name:leg.line,mode:'rail',stopIds:[],provenance:p,providerDetails:{line:leg.line}});
   const calls=leg.stops.map(row=>{const s=stop(row,value.fetchedAt);stops.set(s.id,s);return {stopId:s.id,name:row.name,localTime:row.time,platform:row.platform??null,providerDetails:row};});
   return {routeId,vehicleLabel:leg.train,calls,providerDetails:{line:leg.line,train:leg.train}};
  }),provenance:p,providerDetails:{}}));
  result.stops=[...stops.values()];result.routes=[...routes.values()];
  // A source fare sentence is not an invented numeric ticket price.
  if(value.fares) result.fares=[{id:key(provider,'fare','source-text'),currency:'ZAR',amountMinor:null,product:'Source fare information',rules:value.fares,provenance:p,providerDetails:{text:value.fares}}];
  delete result.providerDetails.journeys;delete result.providerDetails.fares;
 } else if(Array.isArray(value.trips)) {
  const route=value.route;const routeId=key(provider,'route',route.id),stops=new Map();
  result.routes=[{id:routeId,providerId:route.id,name:route.title,mode:'bus',stopIds:[],provenance:p,providerDetails:route}];
  result.schedules=value.trips.map(trip=>({id:key(provider,'trip',trip.id),serviceDate:null,day:trip.day,legs:[{routeId,vehicleLabel:null,calls:trip.calls.map(row=>{const s=stop(row,value.fetchedAt);stops.set(s.id,s);return {stopId:s.id,name:row.name,localTime:row.time,platform:row.platform??null,providerDetails:row};}),providerDetails:{direction:trip.direction,notes:trip.notes,validFrom:trip.validFrom??null}}],provenance:p,providerDetails:{id:trip.id,route:trip.route}}));
  result.stops=[...stops.values()];delete result.providerDetails.trips;delete result.providerDetails.route;
 } else if(Array.isArray(value.alerts)) {
  result.disruptions=value.alerts.map(a=>({id:key(provider,'disruption',a.id),message:a.message,severity:a.kind,reportedAt:timestamp(a.reportedAt),routeIds:[],vehicleLabels:a.trains||[],provenance:{...p,kind:'scheduled',publishedAt:a.reportedAt},providerDetails:a}));delete result.providerDetails.alerts;
 } else if(Array.isArray(value.stops)&&Array.isArray(value.routes)) {
  const mapProvenance=provider==='GOLDEN_ARROW'?'survey':'scheduled';
  result.stops=value.stops.map(row=>stop(row,value.mapFetchedAt,mapProvenance));
  result.routes=value.routes.map(row=>({id:key(provider,'route',row.id),providerId:row.id,name:row.title,mode:'bus',stopIds:provider==='GOLDEN_ARROW'?[]:result.stops.filter(s=>s.routeIds.includes(key(provider,'route',row.id))).map(s=>s.id),provenance:scoped(value.catalogueCheckedAt),providerDetails:row}));
  result.fares=(value.fares||[]).flatMap(row=>Object.entries(row.products).map(([product,amount])=>{if(!Number.isSafeInteger(amount)||amount<0) throw new Error('Invalid fare amount');return {id:key(provider,'fare',`${row.id}:${product}`),currency:'ZAR',amountMinor:amount,product, rules:row.rules||'',provenance:scoped(value.faresCheckedAt),providerDetails:{...row,title:row.title}};}));
  for(const name of ['stops','routes','fares']) delete result.providerDetails[name];
 } else throw new Error('Unsupported provider payload');
 return result;
}
module.exports={normalizeResponse,provenance};
