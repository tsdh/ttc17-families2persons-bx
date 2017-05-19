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

(defn relationshipo [pref-parent f family member pref prel crel]
  (ccl/conda
   [(ccl/all
     (ccl/== pref-parent true)
     (bx/unseto? f family pref member)
     (prel f family member))]
   [(crel f family member)]))

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
   :left  [(f/Family f ?family)
           (f/name f ?family ?last-name)
           (f/->families f ?fam-reg ?family)
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
   :left  [(relationshipo prefer-parent-to-child f ?family ?member
                          :mother f/->mother f/->daughters)]
   :right [(p/Female p ?person)])
  (member2male
   :extends [(member2person)]
   :left  [(relationshipo prefer-parent-to-child f ?family ?member
                          :father f/->father f/->sons)]
   :right [(p/Male p ?person)]))

(comment
  (emf/load-ecore-resource "metamodels/Families.ecore")
  (emf/load-ecore-resource "metamodels/Persons.ecore")

  (let [fm (emf/load-resource "FamilyRegister.xmi")
        pm (emf/load-resource "PersonRegister.xmi")
        trace (families2persons fm pm :left true true)]
    (viz/print-model fm :gtk)
    #_trace))
