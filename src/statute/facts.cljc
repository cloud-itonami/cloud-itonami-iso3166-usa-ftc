(ns statute.facts
  "Agency-level compliance catalog for **USA-FTC** (United States Federal Trade
  Commission) -- the spec-basis behind this leaf's blueprint claim that an
  independent operator can navigate `FTC competition & advertising compliance`.

  Scope. This is the FTC-specific layer only. Government-wide U.S. federal
  statutes (Sarbanes-Oxley, FLSA, ...) live in the country coordinator
  `cloud-itonami-iso3166-usa`'s `statute.facts` and are NOT duplicated here;
  the two catalogs compose, keyed `USA-FTC` -> `USA`.

  Provenance. Every entry cites the official eCFR (Electronic Code of Federal
  Regulations, GPO/Office of the Federal Register) address for the smallest
  stable unit that was independently confirmed. Nothing here is fabricated:
  each `:statute/verified-label` below is the byte-exact `label_description`
  returned by the eCFR versioner API on `:statute/verified-at`, and
  `tools/verify_citations.cljs` re-fetches that API and fails if any label
  drifts.

  Why the citation and the verification URL differ. `:statute/url` is the
  canonical human address a person should open. It is deliberately NOT the
  URL that was machine-verified: fetching www.ecfr.gov from an automated
  client returns HTTP 200 with a `Federal Register :: Request Access`
  interstitial rather than the regulation, so a status-code check against it
  would report success while proving nothing. We therefore verify through the
  documented machine API (`:statute/verified-via`) and record both. Do not
  `curl` the `:statute/url` and treat a 200 as confirmation -- it is not.

  What this catalog also records is an ABSENCE. See `absences` below: the FTC
  has no acquisition-regulation chapter in 48 CFR. That is a checked fact, not
  an omission, and the live gate re-verifies it. An operator reading only the
  positive entries could otherwise assume an FTC FAR supplement exists and go
  looking for one.

  Extending. A regulation not in this table has NO spec-basis, full stop.
  Extend `catalog` with a real, API-confirmed citation; never invent an id,
  a URL, or a label."
  (:require [kotoba.lang.text :as str]))

