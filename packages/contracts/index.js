'use strict';
function provenance(provider,fetchedAt,cached=false,sourceAvailable=true) { if(!Number.isFinite(fetchedAt)||fetchedAt<0||fetchedAt>Date.now()+60000) throw new Error('Invalid source timestamp');return {provider,kind:'scheduled',fetchedAt,publishedAt:null,cached,sourceAvailable}; }
module.exports={provenance};
