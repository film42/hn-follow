(ns hn-follow.core.api
  (:require [cheshire.core :refer :all]
            [hn-follow.core.cache :refer :all]
            [hn-follow.core.poller :as poller]))

(def base-url "https://hacker-news.firebaseio.com/v0/")

(defmacro cacheable [key stmt]
  `(let [~'data (cache-get ~key)]
    (if (nil? ~'data)
      (let [~'response ~stmt]
        (cache-set ~key ~'response)
        ~'response)
      ~'data)))

(defn get-user [username]
  (cacheable (str "##!user#" username)
                  (parse-string
                   (slurp (str base-url "user/" username ".json")))))

(defn get-item [id]
  (cacheable (str "##!item#" id)
             (parse-string
              (slurp (str base-url "item/" id ".json")))))

(defn get-updates
  "Return response regardless of condition"
  [] (parse-string (slurp (str base-url "/updates.json")) true))

(defn interactions
  [user] (user "submitted"))

(defn parent-item-tree [item-id]
  (let [item (get-item item-id)]
      (loop [acc []  x item]
          (if (contains? x "parent")
            (recur (conj acc x)
                   (get-item (x "parent")))
            (conj acc x)))))

(defn interaction-tree
  "Return the interaction tree for users. Allow to skip for pagination support"
  ([user] (interaction-tree user 0 10))
  ([user skip n]
     (pmap
      #(hash-map :tree (parent-item-tree %))
      (take n (drop skip
                    (interactions (or user {})))))))

;
; Realtime API
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;
;; Callbacks
;;
(defn reload-items [updates]
  (doseq [id (updates :items)]
    (let [key (str "##!item#" id)]
      (when-not (empty? (db-keys key))
        ;; Purge if existst
        (cache-delete key)
        ;; Restore the item
        (get-item id)
        (println "Reloaded key: " key)))))

(defn reload-users [updates]
  (doseq [id (updates :profiles)]
    (let [key (str "##!user#" id)]
      (when-not (empty? (db-keys key))
        ;; Purge if existst
        (cache-delete key)
        ;; Restore the user
        (get-user id)
        (println "Reloaded key: " key)))))

;;
;; Poller
;;
(defn poll-updates []
  ;; We can rest for 20-seconds, because HN refreshes about
  ;; every 30 +/- 5-seconds.                
  (poller/poller get-updates 20))

(defn poll-updates-defaults []
  (let [instance (poller/poller get-updates 30)]
    (poller/register instance reload-items)
    (poller/register instance reload-users)
    (poller/start instance)
    ;; Rerturn poller instance
    instance))
