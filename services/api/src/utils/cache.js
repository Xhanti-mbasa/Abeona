'use strict';
class MemoryCache {
 constructor({maxEntries=256,maxBytes=64*1024*1024,clock=Date.now,staleMs=48*60*60*1000}={}) { this.entries=new Map();this.bytes=0;Object.assign(this,{maxEntries,maxBytes,clock,staleMs}); }
 async set(key,value,ttl=300) { if(typeof key!=='string'||key.length>4096||!Number.isInteger(ttl)||ttl<1||ttl>90*86400) throw new Error('Invalid cache key or TTL');const bytes=Buffer.byteLength(JSON.stringify(value));if(bytes>this.maxBytes) throw new Error('Cache item is too large');await this.del(key);this.entries.set(key,{value,bytes,expiresAt:this.clock()+ttl*1000});this.bytes+=bytes;while(this.entries.size>this.maxEntries||this.bytes>this.maxBytes) await this.del(this.entries.keys().next().value); }
 async get(key) { const v=this.entries.get(key);return v&&v.expiresAt>this.clock()?v.value:null; }
 async getStale(key) { const v=this.entries.get(key);if(v&&v.expiresAt+this.staleMs>this.clock()) return v.value;await this.del(key);return null; }
 async del(key) { const v=this.entries.get(key);if(v) this.bytes-=v.bytes;this.entries.delete(key); }
 async flush() { this.entries.clear();this.bytes=0; }
 async stats() { return {memory_entries:this.entries.size,memory_bytes:this.bytes,max_entries:this.maxEntries,max_bytes:this.maxBytes}; }
}
const memory=new MemoryCache();let redis=null;const prefix='abeona:v1:';
async function initRedis() { if(!process.env.REDIS_URL) return;const Redis=require('ioredis');const client=new Redis(process.env.REDIS_URL,{lazyConnect:true,enableOfflineQueue:false,maxRetriesPerRequest:1,connectTimeout:3000,commandTimeout:2000,retryStrategy:()=>null});client.on('error',()=>console.warn('{"event":"cache_failure","kind":"redis_unavailable"}'));try { await client.connect();redis=client; } catch { client.disconnect(); } }
async function set(key,value,ttl=300) { await memory.set(key,value,ttl);if(redis?.status==='ready') try { await redis.set(prefix+key,JSON.stringify({value,expiresAt:Date.now()+ttl*1000}),'EX',ttl); } catch {} }
async function get(key) { const hit=await memory.get(key);if(hit!==null) return hit;if(redis?.status==='ready') try { const raw=await redis.get(prefix+key);if(raw) { const e=JSON.parse(raw),ttl=Math.floor((e.expiresAt-Date.now())/1000);if(ttl>0) { await memory.set(key,e.value,ttl);return e.value; } } } catch {}return null; }
async function del(key) { await memory.del(key);if(redis?.status==='ready') try { await redis.del(prefix+key); } catch {} }
async function flush() { await memory.flush();if(redis?.status!=='ready') return;let cursor='0';do { const r=await redis.scan(cursor,'MATCH',prefix+'*','COUNT',100);cursor=r[0];if(r[1].length) await redis.del(...r[1]); } while(cursor!=='0'); }
async function stats() { return {...await memory.stats(),redis_enabled:redis?.status==='ready'}; }
async function close() { if(redis) { redis.disconnect();redis=null; } }
module.exports={MemoryCache,initRedis,set,get,getStale:key=>memory.getStale(key),del,flush,size:()=>memory.entries.size,stats,close};
