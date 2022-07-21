(require '[clj-yaml.core :as yaml])
(require '[clojure.java.io :as io])
(require '[clojure.pprint :refer [pprint]])
(require '[clojure.edn :as edn])

(comment
  (->> "jib.yaml"
       (io/file)
       (io/reader)
       (yaml/parse-stream)
       (pprint)
       (with-out-str)
       (spit "jib-from-yaml.edn"))

  )

(defn get-target-jar-path [dir pattern]
  (some->> (.listFiles (io/file dir))
           (filter #(re-matches pattern (.getName %)))
           (first)
           (.getPath)))

(def custom-readers {'now     (fn [& _] (java.util.Date.))
                     'app-jar (fn [[dir pattern]]
                                (get-target-jar-path dir (re-pattern pattern)))})

(->> "jib.edn"
     (slurp)
     (edn/read-string {:readers custom-readers})
     (yaml/generate-string)
     (spit "jib.yaml"))
