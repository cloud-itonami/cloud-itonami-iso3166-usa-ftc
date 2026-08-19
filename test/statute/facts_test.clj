(ns statute.facts-test
  "Offline conformance gate for the FTC citation catalog. Runs with no
  network: it pins the shape and provenance rules that make the catalog
  trustworthy. The complementary *live* check -- re-fetching the eCFR API,
  diffing every `:statute/verified-label` and re-confirming every recorded
  absence -- is `tools/verify_citations.cljs`, which is what actually proves
  the citations are not fabricated."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [statute.facts :as facts]))

(def ^:private entries (facts/spec-basis "USA-FTC"))

(deftest ftc-has-spec-basis
  (is (seq entries) "USA-FTC must have a spec-basis; an empty catalog is not a pass")
  (is (= 17 (count entries))))

(deftest every-citation-is-official-ecfr
  (testing "no entry may cite anything but the official eCFR"
    (doseq [e entries]
      (is (facts/official-url? (:statute/url e))
          (str (:statute/id e) " cites a non-eCFR URL: " (:statute/url e)))
      (is (= :official-ecfr (:statute/url-provenance e))
          (str (:statute/id e) " has provenance " (:statute/url-provenance e))))))

(deftest every-entry-carries-its-verification-evidence
  (testing "a citation without recorded evidence is indistinguishable from a fabricated one"
    (doseq [e entries]
      (is (contains? #{16 48} (:statute/cfr-title e))
          (str (:statute/id e) " has no CFR title"))
      (is (= (facts/ecfr-api-for e) (:statute/verified-via e))
          (str (:statute/id e) " verified-via does not match its CFR title's API endpoint"))
      (is (and (string? (:statute/verified-label e))
               (seq (:statute/verified-label e)))
          (str (:statute/id e) " has no verified-label"))
      (is (= "2026-08-19" (:statute/verified-at e))
          (str (:statute/id e) " has no verification date")))))

(deftest required-keys-present
  (doseq [e entries]
    (doseq [k [:statute/id :statute/title :statute/jurisdiction :statute/kind
               :statute/law-number :statute/url :statute/topic
               :statute/cfr-node]]
      (is (contains? e k) (str (:statute/id e) " is missing " k)))
    (is (= "USA-FTC" (:statute/jurisdiction e)))
    (is (seq (:statute/topic e)) (str (:statute/id e) " has no topic"))))

(deftest ids-and-urls-are-unique
  (is (= (count entries) (count (distinct (map :statute/id entries))))
      "duplicate :statute/id")
  (is (= (count entries) (count (distinct (map :statute/url entries))))
      "duplicate :statute/url — two entries pointing at one regulation"))

(deftest law-number-agrees-with-cfr-title
  (testing "a 16 CFR entry may not be filed under title 48, or vice versa"
    (doseq [e entries]
      (is (str/starts-with? (:statute/law-number e)
                            (str (:statute/cfr-title e) " CFR"))
          (str (:statute/id e) ": law-number " (pr-str (:statute/law-number e))
               " contradicts :statute/cfr-title " (:statute/cfr-title e))))))

(deftest url-path-agrees-with-cfr-title
  (doseq [e entries]
    (is (str/includes? (:statute/url e) (str "/title-" (:statute/cfr-title e) "/"))
        (str (:statute/id e) ": URL is not under its declared CFR title"))))

(deftest url-path-agrees-with-cfr-node
  (testing "the human URL must actually address the node that was verified"
    (doseq [e entries]
      (let [[kind id] (:statute/cfr-node e)
            segment (str "/" (name kind) "-" id)]
        (is (str/ends-with? (:statute/url e) segment)
            (str (:statute/id e) ": URL " (pr-str (:statute/url e))
                 " does not end in " (pr-str segment)
                 " — the address and the verified node disagree"))))))

(deftest every-entry-is-under-the-ftc-chapter
  (testing "this is an FTC leaf: 16 CFR entries must sit under chapter I"
    (doseq [e entries
            :when (= 16 (:statute/cfr-title e))]
      ;; The boundary is load-bearing: "/chapter-II/" *contains*
      ;; "/chapter-I", so a bare `includes?` silently accepts every CPSC
      ;; citation. But the chapter entry's own URL legitimately *ends* at
      ;; "/chapter-I", so requiring a trailing slash rejects a true entry.
      ;; Both halves were found by mutation, not by reading.
      (is (or (str/ends-with? (:statute/url e) "/chapter-I")
              (str/includes? (:statute/url e) "/chapter-I/"))
          (str (:statute/id e) " is a 16 CFR entry outside chapter I — "
               "chapter II is the Consumer Product Safety Commission, "
               "which is not this leaf's agency")))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (testing "the catalog must not answer for jurisdictions it does not cover"
    (is (nil? (facts/spec-basis "USA"))
        "government-wide USA law belongs to cloud-itonami-iso3166-usa, not here")
    (is (nil? (facts/spec-basis "USA-DOE")))
    (is (nil? (facts/spec-basis "USA-CPSC"))
        "16 CFR chapter II is the CPSC's, not this leaf's")
    (is (nil? (facts/spec-basis "ZZZ")))
    (is (empty? (facts/by-topic "ZZZ" :antitrust)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["USA-FTC" "USA" "ZZZ"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["USA" "ZZZ"] (:missing-jurisdictions c)))
    (is (= 17 (:citation-count c)))
    (is (= 1 (:absence-count c)))))

(deftest by-topic-filters
  (is (= ["usa-ftc.hsr-subchapter"
          "usa-ftc.hsr-801-coverage"
          "usa-ftc.hsr-802-exemptions"
          "usa-ftc.hsr-803-transmittal"]
         (mapv :statute/id (facts/by-topic "USA-FTC" :merger-control)))
      "merger control = the HSR subchapter and its three parts")
  (is (= ["usa-ftc.16cfr2-nonadjudicative"
          "usa-ftc.16cfr3-adjudicative"
          "usa-ftc.16cfr14-policy-statements"]
         (mapv :statute/id (facts/by-topic "USA-FTC" :enforcement-procedure)))
      "procedure = investigation, adjudication, policy statements")
  (is (empty? (facts/by-topic "USA-FTC" :maritime))
      "a topic the catalog does not cover must return empty, not a guess"))

(deftest citations-are-the-ingest-surface
  (let [cs (facts/citations)]
    (is (= 17 (count cs)))
    (is (every? facts/official-url? cs))
    (is (= cs (sort cs)) "citations must be sorted for stable diffing")))

;; ── the absence half ──────────────────────────────────────────────────────
;; A catalog that only records what exists cannot warn an operator away from
;; hunting for something that does not. These pin the shape of the negatives
;; so the live gate has something well-formed to re-check.

(deftest absences-are-well-formed
  (is (seq facts/absences)
      "an empty absence list is not a pass — it means nothing negative is being checked")
  (doseq [a facts/absences]
    (doseq [k [:absence/id :absence/claim :absence/cfr-title
               :absence/absent-label :absence/scope
               :absence/verified-via :absence/verified-at
               :absence/consequence]]
      (is (contains? a k) (str (:absence/id a) " is missing " k)))
    (is (contains? #{16 48} (:absence/cfr-title a)))
    (is (= (get facts/ecfr-structure-api (:absence/cfr-title a))
           (:absence/verified-via a))
        (str (:absence/id a) " verified-via does not match its CFR title's API endpoint"))
    (is (= "2026-08-19" (:absence/verified-at a)))))

(deftest the-recorded-absence-does-not-contradict-the-catalog
  (testing "we may not assert a label is absent from a title we also cite it in"
    (doseq [a facts/absences
            e entries
            :when (= (:absence/cfr-title a) (:statute/cfr-title e))]
      (is (not= (:absence/absent-label a) (:statute/verified-label e))
          (str (:absence/id a) " claims " (pr-str (:absence/absent-label a))
               " is absent from title " (:absence/cfr-title a)
               " while " (:statute/id e) " cites it as present")))))

(deftest no-far-supplement-is-recorded
  (testing "the FTC's lack of a 48 CFR chapter is the leaf's load-bearing negative"
    (let [a (first (filter #(= "usa-ftc.no-far-supplement" (:absence/id %))
                           facts/absences))]
      (is (some? a) "the no-FAR-supplement absence must be recorded")
      (is (= 48 (:absence/cfr-title a)))
      (is (= :chapter (:absence/scope a)))
      (is (= "Federal Trade Commission" (:absence/absent-label a))))))

;; ── the README is the discovery surface for these citations ───────────────
;; A count in prose drifts silently the moment the catalog grows. Pin it.

(deftest readme-counts-match-the-catalog
  (testing "README must not advertise a citation count the catalog does not have"
    (let [readme (slurp "README.md")]
      (is (str/includes? readme (str "**" (count entries) " FTC regulations**"))
          (str "README does not state the real citation count ("
               (count entries) ")"))
      (is (str/includes? readme (str "**" (count facts/absences) " checked absence**"))
          (str "README does not state the real absence count ("
               (count facts/absences) ")")))))
