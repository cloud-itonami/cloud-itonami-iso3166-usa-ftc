# cloud-itonami-iso3166-usa-ftc

Open ISO 3166 **agency-level** Blueprint for **USA-FTC**: Federal Trade Commission
(parent country: **USA**).

This leaf designs a forkable OSS business for an independent operator
navigating **Federal Trade Commission**-specific public-procurement / regulatory compliance
(antitrust/competition risk screening for multi-bid public contracts), composing with the country coordinator
`cloud-itonami-iso3166-usa`.

## What this is NOT

- **Not Federal Trade Commission.** Commercial compliance navigation only.
- **Not legal advice.** Cite official sources; route licensed work to counsel.

## Official surface

- https://www.ftc.gov/

## Spec-basis

`src/statute/facts.cljk` is this leaf's citation catalog: **17 FTC regulations**
plus **1 checked absence**, each confirmed against the official eCFR versioner
API on 2026-08-19.

| topic | what is cited |
|---|---|
| agency scope | 16 CFR Chapter I — the FTC's entire rule chapter |
| antitrust / merger control | 16 CFR Subchapter H (Hart-Scott-Rodino) and parts 801 / 802 / 803 |
| price discrimination | Part 240 — Robinson-Patman promotional-allowance parity |
| advertising | Parts 233, 255, 260, 323, 461, 464, 465 |
| enforcement procedure | Parts 2 (CIDs), 3 (adjudication), 14 (policy statements) |
| the empty shelf | Subchapter J — the heading exists, parts 910–999 are RESERVED |

### The absence is part of the data

`statute.facts/absences` records that **the FTC has no acquisition-regulation
(FAR supplement) chapter in 48 CFR**. DOE has chapter 9 and DHS has chapter 30;
the FTC has none. This matters for how you read the leaf: its spec-basis is an
**enforcement regime binding bidders' conduct**, not a set of clauses in the
FTC's own solicitations. Do not go looking for FTC-specific bid clauses.

An operator can only learn that by failing to find something, so it is written
down and re-checked rather than left as an omission.

## Verifying the citations yourself

```bash
nbb tools/verify_citations.cljk   # live: re-fetches the official eCFR API
clojure -M:test                   # offline: shape and provenance conformance
```

The live gate re-fetches the eCFR versioner API and asserts both directions:
every recorded label is still byte-exact, **and** every recorded absence is
still absent. Exit codes are three-valued — `0` verified, `1` drifted, `2`
could-not-answer. **`2` is not a pass.**

It deliberately does **not** `curl` the `:statute/url` values. `www.ecfr.gov`
answers automated clients with HTTP 200 and a *Federal Register :: Request
Access* interstitial instead of the regulation, so a status-code check there
reports success while proving nothing. Verification goes through the documented
machine API instead.

## Capability layer

Resolves via `kotoba-lang/iso3166` (`USA-FTC`, parent `USA`).

## License

AGPL-3.0-or-later.
