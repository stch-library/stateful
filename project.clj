(defproject stch-library/stateful "0.1.0"
  :description
  "Stateful cookie and session middleware for Ring apps."
  :url "https://github.com/stch-library/stateful"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.2"]
                 [stch-library/schema "0.3.3"]]
  :profiles {:dev {:dependencies [[speclj "3.0.2"]]}}
  :plugins [[speclj "3.0.2"]
            [codox "0.6.7"]]
  :codox {:src-dir-uri "https://github.com/stch-library/stateful/blob/master/"
          :src-linenum-anchor-prefix "L"}
  :test-paths ["spec"])
