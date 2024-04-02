(ns frontend.core
  (:require [helix.core :refer [defnc $]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            ["react-dom/client" :as rdom]
            [ajax.core :refer [GET POST PATCH DELETE]]
            [clojure.string :as string] 
            ))

(defn snake->kebab
  ([k]
   (snake->kebab k false))
  ([k is-keyword?]
   (let [result (-> k
                    name
                    (string/replace "_" "-"))]
     (if is-keyword?
       (keyword result)
       result))))


(defn map-to-string [m]
  (->> m
       (filter (fn [[k v]] (not (nil? v))))
       (map (fn [[k v]] (str (snake->kebab (name k)) "_" 
                             (string/upper-case (name v)))))
       (string/join ",")))

(defn format-date [date-obj]
  (let [year (.getFullYear date-obj)
            month (-> (.getMonth date-obj) inc)
            day (.getDate date-obj)]
        (str
         year "-"
         (if (< month 10) (str "0" month) month) "-"
         (if (< day 10) (str "0" day) day)))
  )

(defnc TableCell
  [{:keys [column-name content id]}] 
  (let [[is-editing? set-editing?] (hooks/use-state false) ;; Modified line
        [edited-content set-content] (hooks/use-state (cond 
                                                      (= column-name :dob) (when-not (nil? content) (format-date (js/Date. content)))
                                                      :else (str content)))]
    (defn save-editing [value]
      (set-content value)
      (PATCH (str "/api/patients/" id) {:params {(snake->kebab column-name true) value}} :format :json))
    (if is-editing?
      (d/div {:class-name "cell"}
              (d/input {:value edited-content
                        :type (if (= column-name :dob) "date" "text")
                        :on-change #(save-editing (.. % -target -value))
                        :on-blur #(set-editing? false)}))
      (d/div {:class-name "cell" :on-click (fn [] (set-editing? true))}
                (cond 
                  (= column-name :dob) (when-not (nil? content) (format-date (js/Date. edited-content)))
                  :else edited-content)))))

(defnc SortIcon
  [{:keys [sort]}]
  (when sort
    (let [icon (if (= sort :asc) "▲" "▼")]
      (d/div icon))))

(defnc Table
  [{:keys [data sort click-sort-button delete-patient-by-id]}]
  (let [columns [:full_name :address :dob :gender :oms]]
    (d/table 
      (d/thead 
        (d/tr 
          (for [col columns]
            (d/th {:key col :class-name "th"} 
                  (d/div {class-name "column-name" :on-click (fn [] (click-sort-button col))}
                   (d/span (name col))
                   ($ SortIcon {:sort (get sort col)}))))
         (d/th {:class-name "th"} "")))
      (d/tbody 
        (for [row data
              :let [id (get row :id)]]
          (d/tr {:class-name "row" :key id}
                (for [col columns
                      :let [content (get row col)]]
                  (d/td {:key (name col)} ($ TableCell {:column-name col :content content :id id})))
                (d/td {:class-name "btn-delete"}
                      (d/button {:on-click #(delete-patient-by-id id)} "Delete"))))))))

(defnc App []
  (let [[state set-state] (hooks/use-state [])
        [search set-search] (hooks/use-state "")
        [sort set-sort] (hooks/use-state {})]
    (defn load-patients []
      (println (str "test" sort))
      (let [params {:search search :order (map-to-string sort)}
            handler (fn [res] (set-state res))]
        (GET "/api/patients" {:params params
                              :handler handler})))
    (defn create-patient []
      (let [handler (fn [res] (load-patients))]
        (POST "/api/patients" {:handler handler})))
    (hooks/use-effect
     [search sort]
     (load-patients))
    (hooks/use-effect
     :once
     (load-patients))
    (d/div
     (d/h1 "Table of patients")
     (d/button {:class-name "btn-new-row" :on-click #(create-patient)} "Add new row")
     (d/input {:type "text"
               :value search
               :placeholder "Search..."
               :on-change (fn [event]
                            (let [input-value (.. event -target -value)]
                              (set-search input-value)))})
     (if state
       ($ Table {:data state
                 :sort sort
                 :click-sort-button (fn [column-name]
                                      (let [sort-column (get sort column-name)]
                                        (set-sort (assoc sort column-name (cond
                                                                            (nil? sort-column) :asc
                                                                            (= sort-column :asc) :desc
                                                                            (= sort-column :desc) nil)))))
                 :delete-patient-by-id (fn [id]
                                          (let [handler (fn [res] (load-patients))]
                                            (DELETE (str "/api/patients/" id) {:handler handler})))})
       (d/p "Loading...")))))

;; (defonce root (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init []
  (let [root (rdom/createRoot (js/document.getElementById "app"))]
    (.render root ($ App))))