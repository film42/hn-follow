(ns hn-follow.core.cache
  (:require [taoensso.carmine :as car :refer (wcar)]))

(defn- url []
  (or (System/getenv "REDIS_URL") "redis://localhost:6379/"))

(def default-connection
  {:pool {:max-total 10} :spec {:uri (url)}})

(defmacro redis* [& body] `(car/wcar default-connection ~@body))

(defn cache-get [key]
  (redis* (car/get key)))

(defn cache-set
  ([key value] (cache-set key value 1200)) ;; 20 min
  ([key value timeout]
     (redis* (car/set key value)
             (car/expire key timeout))))

(defn db-get [key]
  (cache-get key))

(defn db-set [key value]
  (redis* (car/set key value)))

(defn db-keys [query]
  (redis* (car/keys query)))
