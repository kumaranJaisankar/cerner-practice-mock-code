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
      diabetes-millitius-1-con-1 {:condition-code (itc/build-mock-code "DIABETES_MILLITIUS_TYPEl_1")
                            :effective-date (pth/get-date-from-shifted-interval program-end-date -60 :days :start) }

      diabetes-millitius-2-con-1 {:condition-code (itc/build-mock-code "DIABETES_MILLITIUS_TYPEl_1")
                            :effective-date (pth/get-date-from-shifted-interval program-end-date -3 :years :start) }

      hospital-visit-enc-1 {:encounter-code (itc/build-mock-code "HOSPITAL_VISIT_ENC")
                        :service-date (pth/get-date-from-shifted-interval program-end-date -36 :days :start)}
      hospital-visit-proc-2 {:encounter-code (itc/build-mock-code "HOSPITAL_VISIT_PROC")
                                       :effective-date (pth/get-date-from-shifted-interval program-end-date -50 :days :start) }

      fall-visit-con-enc-1 {:encounter-code (itc/build-mock-code "HOSPITAL_VISIT_ENC")
                            :effective-date (pth/get-date-from-shifted-interval program-end-date -50 :days :start) }
      fall-visit-con-proc-2 {:encounter-code (itc/build-mock-code "HOSPITAL_VISIT_PROC")
                                       :effective-date (pth/get-date-from-shifted-interval program-end-date -50 :days :start) }

      hospice-care-clin-current-year {:condition-code (itc/build-mock-code "HOSPICE_CARE_CLIN")
                                       :effective-date (pth/get-date-from-shifted-interval program-end-date -20  :days :start) }

      hospice-care-clin-previous-year {:condition-code (itc/build-mock-code "HOSPICE_CARE_CLIN")
                                       :effective-date (pth/get-date-from-shifted-interval program-end-date -2 :years :start) }

      hba1c-test-lt-8-obstype {:display-name"hbalc-6-lt-5-obstype"
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
                                   * Program-State: not-identified"
                 :kb           utils/*kb*
                 :setup        {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr :preferred-demographics deseased-conditon-2)]}}
                 :expectations (program-state->expectaiton :not-identified)})

              (utils/run-session-test  {:description "Person:
                                            * Diabetes Type 2 and fall visit in current measurement period
                                             * Registry Inclusion criteria
                                              * Program-state: identified"
                                        :kb utils/*kb*
                                        :setup {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr
                                                                                              :preferred-demographics person-age-18
                                                                                              :conditions [diabetes-mellitus-2-condition]
                                                                                              :encounters [fall-visit-con-enc-1])]}}
                                        :expectations (program-state->expectations :identified)})

               (utils/run-session-test
                              {:description "Person:
                                                 * fall visit and fall visit in previous measurement period
                                                 * Program-State: identified"
                               :kb           utils/*kb*
                               :setup        {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr
                                                                                            :preferred-demographics hospital-visit-con-1)]
                                                                             :conditions [diabetes-mellitus-2-condition]
                                                                             :procedures [fall-visit-con-proc-1]}}
                               :expectations (program-state->expectaiton :identified)})

              (utils/run-session-test
                {:description "Person:
                          * age <= 18
                          * has Hospicecare during current measurement period
                          * has hbalc < 8
                          * Program-State: not-included
                          * measure-state: not-met"
                 :kb utils/*kb*
                 :setup {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr
                                                                       :preferred-demographics person-age-lt-18
                                                                       :conditions [hospice-care-clin-current-year]
                                                                       :results [hbalc-test-lt-8-obstype] ) ] }}
                 :expectations (measure-and-program-state->expectations
                                 {:measure {cs-diabetesregistry/hbalc-lt-8-measure :not-met}
                                  :program-state :not-included} ) })


              (utils/run-session-test
                {:description "Person:
                          * age >= 18
                          * has Hospicecare during current measurement period
                          * has hbalc < 8
                          * Program-State: excluded
                          * measure-state: not-met"
                 :kb utils/*kb*
                 :setup {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr
                                                                       :preferred-demographics person-age-18
                                                                       :conditions [hospice-care-clin-current-year]
                                                                       :results [hbalc-test-lt-8-obstype] ) ] }}
                 :expectations (measure-and-program-state->expectations
                                 {:measure {cs-diabetesregistry/hbalc-lt-8-measure :not-met}
                                  :program-state :excluded} ) })
              (utils/run-session-test
                {:description "Person:
                          * age >= 18
                          * has Hospicecare during current measurement period
                          * has hbalc < 8
                          * Program-State: excluded
                          * measure-state: not-met"
                 :kb utils/*kb*
                 :setup {:pop/pop-health-person-record {:facts [(assoc utils/base-phpr
                                                                       :preferred-demographics person-age-18
                                                                       :conditions [hospice-care-clin-previous-year]
                                                                       :results [hbalc-test-lt-8-obstype] ) ] }}
                 :expectations (measure-and-program-state->expectations
                                 {:measure {cs-diabetesregistry/hbalc-lt-8-measure :met}
                                  :program-state :excluded} ) })


              (utils/run-session-test
                {:description "Person:
                          * age >= 18
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
               )
     )

