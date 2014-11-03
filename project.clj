(defproject hn-follow "0.1.0-SNAPSHOT"
  :description "HN Follow - See what your friends on HN are talking about"
  :url ""
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.0"]
                 [cheshire "5.3.1"]
                 [com.taoensso/carmine "2.7.1"]
                 [ring/ring-defaults "0.1.2"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler hn-follow.core.handler/app}
  :uberjar-name "hn-follow-standalone.jar"
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
