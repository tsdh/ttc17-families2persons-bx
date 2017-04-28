(defproject ttc17-families2persons-bx "0.1.0-SNAPSHOT"
  :description "A FunnyQT solution to the Families to Persons BX case"
  :url "http://example.com/FIXME"
  :license {:name "GNU General Public License, Version 3 (or later)"
            :url "http://www.gnu.org/licenses/gpl.html"
            :distribution :repo}
  :dependencies [[funnyqt "1.0.7"]]
  ;; :main ^:skip-aot ttc17-families2persons-bx.main
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
