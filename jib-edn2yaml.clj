(require '[clj-yaml.core :as yaml])
(require '[clojure.java.io :as io])
(require '[clojure.pprint :refer [pprint]])
(require '[clojure.edn :as edn])
(require '[babashka.cli :as cli])


(defn get-target-jar-path [dir pattern]
  (some->> (.listFiles (io/file dir))
           (filter #(re-matches pattern (.getName %)))
           (first)
           (.getPath)))

(def custom-readers {'now     (fn [& _] (java.util.Date.))
                     'app-jar (fn [[dir pattern]]
                                (get-target-jar-path dir (re-pattern pattern)))})


(defn edn2yaml [edn-file yaml-file]
  (->> edn-file
       (slurp)
       (edn/read-string {:readers custom-readers})
       (yaml/generate-string)
       (spit yaml-file)))


(defn -main [& args]
  (let [arguments (first (:args (cli/parse-args args)))]
    (if (= 2 (count arguments))
      (apply edn2yaml arguments)
      (println "Usage: file.edn file.yaml"))))

(when (= *file* (System/getProperty "babashka.file"))
  (-main *command-line-args*))
