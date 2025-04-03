(ns registry-example.diabetesvisitresgistry.test-diabetesvisitregistry
  (:require [clojure.test :refer :all]))

;program-identified
;program-not-identified
;program-excluded
;program-stratified
;measure-not-included
;measure-met
;measure-not-met
;measure-excluded

(def program-end-date "2025-12-31T00:00:00.000Z")
(def program-start-date "2025-01-01T00:00:00.000Z")
(def previous-year-program-start-date "2024-01-01")
(def previous-year-program-end-date "2024-12-31")


(def program-name (str (:name registry_example.diabetesvistiregistry.clinical.program/diabetes-visit-registry)))

(defn program-state-> expectations [states]
      {:program-outcome {:fact-matches [{:name program-name
                                         :identifications [{:program-name program-name
                                                            :state (:programe-state states)}]
                                         }]}})
(use-fixtures :once
              sis/validate-schemas
              (utils/setup-kb-fixture {'synapse. lang/now program-end-date}
                                      ['cernerstandard. diabetesregistry. clinical. program/diabetes-registry] ) )

(defn measure-and-program-state-> expectations [states]
      {:program-outcome {:fact-matches [{:name program-name
                                         :measures (mapv #(hash-map :name (-> ( key %) :name str)
                                                         :state (val %))
                                         (:measure states))
                                         :identifications [{:program-name program-name
                                                            :state (:programe-state states)}]
                                         }]}})

(let [
      person-age-18 {:birth-date {:birth-date (pth/get-date-from-shifted-interval program-end-date -18 :year :start)
                                  :source {}}}
       person-age-lt-18 {:birth-date {:birth-date (pth/get-date-from-shifted-interval program-end-date -17 :year :start)
                                  :source {}}}

      deceased-condition-1 {:birth-date {:birth-date (pth/get-date-from-shifted-interval program-end-date -88 :years :start) :source {}}
                               :deceased { :deceased true
                                          :date-of-death (pth/get-date-from-shifted-interval program-end-date -30 :days :start)
                                          source {}}}
      deceased-condition-2 {:birth-date {:birth-date (pth/get-date-from-shifted-interval program-end-date -88 :years :start) :source {}}
                            :deceased { :deceased false
                                       :date-of-death (pth/get-date-from-shifted-interval program-end-date -30 :days :start)
                                       source {}}}
      hba1c-test-lt-8-obstype {:display-name"hbalc-6-dot-5-obstype"
                               :service-date (pth/get-date-from-shifted-interval program-end-date -10 :days :start)
                               :result-code (itc/build-mock-code "HEMOGLOBIN_A1C_OBSTYPE")
                               :type     ResultValueType/NUMERIC
                               :normalized-value {:typed-value {:type ResultValueType/NUMERIC
                                                                  :numeric-value {:value "7"}}}}]
     (deftest diabetes-exclution-criteria
                 (utils/run-session-test
                   {:description "Person:
                                   * deceased
                                   * Program-State: excluded"
                    :kb           utils/*kb*
                    :setup        {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr :preferred-demographics deseased-conditon-1)]}}
                    :expectations (program-state->expectaiton :excluded)})
              (utils/run-session-test
                {:description "Person:
                                   * deceased
                                   * Program-State: identified"
                 :kb           utils/*kb*
                 :setup        {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr :preferred-demographics deseased-conditon-2)]}}
                 :expectations (program-state->expectaiton :not-identified)})
               )
     )

          (utils/run-session-test
            {:description "Person:
                          * age >= 18
                          * has diabetes type 1
                          * has hbalc < 8
                          * Program-State: identified
                          * measure-state: met"
             :kb utils/*kb*
             :setup {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr
                                                                   :preferred-demographics person-age-18
                                                                   :conditions [diabetes-mellitus-1-condition]
                                                                   :results [hbalc-test-lt-8-obstype] ) ] }}
             :expectations (measure-and-program-state->expectations
                              {:measure {cs-diabetesregistry/hbalc-lt-8-measure :met}
                               :program-state :identified} ) })