(ns hn-follow.core.api
  (:require [cheshire.core :refer :all]
            [hn-follow.core.cache :refer :all]))

(def base-url "https://hacker-news.firebaseio.com/v0/")

(defmacro cacheable [key stmt]
  `(let [~'data (cache-get ~key)]
    (if (nil? ~'data)
      (let [~'response ~stmt]
        (cache-set ~key ~'response)
        ~'response)
      ~'data)))

;Gets information about a user from hacker news api
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

(defn- tree-template [tree]
  (let [root-item (first (filter #(= "story" (% "type")) tree))
        latest (first tree)]
    {:by (latest "by")
     :id (latest "id")
     :parent (latest "parent")
     :root (root-item "id")
     :type (latest "type")
     :title (root-item "title")
     :url (root-item "url")
     :time (latest "time")
     :text (latest "text")}))

(defn interaction-feed
  ([user] (interaction-feed user 0 10))
  ([user skip n]
     (let [tree (interaction-tree user skip n)
           clean (map #(% :tree) tree)
           ;; Remove any deleted comments now
           valid (map #(filter (fn [x] (nil? (x "deleted"))) %) clean)
           non-empty (filter #(not (empty? %)) valid)] ;;this is a bad way to check for deleted stories
       (map tree-template non-empty))))

(defn time-seconds []
  (quot (System/currentTimeMillis) 1000))

(defn filter-one-week [feed]
  (let[cur-time (time-seconds)
      one-week (* 60 60 24 7)]
    (filter (fn [x] (> (x :time) (- cur-time one-week))) feed)))

(defn week-feed [user]
  (loop [feed-list []
         skip 0]
    (let [feed (filter-one-week (interaction-feed user skip 10))]
      (if (empty? feed)
        feed-list
       (recur (conj feed-list feed) (+ skip 10))))))

;
; Realtime API
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn reload-items [items]
  (doseq [id items]
    (let [key (str "##!item#" id)]
      (when-not (empty? (db-keys key))
        ;; Purge if existst
        (cache-delete key)
        ;; Restore the item
        (get-item id)
        (println "Reloaded key: " key)))))

(defn reload-users [users]
  (doseq [id users]
    (let [key (str "##!user#" id)]
      (when-not (empty? (db-keys key))
        ;; Purge if existst
        (cache-delete key)
        ;; Restore the user
        (get-user id)
        (println "Reloaded key: " key)))))

;; TODO: Make this update state cleaner
(def -update-state (atom {}))
(defn sync-updates []
  (try
    (let [latest (get-updates)]
      (when-not (= @-update-state latest)
        (reload-items (or (latest :items)    []))
        (reload-users (or (latest :profiles) []))
        (reset! -update-state latest)))
    (catch
        Exception e (println "Exception in Sync Updates:" (.getMessage e)))))

