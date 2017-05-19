(ns ttc17-families2persons-bx.core
  (:require [clojure.core.logic :as ccl]
            funnyqt.emf
            [funnyqt.relational :as rel]
            [funnyqt.bidi :as bx]))

(rel/generate-metamodel-relations "metamodels/Families.ecore" f)
(rel/generate-metamodel-relations "metamodels/Persons.ecore" p)

(defn relationshipo [pref-parent f family member prel crel]
  (ccl/conda
   [(ccl/all
     (ccl/== pref-parent true)
     (bx/unseto? f family prel member)
     (prel f family member))]
   [(crel f family member)]))

(bx/deftransformation families2persons [f p prefer-parent prefer-ex-family]
  :delete-unmatched-target-elements true
  :id-init-fn bx/number-all-source-model-elements
  (^:top family-register2person-register
   :left  [(f/FamilyRegister f ?family-register)]
   :right [(p/PersonRegister p ?person-register)]
   :where [(member2female :?family-register ?family-register
                          :?person-register ?person-register)
           (member2male :?family-register ?family-register
                        :?person-register ?person-register)])
  (^:abstract member2person
   :when  [(rel/stro ?last-name ", " ?first-name ?n)]
   :left  [(f/Family f ?family)
           (f/name f ?family ?last-name)
           (f/->families f ?family-register ?family)
           (f/FamilyMember f ?member)
           (f/name f ?member ?first-name)
           (id ?member ?id)
           (ccl/conda
            [(ccl/conde
              [(ccl/== prefer-ex-family true)]
              [(bx/existing-elemento? ?member)])]
            [(id ?family ?id)])]
   :right [(p/->persons p ?person-register ?person)
           (p/Person p ?person)
           (p/name p ?person ?n)
           (id ?person ?id)])
  (member2female
   :extends [(member2person)]
   :left  [(relationshipo prefer-parent f ?family ?member f/->mother f/->daughters)]
   :right [(p/Female p ?person)])
  (member2male
   :extends [(member2person)]
   :left  [(relationshipo prefer-parent f ?family ?member f/->father f/->sons)]
   :right [(p/Male p ?person)]))
