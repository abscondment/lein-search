(ns leiningen.test.search
  (:require [clojure.java.io :as io])
  (:use [clojure.test]
        [leiningen.search]))

(def tmp-index "file:///tmp/lein-sample-index.zip")

(defn fixture-tmp [f]
  (when-not (.exists (io/file tmp-index))
    (io/copy (.openStream (io/resource "sample-index.zip"))
             (io/file "/tmp/lein-sample-index.zip")))
  (binding [remote-index-location (constantly tmp-index)]
    (ensure-fresh-index ["test" {:url "http://example.com/repo"}])
    (f)))

(use-fixtures :once fixture-tmp)

(deftest test-download
  (is (= ["segments.gen" "_0.cfx" "timestamp" "_0.cfs" "segments_2"]
           (vec (.list (index-location "http://example.com/repo"))))))

(deftest test-searchy
  (let [printed (atom [])]
    (binding [clojure.pprint/pprint (partial swap! printed conj)
              results (comp doall results)]
      (search-repository ["test" {:url "http://example.com/repo"}] "hooke"))
    (is (= '#{[[[robert/hooke "1.0.0"] "Hooke your functions!"]
               [[robert/hooke "1.0.1"] "Hooke your functions!"]
               [[robert/hooke "1.0.2"] "Hooke your functions!"]
               [[robert/hooke "1.1.0"] "Hooke your functions!"]]}
           (set @printed)))))
