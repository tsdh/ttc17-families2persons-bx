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

(bx/deftransformation families2persons [f p prefer-creating-parent-to-child prefer-existing-family-to-new]
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
            [(ccl/== prefer-existing-family-to-new true)]
            [(id ?family ?id)])]
   :right [(p/->persons p ?per-reg ?person)
           (p/Person p ?person)
           (p/name p ?person ?n)
           (id ?person ?id)])
  (member2female
   :extends [(member2person)]
   :left  [(relationshipo prefer-creating-parent-to-child f ?family ?member f/->mother f/->daughters)]
   :right [(p/Female p ?person)])
  (member2male
   :extends [(member2person)]
   :left  [(relationshipo prefer-creating-parent-to-child f ?family ?member f/->father f/->sons)]
   :right [(p/Male p ?person)]))

(comment
  (emf/load-ecore-resource "metamodels/Families.ecore")
  (emf/load-ecore-resource "metamodels/Persons.ecore")

  (let [fm (emf/new-resource) #_(emf/load-resource "models/FamilyWithDuplicateMember.xmi"
                                                   #_"models/FamiliesWithSameName.xmi")
        pm (emf/new-resource)
        h (emf/ecreate! pm 'Male {:name "Simpson, Homer"})
        m (emf/ecreate! pm 'Female {:name "Simpson, Marge"})
        m (emf/ecreate! pm 'PersonRegister {:persons [h m]})
        trace (families2persons fm pm :left true false)]
    (viz/print-model fm :gtk)
    trace))
