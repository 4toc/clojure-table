(ns patient.routes 
  (:require [schema.core :as s]
            [patient.handler :refer [get-patients
                                     create-patient
                                     get-patient-by-id
                                     delete-patient-by-id
                                     update-patient-by-id]]))

(def ping-routes
  ["/ping" {:name :ping
            :get (fn [req]
                   {:status 200
                    :body "pong21123"})}])

(defn str-mf
  [x]
  (if (some #{x} ["m" "f"]) x (throw (ex-info "not m or f" {}))))

(def MOrF
  (s/constrained s/Str str-mf "m or f"))

(defn str-date 
  [x]
  (if (re-matches #"^\d{4}-\d{2}-\d{2}$" x) x (throw (ex-info "not in YYYY-MM-DD format" {}))))

(def DOB 
  (s/constrained s/Str str-date "YYYY-MM-DD"))

(def patients-routes
  ["/patients"
   ["" {:get get-patients
        :post create-patient}]
   ["/:id" {:parameters {:path {:id s/Int}}
            :get get-patient-by-id
            :patch {:parameters {:body 
                                 {(s/optional-key :full-name) (s/maybe s/Str)
                                  (s/optional-key :gender) (s/maybe MOrF)
                                  (s/optional-key :dob) (s/maybe DOB)
                                  (s/optional-key :address) (s/maybe s/Str)
                                  (s/optional-key :oms) (s/maybe s/Str)}}
                    :handler update-patient-by-id}
            :delete delete-patient-by-id}]
   ])

(comment 
  (println patients-routes)
  )