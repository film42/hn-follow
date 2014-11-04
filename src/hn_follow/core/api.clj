(ns hn-follow.core.api
  (:require [cheshire.core :refer :all]
            [hn-follow.core.cache :refer :all]))

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

(defn get-item [id]
  (let [key (str "##!item#" id)
        data (cache-get key)]
    (if (nil? data)
      (let [response (parse-string
                      (slurp (str base-url "item/" id ".json")))]
        (cache-set key response)
        response)
      data)))

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
     (pmap
      #(hash-map :tree (parent-item-tree %))
      (interactions user n))))
