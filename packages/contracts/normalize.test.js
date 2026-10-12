'use strict';
const {test}=require('node:test'),assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const {normalizeResponse:normalize}=require('./normalize');
const Ajv=require('ajv/dist/2020');
const ajv=new Ajv({allErrors:true});
ajv.addFormat('date',value=>/^\d{4}-\d{2}-\d{2}$/.test(value)&&new Date(value+'T12:00:00Z').toISOString().slice(0,10)===value);
const validate=ajv.compile(require('./transit.schema.json'));
function normalizeResponse(...args) { const value=normalize(...args);assert.ok(validate(value),JSON.stringify(validate.errors));return value; }
const {parseJourneys}=require('../../services/api/src/transit/mobile-parser');
test('captured rail transfer retains separate trains, calls, platforms and unknown numeric fare',()=>{
 const journeys=parseJourneys(fs.readFileSync(path.join(__dirname,'../../fixtures/providers/html/transfer.html'),'utf8'),'07:00');
 const raw={query:{from:'Unibell',to:'Salt River',date:'2026-10-01',time:'07:00'},journeys,fetchedAt:Date.now()-3600000,fares:'Fares: single R12.00; confirm class',sourceId:'cttrains-api'};
 const a=normalizeResponse(raw,'PRASA',{cached:true,sourceAvailable:false});
 assert.equal(a.schedules[0].legs.length,2);assert.deepEqual(a.schedules[0].legs.map(x=>x.vehicleLabel),journeys[0].legs.map(x=>x.train));
 assert.deepEqual(a.schedules[0].legs.flatMap(x=>x.calls.map(c=>c.providerDetails)),journeys[0].legs.flatMap(x=>x.stops));
 assert.equal(a.fares[0].amountMinor,null);assert.equal(a.fares[0].rules,raw.fares);assert.equal(a.provenance.fetchedAt,raw.fetchedAt);assert.equal(a.provenance.cached,true);assert.equal(a.provenance.sourceAvailable,false);
 assert.deepEqual(normalizeResponse(raw,'PRASA').schedules.map(x=>x.id),a.schedules.map(x=>x.id));
});
test('survey locations never establish current Golden Arrow route membership; retained source ages differ',()=>{
 const now=Date.now(),map=now-86400000,fare=now-3600000;
 const raw={service:'GOLDEN_ARROW',fetchedAt:now,mapFetchedAt:map,catalogueCheckedAt:now,faresCheckedAt:fare,routes:[{id:'1',title:'Published route',url:'https://www.gabs.co.za/route.pdf',pdf:true}],stops:[{id:'1',name:'Survey stop',point:{latitude:-33.9,longitude:18.5},routes:['1'],source:'Government survey'}],shapes:[{route:'survey-code'}],fares:[{id:'fare',title:'Published fare',products:{Weekly:15000},rules:'Adult'}],mapNote:'Current membership unverified'};
 const a=normalizeResponse(raw,'GOLDEN_ARROW');assert.deepEqual(a.stops[0].routeIds,[]);assert.deepEqual(a.routes[0].stopIds,[]);assert.equal(a.stops[0].provenance.kind,'survey');assert.equal(a.stops[0].provenance.fetchedAt,map);assert.equal(a.fares[0].provenance.fetchedAt,fare);assert.equal(a.fares[0].amountMinor,15000);assert.equal(a.fares[0].currency,'ZAR');assert.deepEqual(a.providerDetails.shapes,raw.shapes);assert.deepEqual(a.stops[0].providerDetails,raw.stops[0]);
});
test('day-based bus timetable preserves local clock and direction without inventing a service date',()=>{
 const raw={service:'MYCITI',fetchedAt:Date.now(),route:{id:'101',title:'Vredehoek',url:'https://www.myciti.org.za/101/'},day:'Monday',sourceFile:'source-page',trips:[{id:'am',route:'101',direction:'Inbound',day:'Monday',calls:[{id:'a',name:'Start',time:'23:50'},{id:'b',name:'End',time:'00:10'}],notes:'Published night service'}]};
 const a=normalizeResponse(raw,'MYCITI');assert.equal(a.schedules[0].serviceDate,null);assert.equal(a.schedules[0].day,'Monday');assert.deepEqual(a.schedules[0].legs[0].calls.map(x=>x.localTime),['23:50','00:10']);assert.equal(a.schedules[0].legs[0].providerDetails.direction,'Inbound');assert.equal(a.providerDetails.sourceFile,'source-page');
});
test('disruption published time stays separate from source fetch time',()=>{
 const reported=Date.now()-3600000,raw={fetchedAt:Date.now(),sourceDate:'2026-10-12',alerts:[{id:'delay',message:'Train delay',kind:'Disruption',reportedAt:reported,trains:['3500'],group:'Northern line',time:'07:00'}]};
 const a=normalizeResponse(raw,'PRASA');assert.equal(a.provenance.publishedAt,null);assert.equal(a.disruptions[0].provenance.publishedAt,reported);assert.deepEqual(a.disruptions[0].vehicleLabels,['3500']);assert.deepEqual(a.disruptions[0].providerDetails,raw.alerts[0]);
});
test('reject unknown providers, malformed timestamps and invalid numeric fares',()=>{
 assert.throws(()=>normalizeResponse({fetchedAt:Date.now()},'TAXI'));assert.throws(()=>normalizeResponse({fetchedAt:Date.now()+120000,journeys:[]},'PRASA'));
 assert.throws(()=>normalizeResponse({fetchedAt:Date.now(),service:'MYCITI',stops:[],routes:[],fares:[{products:{Single:-10}}]},'MYCITI'));
});
