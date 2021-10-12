(ns ring-sink.main
  (:require [ring.adapter.jetty :as jetty]
            [taoensso.timbre :refer [log] :as timbre]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(def config {:jetty {:port  3000
                     :join? false}})

(defn setup-logger!
  [{:keys [filename println?] :or {filename nil, println? true}}]
  (when filename (timbre/merge-config! {:appenders {:spit (timbre/spit-appender {:fname filename})}}))
  (timbre/merge-config! {:appenders {:println {:enabled? println?}}}))

(defn log-request [request]
  (log :info (update request :body slurp)))

(defn -app [request]
  (log-request request)
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body  (:body request)})

(def app
  (-> -app
      (wrap-keyword-params)
      (wrap-params)))

(defn -main [& args]
  (setup-logger! {:filename "ring-sink.log" :println? true})
  (jetty/run-jetty #'app (:jetty config)))
