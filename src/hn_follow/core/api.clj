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

(defn get-user [username]
  (cacheable (str "##!user#" username)
                  (parse-string
                   (slurp (str base-url "user/" username ".json")))))

(defn get-item [id]
  (cacheable (str "##!item#" id)
             (parse-string
              (slurp (str base-url "item/" id ".json")))))

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
      (interactions (or user {}) n))))

;
; Realtime API
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-updates
  "Return response regardless of condition"
  [] (parse-string (slurp (str base-url "/updates.json")) true))

(defprotocol IPoller
  "Poller interface for polling an API request or other observable function"
  (start [_] "Start the agent poller")
  (stop [_] "Stop the agent poller")
  (errors [_] "Agent errors from poller")
  (register [_ cb] "Register a callback with the poller"))

(defn poller
  "Run a poller loop for some agent using some observable function `f` and some delay time in seconds"
  [f delay]
  (let [cond-var (atom true)
        state (atom {})
        callbacks (atom [])
        status (agent {})]
    ;; Create new poller object
    (reify IPoller
      ;; Start the poller
      (start [_]
        (send-off status
         (fn [_] (while @cond-var
                  (let [resp (f)]
                    ;; Alert registered callbacks of messages
                    (when-not (or (nil? resp) (= resp @state))
                      (reset! state resp)
                      (doseq [cb @callbacks]
                        (cb @state))))
                  ;; Wait for delay duration                
                  (Thread/sleep (* delay 1000))))))

      ;; Stop the poller from running
      (stop [_]
        (reset! cond-var false)
        (not @cond-var))

      (errors [_]
        (agent-errors status))
      
      ;; Register a callback
      (register [_ cb] (swap! callbacks conj cb)))))

(defn poll-updates []
  ;; We can rest for 30-seconds because that's about
  ;; how long the API interval is +/- 5-seconds.                
  (poller get-updates 30))

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
        (get-item id)
        (println "Reloaded key: " key)))))

