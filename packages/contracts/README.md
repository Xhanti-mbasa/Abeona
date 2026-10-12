# Transit contract v1

The shared JSON schema is `transit.schema.json`. `normalizeResponse` adapts the existing rail API and JVM bus-worker models to this common envelope. It preserves source identifiers and provider fields under `providerDetails`; it does not infer live vehicle data, fare prices, stop membership or dates absent from the source.

Common endpoints under `/mobile/v1`:

| Endpoint | Data |
| --- | --- |
| `/normalized/journeys?from=&to=&date=&time=` | Rail schedules, separate train legs and calls, stops, routes and source fare text |
| `/normalized/providers/MYCITI/snapshot` | Published catalogue, mapped stops, route memberships and fare products |
| `/normalized/providers/GOLDEN_ARROW/snapshot` | Published catalogue and fares; surveyed points carry `survey` provenance and no assumed route membership |
| `/normalized/providers/{provider}/timetable?url=&id=&title=&day=&pdf=` | Published bus calls, direction and service-day name |
| `/normalized/alerts` | Source reports with their published time separate from fetch time |

The existing `/journeys`, `/providers/...` and `/alerts` endpoints remain adapters for the native app and existing consumers. Both representations share the same source/cache requests. The Android client uses the established shared Kotlin models; no destructive data-format migration is needed to offer the common contract to another client.

IDs are namespaced by provider and entity type. Clock times are local `HH:mm` in `Africa/Johannesburg`; bus weekday timetables do not acquire an invented calendar date. ZAR amounts use integer cents. Unparsed rail fare text has `amountMinor: null`. Route validity, source documents, PDF metadata, shapes, notes, directions and raw calls remain available in provider details.

`fetchedAt` is the original successful source check, including retained cache data. Snapshot catalogue/map/fare components have their own check dates. `publishedAt` is null unless the source supplies it; disruption rows retain the report date. `expiresAt` is this contract's recheck threshold, not a guarantee of service validity. `cached` is independent of `kind`; scheduled data does not become live because it was retrieved recently. Zero check dates mean unavailable provenance. A partial/outage response retains original timestamps and sets source availability false.

`npm test` exercises captured rail transfers, exact call/provider-detail preservation, unknown numeric fares, survey limitations, overnight local times, source ages, outages and actual HTTP endpoints. Add a new provider only after declaring capabilities, source conditions, fixtures and a bounded connector; do not advertise an unsupported transport mode.
