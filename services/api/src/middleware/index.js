'use strict';
function errorHandler(err,req,res,next) { console.error(JSON.stringify({event:'request_failure',method:req.method,path:req.path,kind:err.code||err.name}));res.status(err.status||500).json({error:err.status===400?'Invalid request.':'The request could not be completed. Retry later.',path:req.path}); }
function notFound(req,res) { res.status(404).json({error:`Route not found: ${req.method} ${req.path}`}); }
const RATE_LIMIT=Math.max(1,Math.min(600,parseInt(process.env.RATE_LIMIT_RPM||'60',10)||60)),ipHits=new Map();
function rateLimiter(req,res,next) {
 if(req.path==='/health'||req.path==='/ready') return next();const ip=req.ip||req.socket.remoteAddress,now=Date.now();let entry=ipHits.get(ip);
 if(!entry||entry.until<=now) { for(const [key,value] of ipHits) if(value.until<=now) ipHits.delete(key);if(ipHits.size>=10000&&!ipHits.has(ip)) return res.set('Retry-After','60').status(429).json({error:'Server busy. Retry later.'});entry={hits:0,until:now+60000};ipHits.set(ip,entry); }
 if(++entry.hits>RATE_LIMIT) return res.set('Retry-After','60').status(429).json({error:`Rate limit exceeded. Maximum ${RATE_LIMIT} requests per minute.`,retry_after_seconds:60});next();
}
module.exports={errorHandler,notFound,rateLimiter};
