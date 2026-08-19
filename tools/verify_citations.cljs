#!/usr/bin/env nbb
;; Live citation gate for src/statute/facts.cljc.
;;
;; Re-fetches the official eCFR versioner API and asserts TWO things:
;;
;;   1. PRESENCE -- every :statute/verified-label in `catalog` is still the
;;      byte-exact label_description the API returns for that node.
;;   2. ABSENCE  -- every :absence/absent-label in `absences` is still absent
;;      from the named CFR title at the named scope.
;;
;; Any drift, any missing node, any absence that stopped being true, any
;; unreachable API => non-zero.
;;
;;   nbb tools/verify_citations.cljs
;;
;; Why check absences at all. A gate that only re-checks what we wrote down
;; can only ever tell us our positives rotted. It cannot tell us the world
;; grew something we recorded as missing. `usa-ftc.no-far-supplement` says
;; the FTC has no 48 CFR chapter; if the FTC ever gets one, a presence-only
;; gate stays green while the catalog's central claim quietly goes false.
;;
;; Why this does NOT curl :statute/url --
;; www.ecfr.gov answers automated clients with HTTP 200 and a
;; "Federal Register :: Request Access" interstitial instead of the
;; regulation. A status-code check against those URLs reports success while
;; proving nothing, which is the exact failure this gate is meant to close.
;; We verify through the documented machine API instead.
;;
;; Exit codes are three-valued on purpose: 0 verified, 1 drifted/mismatched,
;; 2 could-not-answer (network/API down). 2 must never be read as a pass.
(ns verify-citations
  (:require [clojure.string :as str]))

(def catalog-file "src/statute/facts.cljc")

(defn- die [code & msg]
  (println (str/join " " msg))
  (js/process.exit code))

;; ── read the catalog without needing a Clojure runtime ────────────────────
;; We parse the entries out of the .cljc source so this gate has no build
;; step. Each entry is a map literal; we pull the fields we check.
(defn- source []
  (.readFileSync (js/require "fs") catalog-file "utf8"))

(defn- entries [src]
  (let [blocks (rest (str/split src #"\{:statute/id "))]
    (mapv (fn [b]
            (let [f (fn [re] (second (re-find re b)))]
              {:id (f #"^\"([^\"]+)\"")
               :title (f #":statute/cfr-title (\d+)")
               :node-type (f #":statute/cfr-node \[:(\w+)")
               :node-id (f #":statute/cfr-node \[:\w+ \"([^\"]+)\"\]")
               :label (f #":statute/verified-label \"((?:[^\"\\]|\\.)*)\"")
               :api (f #":statute/verified-via \"([^\"]+)\"")
               :url (f #":statute/url \"([^\"]+)\"")}))
          blocks)))

(defn- absence-entries [src]
  (let [blocks (rest (str/split src #"\{:absence/id "))]
    (mapv (fn [b]
            (let [f (fn [re] (second (re-find re b)))]
              {:id (f #"^\"([^\"]+)\"")
               :title (f #":absence/cfr-title (\d+)")
               :absent-label (f #":absence/absent-label \"((?:[^\"\\]|\\.)*)\"")
               :scope (f #":absence/scope :(\w+)")
               :api (f #":absence/verified-via \"([^\"]+)\"")}))
          blocks)))

(defn- fetch-json [url]
  (-> (js/fetch url)
      (.then (fn [r]
               (when-not (.-ok r)
                 (die 2 "CANNOT-ANSWER: eCFR API returned HTTP" (.-status r) "for" url))
               (.json r)))
      (.catch (fn [e] (die 2 "CANNOT-ANSWER: eCFR API unreachable:" (str e))))))

(defn- find-node
  "Depth-first search for a node of `type` with `identifier`."
  [node type id]
  (if (and (= type (.-type node)) (= id (.-identifier node)))
    node
    (some #(find-node % type id) (or (.-children node) []))))

(defn- labels-at-scope
  "Every label_description among nodes of `scope` anywhere under `root`."
  [root scope]
  (let [acc (atom [])]
    (letfn [(walk [n]
              (when (= scope (.-type n))
                (swap! acc conj (.-label_description n)))
              (doseq [c (or (.-children n) [])] (walk c)))]
      (walk root))
    @acc))

(defn -main []
  (let [src (source)
        es (entries src)
        as (absence-entries src)]
    (when (empty? es)
      (die 2 "CANNOT-ANSWER: parsed 0 entries from" catalog-file
           "— the gate could not read what it is supposed to check"))
    (when (empty? as)
      (die 2 "CANNOT-ANSWER: parsed 0 absences from" catalog-file
           "— the absence half of this gate had nothing to check"))
    (println "SCANNED\t" (count es) "citations and" (count as) "absences from" catalog-file)
    (-> (js/Promise.all
         (clj->js (map fetch-json (distinct (concat (map :api es) (map :api as))))))
        (.then
         (fn [docs]
           (let [by-api (zipmap (distinct (concat (map :api es) (map :api as))) docs)
                 results
                 (concat
                  ;; 1. presence
                  (for [e es]
                    (let [root (get by-api (:api e))
                          node (find-node root (:node-type e) (:node-id e))]
                      (cond
                        (nil? node)
                        [:missing (:id e) (str (:node-type e) " " (:node-id e)
                                               " not found in " (:api e))]
                        (not= (:label e) (.-label_description node))
                        [:drift (:id e) (str "recorded " (pr-str (:label e))
                                             " but API says "
                                             (pr-str (.-label_description node)))]
                        :else [:ok (:id e) (.-label_description node)])))
                  ;; 2. absence
                  (for [a as]
                    (let [root (get by-api (:api a))
                          seen (labels-at-scope root (:scope a))]
                      (if (some #(= (:absent-label a) %) seen)
                        [:appeared (:id a)
                         (str "recorded as ABSENT, but " (:scope a) " "
                              (pr-str (:absent-label a)) " now EXISTS in title "
                              (:title a) " — the catalog's claim has gone false")]
                        [:ok (:id a)
                         (str "still absent: no " (:scope a) " named "
                              (pr-str (:absent-label a)) " among "
                              (count seen) " " (:scope a) "(s) in title "
                              (:title a))]))))
                 bad (remove #(= :ok (first %)) results)]
             (doseq [[status id detail] results]
               (println (if (= :ok status) "  OK  " "  FAIL") id "—" detail))
             (println "VERIFIED\t" (count (filter #(= :ok (first %)) results))
                      "/" (count results))
             (if (seq bad)
               (die 1 "FAIL:" (count bad) "claim(s) do not match the official eCFR API")
               (println "PASS: every citation and every recorded absence matches the official eCFR API"))))))))

(-main)
