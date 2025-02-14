(ns patient.env)

(def envvars (clojure.edn/read-string (slurp "env.edn")))

(defn env [k]
  (or (k envvars) (System/getenv (name k))))

(comment
  (name :HOME)
  (format "//localhost:%s/%s" (env :DB_PORT) (env :DB_NAME))
  (System/getenv "HOME"))