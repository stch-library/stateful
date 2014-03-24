(ns stch.stateful.session
  "Stateful session middleware."
  (:require [clojure.core :as core])
  (:refer-clojure :exclude [get get-in remove])
  (:use [ring.middleware.session]
        [stch.schema]
        [stch.util :only [dissoc-in]]))

(def ^:dynamic *session*)
(declare remove! dissoc-in!)

(def Handler
  "Handler type annotation."
  (Fn Map [Map]))

(defn' get :- Any
  "Get the key's value from the session, returns nil if it
  doesn't exist."
  ([k :- Any] (get k nil))
  ([k :- Any, default :- Any]
   (core/get @*session* k default)))

(defn' get-in :- Any
  "Gets the value at the path specified by the vector ks
  from the session, returns nil if it doesn't exist."
  ([ks :- [Any]] (get-in ks nil))
  ([ks :- [Any], default :- Any]
   (core/get-in @*session* ks default)))

(defn' get! :- Any
  "Destructive get from the session. Returns the
  current value of the key and then removes it
  from the session."
  ([k :- Any] (get! k nil))
  ([k :- Any, default :- Any]
   (let [v (get k default)]
     (remove! k)
     v)))

(defn' get-in! :- Any
  "Destructive get from the session. This returns the
  current value of the path specified by the vector
  ks and then removes it from the session."
  ([ks :- [Any]] (get-in! ks nil))
  ([ks :- [Any], default :- Any]
   (let [v (core/get-in @*session* ks default)]
     (dissoc-in! ks)
     v)))

(defn' put! :- Map
  "Associates the key with the given value in the session"
  [k :- Any, v :- Any]
  (core/swap! *session* assoc k v))

(defn' assoc-in! :- Map
  "Associates a value in the session, where ks is a
  sequence of keys and v is the new value and returns
  a new nested structure. If any levels do not exist,
  hash-maps will be created."
  [ks :- [Any], v :- Any]
  (core/swap! *session* assoc-in ks v))

(defn' dissoc-in! :- Map
  "Disassociates a value in the session, where ks is
  a sequence of keys."
  [ks :- [Any]]
  (core/swap! *session* dissoc-in ks))

(defn' update! :- Map
  "Update the value at key k, passing the current
  value and args to f."
  ([k :- Any, f :- (Fn Any [Any])]
   (put! k (f (get k))))
  ([k :- Any, f :- (Fn), & args :- [Any]]
   (->> (apply f (get k) args)
        (put! k))))

(defn' update-in! :- Map
  "Updates a value in the session, where ks is a
   sequence of keys and f is a function that will
   take the old value along with any supplied args and return
   the new value. If any levels do not exist, hash-maps
   will be created."
  ([ks :- [Any], f :- (Fn Any [Any])]
   (core/swap! *session* update-in ks f))
  ([ks :- [Any], f :- (Fn), & args :- [Any]]
   (core/swap! *session* update-in ks #(apply f % args))))

(defn' remove! :- Map
  "Remove a key from the session."
  [k :- Any]
  (core/swap! *session* dissoc k))

(defn' clear! :- Map
  "Remove all data from the session and start over cleanly."
  []
  (reset! *session* {}))

(defn' destroy! :- (Eq nil)
  "Destroy the session."
  []
  (reset! *session* nil))

(defn' stateful-session :- Handler
   "Stateful session handler."
  [handler :- Handler]
  (fn [req]
    (let [before (:session req {})]
      (binding [*session* (atom before)]
        (when-let [resp (handler req)]
          (let [after @*session*]
            (if (= before after)
              resp
              (assoc resp :session after))))))))

(defn' wrap-session-stateful :- Handler
  "A stateful layer inside wrap-session.
  Options are passed to wrap-session."
  ([handler :- Handler]
   (wrap-session-stateful handler {}))
  ([handler :- Handler, opts :- Map]
   (-> handler
       stateful-session
       (wrap-session opts))))








