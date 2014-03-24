# stch.stateful

Stateful cookie and session middleware for Clojure Ring apps.

Based on code from https://github.com/noir-clojure/lib-noir.

## Installation

Add the following to your project dependencies:

```clojure
[stch-library/stateful "0.1.0"]
```

## API Documentation

http://stch-library.github.io/stateful

Note: This library uses [stch.schema](https://github.com/stch-library/schema). Please refer to that project page for more information regarding type annotations and their meaning.

## How to use

Cookies

```clojure
(ns my.ns
  (:require [stch.stateful.cookies :as cookie
             :refer [wrap-cookies-stateful]]))

(defn handler [req]
  (cookie/get "session-id"))

; or

(defn handler [req]
  (cookie/put! "session-id" "12345"))

(defn site []
  (-> handler
      wrap-cookies-stateful))

; Start some web server
(serve site)
```

Sessions

```clojure
(ns my.ns
  (:require [stch.stateful.session :as session
             :refer [wrap-session-stateful]]))

(defn handler [req]
  (session/get "user"))

; or

(defn handler [req]
  (session/put! "user" {:name "Billy" :id 12345}))

(defn site []
  (-> handler
      wrap-session-stateful))

; Start some web server
(run-server (site) {:port 9999})
```

Combined

```clojure
(defn site []
  (-> handler
      wrap-session-stateful
      wrap-cookies-stateful))

; Start some web server
(run-server (site) {:port 9999})
```

## Unit-tests

Run "lein spec"







