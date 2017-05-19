(defproject ttc17-families2persons-bx "1.0.0"
  :description "A FunnyQT solution to the Families to Persons BX case"
  :url "https://github.com/tsdh/ttc17-families2persons-bx"
  :license {:name "GNU General Public License, Version 3 (or later)"
            :url "http://www.gnu.org/licenses/gpl.html"
            :distribution :repo}
  :dependencies [[funnyqt "1.1.3"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot nil}}
  :uberjar-exclusions [#"org/eclipse/.*"])
