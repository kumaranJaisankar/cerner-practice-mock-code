(ns system)



(def-concept-context-reltn pain-assessment-proc
                           {:concept-alias "PAIN_ASSESSMENT_PROC" :context-id "" })

(def-concept-context-reltn pain-score-obstype
                           {:concept-alias "PAIN_SCORE_OBSTYPE" :context-id "" })



; rules
(def-ontology-rule
  get-pain-assessment-proc-type
  {:concepts [{:name pain-assessment-proc}]}
  (binding
    <-
    Procedure
    (and (has-concept? (primary-codes this) **pain-assessment-proc) (valid-status? this)))
  = >
  (insert-proxy PainAssessment binding))

(def-ontology-rule
  get-pain-score-obstype-type
  {:concepts [{:name pain-assessment-proc}]}
  (binding
    <-
    ObservationType
    (and (has-concept? (primary-codes this) **pain-score-obstype) (valid-status? this)))
  = >
  (insert-proxy PainAssessment binding))