(defproject org.clojars.technomancy/lein-search "1.0.0-SNAPSHOT"
  :description "Leiningen plugin to search repositories."
  :dependencies  [[clucy "0.1.0"]]
  ;; if it's not in dev-deps, the leiningen process won't have access to it
  :dev-dependencies  [[clucy "0.1.0"]]
  :eval-in-leiningen true)
