(ns structural-typing.mechanics.m-lifting-predicates
  (:require [clojure.repl :as repl]
            [clojure.string :as str]
            [blancas.morph.monads :as e]
            [structural-typing.api.path :as path]
            [structural-typing.api.defaults :as defaults]))

(defn friendly-name [f]
  (cond (var? f)
        (str (:name (meta f)))

        (fn? f)
        (let [basename (-> (str f)
                             repl/demunge
                             (str/split #"/")
                             last
                             (str/split #"@")
                             first
                             (str/split #"--[0-9]+$")
                             first)]
          (if (= basename "fn") "your custom predicate" basename))
        :else
        (str f)))


(letfn [(gm [f k default] (get (meta f) k default))]
  (defn get-predicate [f]        (gm f ::original-predicate f))
  (defn get-predicate-string [f] (gm f ::predicate-string (friendly-name f)))
  (defn get-explainer [f]        (gm f ::error-explainer defaults/default-error-explainer)))

(letfn [(vm [f k v] (vary-meta f assoc k v))
        (ensure-meta [f k v] (if (contains? (meta f) k) f (vm f k v)))]

  (defn stash-defaults [f]
    (-> f
        (ensure-meta ::original-predicate f)
        (ensure-meta ::predicate-string (friendly-name f))))

  (defn replace-predicate-string [f name] (vm f ::predicate-string name))
  (defn replace-explainer [f explainer] (vm f ::error-explainer explainer)))
  

(defn lift [pred]
  (let [diagnostics {:error-explainer (get-explainer pred)
                     :predicate-string (get-predicate-string pred)
                     :predicate (get-predicate pred)}]
    (fn [leaf-value-context]
      (e/make-either (merge diagnostics leaf-value-context)
                     (fn [x] (try (pred x) (catch Exception ex false)))
                     (:leaf-value leaf-value-context)))))