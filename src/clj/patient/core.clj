(ns patient.core
    (:require [patient.routes :refer [ping-routes patients-routes]]
              [muuntaja.core :as m]
              [org.httpkit.server :refer [run-server]]
              [reitit.coercion.schema]
              [reitit.ring :as ring]
              [reitit.ring.coercion :refer [coerce-exceptions-middleware
                                            coerce-request-middleware
                                            coerce-response-middleware]]
              [reitit.ring.middleware.exception :refer [exception-middleware]]
              [reitit.ring.middleware.muuntaja :refer [format-negotiate-middleware
                                                       format-request-middleware
                                                       format-response-middleware]]
              [reitit.ring.middleware.parameters :refer [parameters-middleware]]
              [clojure.tools.namespace.repl :refer [refresh]]
              [ring.middleware.reload :refer [wrap-reload]]
              [clojure.java.io :as io]
              )
  (:gen-class))
                                                 
(defonce server (atom nil))

(defn index []
  (slurp (io/resource "public/index.html")))

(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(def app 
  (ring/ring-handler
    (ring/router
      ["/"
       ["" {:handler (fn [req] {:body (index) :status 200})}]
       ["assets/*" (ring/create-resource-handler {:root "public/assets"})]
       ["api"
        ping-routes
        patients-routes]
       ]
      {:data {:coercion reitit.coercion.schema/coercion
              :muuntaja m/instance
              :middleware [
                           parameters-middleware
                           format-negotiate-middleware
                           format-response-middleware
                           exception-middleware
                           format-request-middleware
                           coerce-exceptions-middleware
                           coerce-request-middleware
                           coerce-response-middleware 
                           ]}})
    ;; (ring/routes
    ;;   (ring/redirect-trailing-slash-handler)
    ;;   (ring/create-default-handler
    ;;     {:not-found (constantly {:status 404 :body "Route not found"})}))
    ))

(defn -main []
  (println "Server started")
  (reset! server (run-server (wrap-reload #'app) {:port 4200}))) ;; TODO: make wrap-reload only for dev


(defn restart-server []
  (stop-server)
  (-main))

(defn restart-and-refresh []
  (refresh)
  (restart-server))


(comment
  (restart-and-refresh)
  (stop-server)

   ;; Refresh 🔄
  (refresh)


  ;; Rerun 🫠
  (restart-server)


  ;; Stop 🛑
  (stop-server)


  ;; Start 🏰
  (-main)
  )