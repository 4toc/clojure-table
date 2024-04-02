(defn format-date [date-obj]
  (let [year (.getFullYear date-obj)
        month (-> (.getMonth date-obj) inc)
        day (.getDate date-obj)]
    (str
     year "-"
     (if (< month 10) (str "0" month) month) "-"
     (if (< day 10) (str "0" day) day))))