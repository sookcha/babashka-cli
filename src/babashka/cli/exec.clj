(ns babashka.cli.exec
  (:require
   [babashka.cli :refer [coerce parse-args]]
   [clojure.edn :as edn]))

(defn -main
  "Main entrypoint for command line usage.
  Expects a namespace and var name followed by zero or more key value pair arguments.

  Example when used as a clojure CLI alias:
  ``` clojure
  clojure -M:exec clojure.core prn :a 1 :b 2
  ;;=> {:a \"1\" :b \"2\"}
  ```"
  [& args]
  (let [[f & args] args
        basis (some-> (System/getProperty "clojure.basis")
                      slurp
                      edn/read-string)
        resolve-args (:resolve-args basis)
        exec-args (:exec-args resolve-args)
        f (coerce f symbol)
        ns (namespace f)
        fq? (some? ns)
        ns (or ns f)
        ns (coerce ns symbol)
        [f args] (if fq?
                   [f args]
                   [(symbol (str ns) (first args)) (rest args)])
        f (requiring-resolve f)
        opts (:org.babashka/cli (meta f))
        opts (merge opts (:org.babashka/cli resolve-args))
        opts (:opts (parse-args args opts))
        opts (merge exec-args opts)]
    (try (f opts)
         (finally (shutdown-agents)))))
