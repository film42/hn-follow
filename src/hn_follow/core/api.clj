(ns hn-follow.core.api
  (:require [cheshire.core :refer :all]
            [taoensso.carmine :as car :refer (wcar)]))

(def redis-conn {:pool {} :spec {:uri (or (System/getenv "REDIS_URL")
                                          "redis://localhost:6379/")}})
(defmacro redis* [& body] `(car/wcar redis-conn ~@body))

(defn- cache-get [key]
  (redis* (car/get key)))

(defn- cache-set
  ([key value] (cache-set key value 300))
  ([key value timeout]
     (redis* (car/set key value)
             (car/expire key timeout))))

(def base-url "https://hacker-news.firebaseio.com/v0/")

(defn get-user [username]
  (let [key (str "##!user#" username)
        data (cache-get key)]
    (if (nil? data)
      (let [response (parse-string
                      (slurp (str base-url "user/" username ".json")))]
        (cache-set key response)
        response)
      data)))

;; TODO: Cache here and not the interaction tree so we can share cache
;; among all users. Much better!
(defn get-item [id]
  (parse-string
   (slurp (str base-url "item/" id ".json"))))

(defn interactions
  ([user] (interactions user 10))
  ([user n] (take n (user "submitted"))))

(defn parent-item-tree [item-id]
  (let [item (get-item item-id)]
      (loop [acc []  x item]
          (if (contains? x "parent")
            (recur (conj acc x)
                   (get-item (x "parent")))
            (conj acc x)))))

(defn interaction-tree
  ([user] (interaction-tree user 10))
  ([user n]
     (let [key (str "##!interactions#" (user "id"))
           data (cache-get key)]
       (if (nil? data)
         (let [response (pmap
                         #(hash-map :tree (parent-item-tree %))
                         (interactions user n))]
           (cache-set key response)
           response)
         data))))
