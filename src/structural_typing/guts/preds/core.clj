(ns ^:no-doc structural-typing.guts.preds.core
  "Preds that are used througout"
  (:use structural-typing.clojure.core)
  (:require [structural-typing.guts.preds.wrap :as wrap]
            [structural-typing.guts.expred :as expred]
            [structural-typing.assist.oopsie :as oopsie]
            [such.metadata :as meta]
            [defprecated.core :as depr]))

(defn- rejects-missing-and-nil [pred]
  (meta/assoc pred ::special-case-handling :reject-missing-and-nil))

(defn special-case-handling [pred]
  (meta/get pred ::special-case-handling :none))

(defn rejects-missing-and-nil? [pred]
  (= (special-case-handling pred) :reject-missing-and-nil))

(def required-path
  "False iff a key/path does not exist or has value `nil`. 
   
   Note: At some point in the future, this library might make a distinction
   between a `nil` value and a missing key. If so, this predicate will change
   to accept `nil` values. See [[not-nil]].
"
  (-> (expred/->ExPred (comp not nil?)
                       "required-path"
                       #(format "%s must exist and be non-nil"
                                (oopsie/friendly-path %)))
      (wrap/lift-expred [:check-nil])
      rejects-missing-and-nil))


(def not-nil
  "False iff a key/path does not exist or has value `nil`. 
   
   Note: At some point in the future, this library might make a distinction
   between a `nil` value and a missing key. If so, this predicate will change
   to reject `nil` values but be silent about missing keys. See [[required-path]].
"
  (-> (expred/->ExPred (comp not nil?)
                       "not-nil"
                       #(format "%s is nil, and that makes Sir Tony Hoare sad"
                                (oopsie/friendly-path %)))
      (wrap/lift-expred [:check-nil])
      rejects-missing-and-nil))
