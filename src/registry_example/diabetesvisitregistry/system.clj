(ns registry-example.diabetesvisitregistry.system)


(def-concept-context-reltn hospicare-clin
  {:concept-alias "HOSPICE_CARE_CLIN" :context-id "" })

(def-concept-context-reltn diabetes-mellitus-type-1-clin
  {:concept-alias "DIABETES_MELLITUS_TYPE_1_CLIN" :context-id "" })

(def-concept-context-reltn diabetes-mellitus-type-2-clin
  {:concept-alias "DIABETES_MELLITUS_TYPE_2_CLIN" :context-id "" })

(def-concept-context-reltn hba1c-obstype
  {:concept-alias "HEMOGLOBIN_A1C_OBSTYPE" :context-id "" })

(def-concept-context-reltn hospital-visit-enc
  {:concept-alias "HOSPITAL_VISIT_ENC" :context-id "" }) ;default modal YES

;(def-concept-context-reltn hospital-care-clin
;  {:concept-alias "HOSPITAL_VISIT_ENC" :context-id "" }) ;additional modal "procedure"

(def-concept-context-reltn fall-visit-clin
  {:concept-alias "FALL_VISIT_ENC" :context-id "" })        ;default modal YES
;(def-concept-context-reltn hospital-care-clin
;  {:concept-alias "FALL_VISIT_ENC" :context-id "" })  ;additional modal "procedure"


; rules
(def-ontology-rule
  get-hospital-care-clinical-finding-type
  {:concepts [{:name hospital-care-clin}]}
  (binding
    <-
    ClinicalFindingType
    (and (has-concept? (clinical-finding-code this) **hospital-care-clin) (valid-status? this)))
  = >
  (insert-proxy HospiceCare binding))

(defrule  get-hospital-vist-enc
          {:concepts [{:name hospital-visit-enc}]}
          (binding
            <-
            Encounter
            (and (has-concept? (primary-codes this) **hospital-visit-enc) (valid-status? this)))
          = >
          (insert-proxy HospitalVisit binding))

(defrule  get-hospital-vist-proc
          {:concepts [{:name hospital-visit-enc}]}
          (binding
            <-
            Procedure
            (and (has-concept? (primary-codes this) **hospital-visit-enc) (valid-status? this)))
          = >
          (insert-proxy HospitalVisit binding))

(def-ontology-rule
  get-diabetes-type-1-clinical-finding-type
  {:concepts [{:name diabetes-mellitus-type-1-clin}]}
  (binding
    <-
    ClinicalFindingType
    (and (has-concept? (clinical-finding-code this) **diabetes-mellitus-type-1-clin) (valid-status? this)))
   =>
  (insert-proxy DiabetesType1 binding))


(def-ontology-rule
  get-diabetes-type-2-clinical-finding-type
  {:concepts [{:name diabetes-mellitus-type-2-clin}]}
  (binding
    <-
    ClinicalFindingType
    (and (has-concept? (clinical-finding-code this) **diabetes-mellitus-type-2-clin) (valid-status? this)))
  = >
  (insert-proxy DiabetesMellitusType2 binding))



(def-ontology-rule
  get-hba1c-observation-type
  {:concepts [{:name hba1c-obstype}]}
  (binding
    <-
    ObservationType
    (and (has-concept? (primary-code this) **hba1c-obstype) (valid-status? this)))
  = >
  (insert-proxy Hba1cTest binding))


