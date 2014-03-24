(ns stch.stateful.cookies-spec
  (:use [speclj.core]
        [stch.schema :only [with-fn-validation]])
  (:require [stch.stateful.cookies :as cookie
             :refer [wrap-cookies-stateful]]))

(defn req
  ([] (req {}))
  ([cookies]
   {:uri "/"
    :request-method :get
    :server-name "localhost"
    :scheme :http
    :headers {}
    :cookies cookies}))

(def resp
   {:status 200
    :headers {}
    :body "OK"})

(describe "stch.stateful.cookies"
  (around [it]
    (with-fn-validation (it)))
  (it "cookies"
    (should= (assoc resp :body "Billy")
             ((wrap-cookies-stateful
               (fn [req]
                 (let [c (cookie/cookies)
                       v (get-in c ["name" :value])]
                   (assoc resp :body v))))
              (req {"name" {:value "Billy"}}))))
  (context "get"
    (it "one arg"
      (should= (assoc resp :body "Billy")
               ((wrap-cookies-stateful
                 (fn [req]
                   (assoc resp :body (cookie/get "name"))))
                (req {"name" {:value "Billy"}}))))
    (it "two args"
      (should= (assoc resp :body "Billy")
               ((wrap-cookies-stateful
                 (fn [req]
                   (assoc resp :body (cookie/get "name" "Billy"))))
                (req)))))

  (context "put!"
    (it "string v"
      (should= (assoc resp :headers {"Set-Cookie" '("name=Billy;Path=/")})
               ((wrap-cookies-stateful
                 (fn [req]
                   (cookie/put! "name" "Billy")
                   resp)) (req))))
    (it "map v"
      (should= (assoc resp :headers {"Set-Cookie" '("name=Billy;Path=/admin")})
               ((wrap-cookies-stateful
                 (fn [req]
                   (cookie/put! "name" {:value "Billy" :path "/admin"})
                   resp)) (req)))))
  (context "update!"
    (it "two args"
      (should= (assoc resp :headers {"Set-Cookie" '("counter=2;Path=/")})
               ((wrap-cookies-stateful
                 (fn [req]
                   (cookie/update! "counter"
                                   #(inc (Integer/parseInt %)))
                   resp))
                (req {"counter" {:value "1"}}))))
    (it "three args"
      (should= (assoc resp :headers {"Set-Cookie" '("num=123;Path=/")})
               ((wrap-cookies-stateful
                 (fn [req]
                   (cookie/update! "num" str "3")
                   resp))
                (req {"num" {:value "12"}})))))
  (context "remove!"
    (it "one arg"
      (should= (assoc resp :headers {"Set-Cookie" '("name=;Path=/;Expires=Thu, 01-Jan-1970 00:00:01 GMT")})
               ((wrap-cookies-stateful
                 (fn [req]
                   (cookie/remove! "name")
                   resp))
                (req {"name" {:value "Billy"}}))))
    (it "two args"
      (should= (assoc resp :headers {"Set-Cookie" '("name=;Path=/admin;Expires=Thu, 01-Jan-1970 00:00:01 GMT")})
               ((wrap-cookies-stateful
                 (fn [req]
                   (cookie/remove! "name" {:path "/admin"})
                   resp))
                (req {"name" {:value "Billy" :path "/admin"}}))))))









