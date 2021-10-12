(ns ring-sink.main
  (:require [ring.adapter.jetty :as jetty]
            [taoensso.timbre :refer [log] :as timbre]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [clojure.data.json :as json]))

(def config {:jetty {:port  3000
                     :join? false}})

(def server (atom nil))

(defn setup-logger!
  [{:keys [filename println?] :or {filename nil, println? true}}]
  (when filename (timbre/merge-config! {:appenders {:spit (timbre/spit-appender {:fname filename})}}))
  (timbre/merge-config! {:appenders {:println {:enabled? println?}}}))

(defn log-request [request]
  (log :info (update request :body slurp)))

(defn -app [request]
  (log-request request)
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (-> request (update :body slurp) json/json-str)})

(def app
  (-> -app
      (wrap-keyword-params)
      (wrap-params)))

(defn -main [& args]
  (setup-logger! {:filename "ring-sink.log" :println? true})
  (reset! server (jetty/run-jetty #'app (:jetty config))))

(comment
  (.stop @server)
  )
