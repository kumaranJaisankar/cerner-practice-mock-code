(ns registry-example.diabetesvisitregistry.clinical.program)


(defprogram diabetes-vist-registry
            "The Diabetes Registry will include persons in the population, aged 18 years and older, who have had a diagnosis of diabetes mellitus type 1 or diabetes mellitus type 2 in the past two years."
            (display "Diabetes Visit Registry")
           (concept-contects
             concept-contect-index)
            (identification-criteria
              registry-identification )
            (exclusion-ctiteria
              registry-exclusion)
            (stratifications scoreble)
            (measures hbalc-lt-8-measure)
            )

