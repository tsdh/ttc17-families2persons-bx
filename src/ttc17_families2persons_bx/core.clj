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

(bx/deftransformation families2persons [f p]
  (^:top family-register2person-register
   :left  [(f/FamilyRegister f ?fr)]
   :right [(p/PersonRegister p ?pr)]
   :where [(member2female :?fr ?fr :?pr ?pr)
           (member2male :?fr ?fr :?pr ?pr)])
  (^:abstract member2person
   :left  [(f/->families f ?fr ?f)
           (f/Family f ?f)
           (f/name f ?f ?ln)
           (f/FamilyMember f ?m)
           (f/name f ?m ?fn)]
   :right [(p/->persons p ?pr ?p)
           (p/Person p ?p)
           (rel/stro ?ln ", " ?fn ?n)
           (p/name p ?p ?n)])
  (member2female
   :extends [(member2person)]
   :left  [(ccl/conde
            [(f/->mother    f ?f ?m)]
            [(f/->daughters f ?f ?m)])]
   :right [(p/Female p ?p)])
  (member2male
   :extends [(member2person)]
   :left  [(ccl/conde
            [(f/->father p ?f ?m)]
            [(f/->sons   p ?f ?m)])]
   :right [(p/Male p ?p)]))

;;* Tests

(emf/load-ecore-resource "metamodels/Families.ecore")
(emf/load-ecore-resource "metamodels/Persons.ecore")

(def fm (emf/load-resource "models/FamiliesWithSameName.xmi"))
(def pm (emf/new-resource))

(def trace (families2persons fm pm :right))

;; (viz/print-model fm :gtk)
(viz/print-model pm :gtk)
