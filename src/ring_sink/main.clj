(ns ring-sink.main
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [ring.adapter.jetty :as jetty]
            [taoensso.timbre :refer [log] :as timbre]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [clojure.data.json :as json]))

(def config (-> "config.edn" io/resource slurp edn/read-string))


(defonce server (atom nil))

(defn setup-logger!
  [{:keys [filename println?] :or {filename nil, println? true}}]
  (when filename (timbre/merge-config!
                  {:appenders
                   {:spit (timbre/spit-appender {:fname filename})}}))
  (timbre/merge-config! {:appenders {:println {:enabled? println?}}}))

(defn log-request [request]
  (log :info request))

(defn -app [request]
  (let [req-slurped (update request :body slurp)
        req-method  (:request-method request)
        resp-code   (get-in config [:response-codes req-method] 200)]
    (log-request req-slurped)
    {:status  resp-code
     :headers {"Content-Type" "application/json"}
     :body    (-> req-slurped json/json-str)}))

(def app
  (-> -app
      (wrap-keyword-params)
      (wrap-params)))

(defn start-server []
  (reset! server (jetty/run-jetty #'app (:jetty config))))

(defn stop-server []
  (.stop @server))

(defn -main [& args]
  (setup-logger! {:filename (:log-file config)
                  :println? true})
  (start-server)
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread. stop-server)))


(comment
  (do ;;restart
    (stop-server)
    (Thread/sleep 500)
    (start-server))

  )
