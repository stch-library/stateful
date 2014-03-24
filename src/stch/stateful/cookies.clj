(ns stch.stateful.cookies
  "Stateful cookie middleware."
  (:use [ring.middleware.cookies]
        [stch.schema])
  (:refer-clojure :exclude [get])
  (:import [org.joda.time Interval DateTime]))

(def ^:dynamic *req-cookies*)
(def ^:dynamic *resp-cookies*)

(def Cookies
  "Cookies type annotation."
  {String {:value String
           (optional-key :path) String
           (optional-key :domain) String
           (optional-key :max-age) (U Interval Int)
           (optional-key :expires) (U DateTime String)
           (optional-key :secure) Boolean
           (optional-key :http-only) Boolean}})

(def Handler
  "Handler type annotation."
  (Fn Map [Map]))

(defn' cookies :- Cookies
  "Returns all the request cookies."
  []
  *req-cookies*)

(defn' get :- Any
  "Returns the value of the cookie with name k
  from the request."
  ([k :- Named] (get k nil))
  ([k :- Named, default :- Any]
   (if-let [v (get-in *req-cookies* [(name k) :value])]
     v
     default)))

(defn' put! :- Cookies
  "Add a new cookie whose name is k and has the value v.
  If v is a string a cookie map is created with :path '/'. To set
  custom attributes, such as \"expires\", provide a map as v.
  Stores all keys as strings."
  [k :- Named, v :- Any]
  (let [props (if (map? v)
                v
                {:value (str v) :path "/"})]
    (swap! *resp-cookies* assoc (name k) props)))

(defn' update! :- Cookies
  "Update the cookie with name k, calling f and any
  additional args on it's value."
  [k :- Named, f :- (Fn), & args :- [Any]]
  (->> (apply f (get k) args)
       (put! k)))

(defn' remove! :- Cookies
  "Remove the cookie with name k. Optionally pass
  a map with the path and/or domain."
  ([k :- Named] (remove! k {:path "/"}))
  ([k :- Named
    opts :- {(optional-key :path) String
             (optional-key :domain) String}]
   (let [v (merge {:value ""
                   :expires "Thu, 01-Jan-1970 00:00:01 GMT"}
                  opts)]
     (put! k v))))

(defn' stateful-cookies :- Handler
  [handler :- Handler]
  "Stateful cookie handler."
  (fn [req]
    (binding [*req-cookies* (:cookies req)
              *resp-cookies* (atom {})]
      (when-let [resp (handler req)]
        (if (seq @*resp-cookies*)
          (update-in resp [:cookies] merge @*resp-cookies*)
          resp)))))

(defn' wrap-cookies-stateful :- Handler
  "A stateful layer inside wrap-cookies."
  [handler :- Handler]
  (-> handler
      stateful-cookies
      wrap-cookies))








