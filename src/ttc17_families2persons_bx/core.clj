(ns ttc17-families2persons-bx.core
  (:require [clojure.core.logic :as ccl]
            [funnyqt.visualization :as viz]
            [funnyqt.emf :as emf]
            [funnyqt.relational :as rel]
            [funnyqt.bidi :as bx]))

;;* Generating the API

(rel/generate-metamodel-relations "metamodels/Families.ecore" f)
(rel/generate-metamodel-relations "metamodels/Persons.ecore" p)

;;* The Transformation

(defn relationshipo [pref-parent f family member parent-rel child-rel]
  (ccl/conda
   [(ccl/== pref-parent true)
    (ccl/conda
     [(parent-rel f family member)]
     [(child-rel f family member)])]
   [(ccl/conda
     [(child-rel f family member)]
     [(parent-rel f family member)])]))

(bx/deftransformation families2persons [f p prefer-parent-to-child prefer-ex-family-to-new]
  :delete-unmatched-target-elements true
  :id-init-fn bx/number-all-source-model-elements
  (^:top family-register2person-register
   :left  [(f/FamilyRegister f ?fam-reg)]
   :right [(p/PersonRegister p ?per-reg)]
   :where [(member2female :?fam-reg ?fam-reg :?per-reg ?per-reg)
           (member2male :?fam-reg ?fam-reg :?per-reg ?per-reg)])
  (^:abstract member2person
   :when  [(rel/stro ?last-name ", " ?first-name ?n)]
   :left  [(f/->families f ?fam-reg ?family)
           (f/Family f ?family)
           (f/name f ?family ?last-name)
           (f/FamilyMember f ?member)
           (f/name f ?member ?first-name)
           (id ?member ?id)
           (ccl/conda
            [(ccl/conde
              [(ccl/== prefer-ex-family-to-new true)]
              [(bx/existing-elemento? ?member)])]
            [(id ?family ?id)])]
   :right [(p/->persons p ?per-reg ?person)
           (p/Person p ?person)
           (p/name p ?person ?n)
           (id ?person ?id)])
  (member2female
   :extends [(member2person)]
   :left  [(relationshipo prefer-parent-to-child f ?family ?member f/->mother f/->daughters)]
   :right [(p/Female p ?person)])
  (member2male
   :extends [(member2person)]
   :left  [(relationshipo prefer-parent-to-child f ?family ?member f/->father f/->sons)]
   :right [(p/Male p ?person)]))

(comment
  (defn test-model []
    (let [m (emf/new-resource)
          mem1 (emf/ecreate! m 'FamilyMember {:name "Jim"})
          mem2 (emf/ecreate! m 'FamilyMember {:name "Jill"})
          f (emf/ecreate! m 'Family {:name "Smith" :father mem1 :mother mem2})
          fr (emf/ecreate! m 'FamilyRegister {:families [f]})]
      m))

  (emf/load-ecore-resource "metamodels/Families.ecore")
  (emf/load-ecore-resource "metamodels/Persons.ecore")

  (let [fm (test-model)
        pm (emf/new-resource)
        _ (viz/print-model fm :gtk)
        _ (families2persons fm pm :right true true)
        _ (families2persons fm pm :left true true)]
    (viz/print-model pm :gtk)))