(def ecfr-structure-api
  "eCFR versioner structure endpoints these entries were verified against.
  Keyed by CFR title. The date is the title's `up_to_date_as_of` at
  verification time, so the call is reproducible rather than `current`."
  {16 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
   48 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-07/title-48.json"})

(def catalog
  "iso3166 code -> vector of regulation entries.

  `USA-FTC` is an agency-level key (parent `USA`), matching
  `blueprint.edn`'s `:itonami.blueprint/iso3166`."
  {"USA-FTC"
   [;; ── 16 CFR chapter I — the FTC's own chapter ──────────────────────────
    {:statute/id "usa-ftc.16cfr-chapter-i"
     :statute/title "16 CFR Chapter I — Federal Trade Commission (the agency's entire rule chapter)"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Chapter I"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:chapter "I"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Federal Trade Commission"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:agency-scope}}

    ;; ── Subchapter H — Hart-Scott-Rodino premerger notification ───────────
    ;; The competition-side obligation an operator actually files under.
    {:statute/id "usa-ftc.hsr-subchapter"
     :statute/title "HSR Antitrust Improvements Act rules — premerger notification regime"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Subchapter H"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-H"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:subchapter "H"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Rules, Regulations, Statements and Interpretations Under the Hart-Scott-Rodino Antitrust Improvements Act of 1976"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:antitrust :merger-control}}
    {:statute/id "usa-ftc.hsr-801-coverage"
     :statute/title "HSR Part 801 — Coverage Rules (does a transaction have to be reported at all)"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Part 801"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-H/part-801"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "801"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Coverage Rules"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:antitrust :merger-control :filing-threshold}}
    {:statute/id "usa-ftc.hsr-802-exemptions"
     :statute/title "HSR Part 802 — Exemption Rules (which reportable transactions are carved out)"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Part 802"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-H/part-802"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "802"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Exemption Rules"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:antitrust :merger-control :filing-threshold}}
    {:statute/id "usa-ftc.hsr-803-transmittal"
     :statute/title "HSR Part 803 — Transmittal Rules (how and when the notification is filed)"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Part 803"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-H/part-803"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "803"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Transmittal Rules"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:antitrust :merger-control :filing-procedure}}

    ;; ── Robinson-Patman: the competition rule that bites on bid pricing ───
    {:statute/id "usa-ftc.16cfr240-advertising-allowances"
     :statute/title "Part 240 — Guides for Advertising Allowances (Robinson-Patman s2(d)/(e) promotional-allowance parity)"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :guidance
     :statute/law-number "16 CFR Part 240"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-B/part-240"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "240"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Guides for Advertising Allowances and Other Merchandising Payments and Services"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:antitrust :price-discrimination}}

    ;; ── Advertising-side substantive rules ────────────────────────────────
    {:statute/id "usa-ftc.16cfr233-deceptive-pricing"
     :statute/title "Part 233 — Guides Against Deceptive Pricing"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :guidance
     :statute/law-number "16 CFR Part 233"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-B/part-233"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "233"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Guides Against Deceptive Pricing"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:advertising :pricing-claims}}
    {:statute/id "usa-ftc.16cfr255-endorsements"
     :statute/title "Part 255 — Guides Concerning Use of Endorsements and Testimonials in Advertising"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :guidance
     :statute/law-number "16 CFR Part 255"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-B/part-255"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "255"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Guides Concerning Use of Endorsements and Testimonials in Advertising"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:advertising :endorsements}}
    {:statute/id "usa-ftc.16cfr260-green-guides"
     :statute/title "Part 260 — Guides for the Use of Environmental Marketing Claims (Green Guides)"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :guidance
     :statute/law-number "16 CFR Part 260"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-B/part-260"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "260"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Guides for the Use of Environmental Marketing Claims"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:advertising :environmental-claims}}
    {:statute/id "usa-ftc.16cfr323-made-in-usa"
     :statute/title "Part 323 — Made in USA Labeling Rule"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Part 323"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-C/part-323"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "323"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Made in USA Labeling"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:advertising :origin-claims}}
    {:statute/id "usa-ftc.16cfr464-unfair-fees"
     :statute/title "Part 464 — Rule on Unfair or Deceptive Fees"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Part 464"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-D/part-464"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "464"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Rule on Unfair or Deceptive Fees"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:advertising :pricing-claims}}
    {:statute/id "usa-ftc.16cfr465-consumer-reviews"
     :statute/title "Part 465 — Rule on the Use of Consumer Reviews and Testimonials"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Part 465"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-D/part-465"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "465"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Rule on the Use of Consumer Reviews and Testimonials"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:advertising :endorsements}}
    {:statute/id "usa-ftc.16cfr461-impersonation"
     :statute/title "Part 461 — Rule on Impersonation of Government and Businesses"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Part 461"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-D/part-461"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "461"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Rule on Impersonation of Government and Businesses"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:advertising :government-affiliation-claims}}

    ;; ── Procedure: what happens when the FTC comes asking ─────────────────
    {:statute/id "usa-ftc.16cfr2-nonadjudicative"
     :statute/title "Part 2 — Nonadjudicative Procedures (investigations, civil investigative demands)"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :procedure
     :statute/law-number "16 CFR Part 2"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-A/part-2"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "2"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Nonadjudicative Procedures"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:enforcement-procedure :investigation}}
    {:statute/id "usa-ftc.16cfr3-adjudicative"
     :statute/title "Part 3 — Rules of Practice for Adjudicative Proceedings"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :procedure
     :statute/law-number "16 CFR Part 3"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-A/part-3"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "3"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Rules of Practice for Adjudicative Proceedings"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:enforcement-procedure :adjudication}}
    {:statute/id "usa-ftc.16cfr14-policy-statements"
     :statute/title "Part 14 — Administrative Interpretations, General Policy Statements, and Enforcement Policy Statements"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :guidance
     :statute/law-number "16 CFR Part 14"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-A/part-14"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:part "14"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Administrative Interpretations, General Policy Statements, and Enforcement Policy Statements"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:enforcement-procedure :agency-scope}}

    ;; ── Subchapter J — the empty shelf, recorded on purpose ───────────────
    ;; The subchapter exists; its only part range (910-999) is [RESERVED].
    ;; An operator screening for "codified unfair-methods-of-competition
    ;; rules" must be told the shelf is empty rather than left to search.
    {:statute/id "usa-ftc.umc-subchapter-j"
     :statute/title "Subchapter J — Rules Concerning Unfair Methods of Competition (heading exists; parts 910-999 are RESERVED)"
     :statute/jurisdiction "USA-FTC"
     :statute/kind :regulation
     :statute/law-number "16 CFR Subchapter J"
     :statute/url "https://www.ecfr.gov/current/title-16/chapter-I/subchapter-J"
     :statute/url-provenance :official-ecfr
     :statute/cfr-title 16
     :statute/cfr-node [:subchapter "J"]
     :statute/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-17/title-16.json"
     :statute/verified-label "Rules Concerning Unfair Methods of Competition"
     :statute/verified-at "2026-08-19"
     :statute/topic #{:antitrust :reserved-shelf}}]})

(def absences
  "Checked NEGATIVES. Each is a fact an operator would otherwise have to
  discover by failing to find something, and each is re-verified by
  `tools/verify_citations.cljs` so that it turns red if it ever stops being
  true.

  `:absence/assertion` is machine-checkable: no chapter of the named CFR
  title carries `:absence/absent-label` as its `label_description`."
  [{:absence/id "usa-ftc.no-far-supplement"
    :absence/claim
    (str "The FTC has no agency acquisition regulation (FAR supplement) chapter "
         "in 48 CFR. Agencies such as DOE (chapter 9) and DHS (chapter 30) do; "
         "the FTC does not. An operator selling TO the FTC is governed by the "
         "government-wide FAR (48 CFR chapter 1) with no FTC-specific "
         "supplement layered on top.")
    :absence/cfr-title 48
    :absence/absent-label "Federal Trade Commission"
    :absence/scope :chapter
    :absence/verified-via "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-07/title-48.json"
    :absence/verified-at "2026-08-19"
    :absence/consequence
    (str "This leaf's spec-basis is an ENFORCEMENT regime that binds bidders' "
         "conduct, not a set of contract clauses in the FTC's own solicitations. "
         "Do not look for FTC-specific bid clauses; there are none.")}])

(defn spec-basis
  "Entries for `iso3166`, or nil when this catalog claims no basis for it."
  [iso3166]
  (get catalog iso3166))

(defn by-topic
  "Entries for `iso3166` carrying `topic`. Empty for unknown jurisdictions."
  [iso3166 topic]
  (filterv #(contains? (:statute/topic %) topic) (spec-basis iso3166)))

(defn citations
  "Every official URL in the catalog, deduplicated and sorted. This is the
  set `statute.facts-test` and the ingest gate check."
  []
  (->> (vals catalog)
       (mapcat identity)
       (map :statute/url)
       distinct
       sort
       vec))

(defn coverage
  "Honest coverage report: what was asked for vs what has a spec-basis.
  Never reports a jurisdiction as covered because it looks plausible."
  ([] (coverage (keys catalog)))
  ([iso3166s]
   (let [have (filter catalog iso3166s)
         missing (remove catalog iso3166s)]
     {:requested (count iso3166s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :citation-count (count (citations))
      :absence-count (count absences)
      :note (str "cloud-itonami-iso3166-usa-ftc statute.facts: "
                 (count (get catalog "USA-FTC"))
                 " FTC agency-level regulations plus "
                 (count absences)
                 " checked absence(s), each confirmed against the official "
                 "eCFR versioner API on 2026-08-19. Government-wide U.S. "
                 "statutes are NOT here -- see cloud-itonami-iso3166-usa. "
                 "Extend `statute.facts/catalog`; never invent an id or URL.")})))

(defn ecfr-api-for
  "The eCFR structure endpoint an entry was verified against."
  [entry]
  (get ecfr-structure-api (:statute/cfr-title entry)))

(defn official-url?
  "True when `u` is an eCFR address. The only provenance this catalog accepts."
  [u]
  (and (string? u) (str/starts-with? u "https://www.ecfr.gov/current/")))
