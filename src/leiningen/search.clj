(ns leiningen.search
  (:use [leiningen.core :only [home-dir repositories-for #_user-settings]]
        [leiningen.util.file :only [delete-file-recursively]])
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.pprint :as pp])
  (:import (java.util.zip ZipFile)
           (java.net URL)
           (org.apache.lucene.index IndexReader)
           (org.apache.lucene.search IndexSearcher)
           (org.apache.lucene.queryParser QueryParser)
           (org.apache.lucene.util Version)
           (org.apache.lucene.analysis.standard StandardAnalyzer)
           (org.apache.lucene.store SimpleFSDirectory)))

;;; Data shuffling

(defn- unzip [source target-dir]
  (let [zip (ZipFile. source)
        entries (enumeration-seq (.entries zip))
        target-file #(io/file target-dir (.getName %))]
    (doseq [entry entries :when (not (.isDirectory entry))
            :let [f (target-file entry)]]
      (.mkdirs (.getParentFile f))
      (io/copy (.getInputStream zip entry) f))))

(defn index-location [url]
  ;; TODO: what are all url-safe chars that aren't filename-safe?
  (io/file (home-dir) "indices" (string/replace url #"[:/]" "_")))

(defn remote-index-location [url]
  (format "%s/.index/nexus-maven-repository-index.zip" url))

(defn- download-index [[id {url :url}]]
  (with-open [stream (.openStream (URL. (remote-index-location url)))]
    (println "Downloading index from" id "-" url)
    (let [tmp (java.io.File/createTempFile "lein" "index")]
      (println :copying-to tmp)
      (try (io/copy stream tmp)
           (println :unzippin)
           (unzip tmp (index-location url))
           (println :unzipped)
           true
           (finally (.delete tmp))))))

(defn download-needed? [[id {url :url}]]
  (not (.exists (index-location url))))

(defn ensure-fresh-index [repository]
  (try (if (download-needed? repository)
         (download-index repository)
         true)
       (catch java.io.FileNotFoundException _
         false)))

;;; Lucene stuff

(defn make-directory [url]
  (-> (index-location url)
      io/file
      SimpleFSDirectory.))

(defn- make-query [query]
  (.parse (QueryParser. Version/LUCENE_30 "a"
                        (StandardAnalyzer. Version/LUCENE_30)) query))

(defn parse-identifier [u]
  ;; TODO: is this really classifier? support it?
  (let [[group artifact version classifier] (.split u "\\|")
        group (if (not= group artifact) group)]
    [(symbol group artifact)
     version]))

(defn results [searcher top-docs]
  (for [score-doc (.scoreDocs top-docs)
        :let [doc (.doc searcher (.doc score-doc))]]
    [(parse-identifier (first (.getValues doc "u")))
     (first (.getValues doc "d"))]))

(defn search-repository [[id {url :url}] query]
  (with-open [r (IndexReader/open (make-directory url))
              searcher (IndexSearcher. r)]
    (pp/pprint (results searcher
                        (.search searcher (make-query query)
                                 25
                                 #_(:search-page-size (user-settings)))))))

;;; Task
(defn search [project query]
  (if (= "--update" query)
    (do (delete-file-recursively (index-location "") :silently)
        (doseq [repo (repositories-for project)]
          (ensure-fresh-index repo)))
    (doseq [repo (repositories-for project)]
      (when (ensure-fresh-index repo)
        (search-repository repo query)))))
