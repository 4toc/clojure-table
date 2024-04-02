(ns patient.db 
  (:refer-clojure  :exclude [distinct filter for group-by into partition-by set update])
  (:require [patient.env :refer [env]]
            [next.jdbc :as j]
            [honey.sql :as sql] 
            [honey.sql.helpers :refer :all :as h]
            [clojure.core :as c]
            [next.jdbc.result-set :as rs]
            ))

(def config-db
  {:dbtype "postgresql"
   :subprotocol "postgresql"
   :host "localhost"
   :port (env :DB_PORT)
   :dbname (env :DB_NAME)
   :user (env :DB_USER)
   :password (env :DB_PASSWORD)})

(defn query [q]
  (j/execute! config-db q {:builder-fn rs/as-unqualified-lower-maps}))

(defn query-one [q]
  (j/execute-one! config-db q {:builder-fn rs/as-unqualified-lower-maps}))

(defn get-patients 
  [{:keys [order-by-params search]}]
    (let [default-order-by [[:created-at :desc]]
          order-by (if (seq order-by-params) 
                     (concat order-by-params default-order-by)
                     default-order-by)
          sql-map (-> (select :*)
                      (assoc :order-by order-by)
                      (from :patients))
          sql-map-formatted (cond-> sql-map 
                              (seq search) (assoc :where [:like :full-name (str "%" search "%")]))] 
      (query (sql/format sql-map-formatted))))

(comment
  (get-patients {:order-by-params [[:full-name :asc], [:id :desc]]})
  (get-patients {:order-by-params []})
  (get-patients {:order-by-params [[:full-name :asc], [:test :desc]]})

  (get-patients {:search "Test"})
  )

(defn create-patient 
  []
  (let [sql-map (-> (insert-into :patients) 
                    (columns :full-name :dob :address :oms :gender)
                    (values [[nil nil nil nil nil]])
                    (returning :id))]
    (query-one (sql/format sql-map))))

(defn get-patient-by-id
  [id]
  (let [sql-map (-> (select :*)
                    (from :patients)
                    (where [:= :id id]))]
    (query-one (sql/format sql-map))))

(defn delete-patient-by-id
  [id]
  (let [sql-map (-> (delete-from :patients)
                    (where [:= :id id]))]
    (query-one (sql/format sql-map))))

(defn update-patient-by-id
  [id data]
  (let [sql-map (-> (update :patients)
                    (set data)
                    (where [:= :id id]))]
    (query-one (sql/format sql-map))))

(comment 
  (update-patient-by-id 29 {:full-name "Atn" :dob "2000-01-01"}))



(comment 
  (create-patient)
  (get-patient-by-id 10)
  (get-patients {})
  (delete-patient-by-id 7)
  (update-patient-by-id 6 {:full-name "Atnot", :gender "f"})

  
  )