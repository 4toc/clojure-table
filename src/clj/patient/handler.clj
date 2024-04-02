(ns patient.handler
    (:require [patient.db :as db]
              [clojure.string]
              ))


(def valid-order-columns #{:id :full-name :address :dob :gender :oms :created-at })
(def valid-editable-columns #{:full-name :address :dob :gender :oms})

(defn validate-order-by
  [pair]
  (valid-order-columns (first pair)))

(defn parse-order-params
  [param-str]
  (->> (clojure.string/split param-str #",")
       (mapv (fn [s] (let [[column order] (clojure.string/split s #"_" 2)]
                      [(keyword column) (keyword order)])))
       (filter validate-order-by)))

(defn get-patients
  [{:keys [params]}]
  (let [order-data (parse-order-params (get params "order" ""))
        search (get params "search" "")]
    {:status 200
     :body (db/get-patients {:order-by-params order-data 
                             :search search})}))

(defn create-patient [params]
  (let [created-id (db/create-patient)]
    {:status 201
     :body created-id}) 
  )

(defn get-patient-by-id
  [{:keys [parameters]}] 
  (let [{id :id} (:path parameters) 
        patient (db/get-patient-by-id id)]
    (println "Patient:" patient)
    (if patient
      {:status 200
       :body patient}
      {:status 404
       :body {:error "Not found"}})))

(defn delete-patient-by-id
  [{:keys [parameters]}]
  (let [{id :id} (:path parameters) 
        patient (db/delete-patient-by-id id)]
    (if patient
      {:status 200
       :body patient}
      {:status 404
       :body {:error "Not found"}})))

(defn update-patient-by-id
  [{:keys [parameters]}]
  (let [data (:body parameters) 
        id (get-in parameters [:path :id])
        fields (select-keys data valid-editable-columns)
        updated-fields (cond-> fields
                         (contains? fields :dob) (update :dob #(java.sql.Date/valueOf %)))
        result (if (seq updated-fields)
                 (db/update-patient-by-id id updated-fields)
                 nil)]
    (if result
      {:status 200
       :body result}
      {:status 404
       :body {:error "Not Updated"}})))