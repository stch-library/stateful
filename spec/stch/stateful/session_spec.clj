(ns stch.stateful.session-spec
  (:use [speclj.core]
        [stch.schema :only [with-fn-validation]])
  (:require [stch.stateful.session :as session
             :refer [stateful-session]]))

(defn req
  ([] (req {}))
  ([session]
   {:uri "/"
    :request-method :get
    :server-name "localhost"
    :scheme :http
    :headers {}
    :session session}))

(def resp
   {:status 200
    :headers {}
    :body "OK"})

(describe "stch.stateful.session"
  (around [it]
    (with-fn-validation (it)))
  (context "get"
    (it "one arg"
      (should= (assoc resp :body "Billy")
               ((stateful-session
                 (fn [req]
                   (assoc resp
                     :body (session/get "name"))))
                (req {"name" "Billy"}))))
    (it "two args"
      (should= (assoc resp :body "Billy")
               ((stateful-session
                 (fn [req]
                   (assoc resp
                     :body (session/get "name" "Billy"))))
                (req)))))
  (context "get-in"
    (it "one arg"
      (should= (assoc resp :body "LA")
               ((stateful-session
                 (fn [req]
                   (assoc resp
                     :body (session/get-in ["address" "city"]))))
                (req {"address" {"city" "LA"}}))))
    (it "two args"
      (should= (assoc resp :body "LA")
               ((stateful-session
                 (fn [req]
                   (assoc resp
                     :body (session/get-in ["address" "city"]
                                           "LA"))))
                (req {"address" {"state" "CA"}})))))
  (context "get!"
    (it "one arg"
      (should= (assoc resp
                 :body "Billy"
                 :session {})
               ((stateful-session
                 (fn [req]
                   (assoc resp
                     :body (session/get! "name"))))
                (req {"name" "Billy"}))))
    (it "two args"
      (should= (assoc resp :body "Billy")
               ((stateful-session
                 (fn [req]
                   (assoc resp
                     :body (session/get! "name" "Billy"))))
                (req)))))
  (context "get-in!"
    (it "one arg"
      (should= (assoc resp
                 :body "LA"
                 :session {"address" {"state" "CA"}})
               ((stateful-session
                 (fn [req]
                   (assoc resp
                     :body (session/get-in! ["address" "city"]))))
                (req {"address" {"city" "LA"
                                 "state" "CA"}}))))
    (it "two args"
      (should= (assoc resp :body "LA")
               ((stateful-session
                 (fn [req]
                   (assoc resp
                     :body (session/get-in! ["address" "city"]
                                           "LA"))))
                (req {"address" {"state" "CA"}})))))
  (context "put!"
    (it "new key"
      (should= (assoc resp :session {"name" "Billy"})
               ((stateful-session
                 (fn [req]
                   (session/put! "name" "Billy")
                   resp))
                (req))))
    (it "key/value identical"
      (should= resp
               ((stateful-session
                 (fn [req]
                   (session/put! "name" "Billy")
                   resp))
                (req {"name" "Billy"}))))
    (it "existing key, new value"
      (should= (assoc resp :session {"name" "Bobby"})
               ((stateful-session
                 (fn [req]
                   (session/put! "name" "Bobby")
                   resp))
                (req {"name" "Billy"})))))
  (context "assoc-in!"
    (it "missing keys"
      (should= (assoc resp :session {"address" {"city" "LA"}})
                 ((stateful-session
                   (fn [req]
                     (session/assoc-in! ["address" "city"] "LA")
                     resp))
                  (req))))
    (it "existing keys"
      (should= (assoc resp :session {"address" {"city" "San Diego"}})
                 ((stateful-session
                   (fn [req]
                     (session/assoc-in! ["address" "city"] "San Diego")
                     resp))
                  (req {"address" {"city" "LA"}})))))
  (it "dissoc-in!"
    (should= (assoc resp :session {"address" {"state" "CA"}})
             ((stateful-session
               (fn [req]
                 (session/dissoc-in! ["address" "city"])
                 resp))
              (req {"address" {"city" "LA"
                               "state" "CA"}}))))
  (context "update!"
    (it "two args"
      (should= (assoc resp :session {"counter" 1})
               ((stateful-session
                 (fn [req]
                   (session/update! "counter" inc)
                   resp))
                (req {"counter" 0}))))
    (it "three args"
      (should= (assoc resp
                 :session {"users" ["Billy" "Bobby"]})
               ((stateful-session
                 (fn [req]
                   (session/update! "users" conj "Bobby")
                   resp))
                (req {"users" ["Billy"]})))))
  (context "update-in!"
    (it "two args"
      (should= (assoc resp :session {1234 {"counter" 1}})
               ((stateful-session
                 (fn [req]
                   (session/update-in! [1234 "counter"] inc)
                   resp))
                (req {1234 {"counter" 0}}))))
    (it "three args"
      (should= (assoc resp
                 :session {"users" {1234 ["Pizza" "Burgers"]}})
               ((stateful-session
                 (fn [req]
                   (session/update-in! ["users" 1234] conj "Burgers")
                   resp))
                (req {"users" {1234 ["Pizza"]}})))))
  (it "remove!"
    (should= (assoc resp :session {})
             ((stateful-session
               (fn [req]
                 (session/remove! "name")
                 resp))
              (req {"name" "Billy"}))))
  (it "clear!"
    (should= (assoc resp :session {})
             ((stateful-session
               (fn [req]
                 (session/clear!)
                 resp))
              (req {"name" "Billy"}))))
  (it "destroy!"
    (should= (assoc resp :session nil)
             ((stateful-session
               (fn [req]
                 (session/destroy!)
                 resp))
              (req {"name" "Billy"})))))


















