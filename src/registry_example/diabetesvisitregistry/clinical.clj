(ns registry-example.diabetesvisitregistry.clinical)


(defcomponent age-gte-18
              (fact-type Person)
              (settings
                program-end-date <-(:name program-end-date :group "cerner-standard"))
              (value-expr
                (valid-age? this {:min {:age 18 :as-of program-end-date :exclude false}}))

(defcomponent deceased
              (fact-type Deceased)
              (selector "clinical-newest - no table about this component")
              (settings
                program-end-date {:name program-end-date :group "cerner-standard"})
              (date-range
                :end **program-end-date))

(def-component diabetes-mellitus-type1
               (fact-type DiabetesType1)
               (selector clinical-newest)
               (settings
                 program-end-date <- {:name program-end-date :group "cerner-standard"} ; these are imports from the fact_setting file
                 program-start-date <- { :name program-start-date :group "cerner-standard"})
               (date-range :start program-start-date
                           :end program-end-date))

(def-component diabetes-mellitus-type2
              (fact-type DiabetesMellitusType2)
              (selector clinical-newest)
              (settings
                program-end-date <- {:name program-end-date :group "cerner-standard"} ; these are imports from the fact_setting file
                program-start-date <- { :name program-start-date :group "cerner-standard"})
              (date-range :start program-start-date
                          :end program-end-date))

(def-component hospital-visit
               (fact-type HospitalVisit)
               ;(selector "no table")
               (settings
                 program-end-date <- {:name program-end-date :group "cerner-standard"} ; these are imports from the fact_setting file
                 program-start-date <- { :name program-start-date :group "cerner-standard"})
               (date-range :start program-start-date
                           :end program-end-date))
(def-component fall-risk-visit
         (fact-type DiabetesMellitusType2)
         ;(selector "no table")
         (settings
           program-end-date <- {:name program-end-date :group "cerner-standard"} ; these are imports from the fact_setting file
           program-start-date <- { :name program-end-date :group "cerner-standard"})
         (date-range :start **program-start-date
                     :end **program-end-date)
               )

(def-component hospic-care
               (fact-type HospiceCare)
               (selector clinical-newest)
               (settings
                 program-end-date <- {:name program-end-date :group "cerner-standard"} ; these are imports from the fact_setting file
                 program-start-date <- { :name program-end-date :group "cerner-standard"})
               (date-range :start **program-start-date
                           :end **program-end-date)
               )


(def-component hba1c-lt8
              (fact-type Hba1cTest)
              (selector clinical-newest)
              (value-expr
                (clinically-less? this 8))                 ; if componet mets a value test
              (settings
                program-end-date <- {:name program-end-date :group "cerner-standard"} ; these are imports from the fact_setting file
                program-start-date <- { :name program-start-date :group "cerner-standard"}
                )
              (date-range :start **program-start-date
                          :end **program-end-date)
              ))
;
;(def-component hba1c-lt8
;               (fact-type Hba1cTest)
;               (selector "no table"
;                     :cardinality {:connect-at-least 2 }    ; if there is a  cardinalty value
;                         :with-distinct sit/time-interval->end-localdate ; if there is distinct date is mentioned
;                         )
;               (value-expr
;                 (clinically-less? this 8))                 ; if componet mets a value test
;
;               (settings
;                 program-end-date <- {:name program-end-date :group "cerner-standard"} ; these are imports from the fact_setting file
;                 program-start-date <- { :name program-end-date :group "cerner-standard"})
;               (date-range :start **program-start-date
;                           :end **program-end-date)
;               )

(defcomponent-group registry-identification
                        (criteria
                          (or
                            (and hospital-vist diabetes-mellitus-type1)
                              (and fall-risk-visit diabetes-mellitus-type2)
                              )))

(defcomponent-group registry-exclution
  (criteria
    deceased))


                    ; MEASURES
(defmeasure hbalc-lt-8-measure
                "The proportion of persons in the Diabetes Registry population whose most recent HbA1c test result was < 8.0% during the current measurement period"
                (polarity :good)
                (short-display "HbA1c < 8%")
                (display "HbA1c < 8%")                      ;long display
                (inclusion-criteria
                  age-gte-18)
                (exclusion-criteria
                  hospic-care)
                (met-criteria
                  hba1c-lt8)
               )