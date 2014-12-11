;Emails updates to subscribers.
(ns hn-follow.core.mailer
  (:require [hn-follow.core.api :as api]
            [postal.core :as postal]
            [hn-follow.core.account :as account]))


(defn get-email-feed [username]
  (loop [feed []
         feed-list (map api/get-user (:follow (account/following username)))]
    (if (empty? feed-list)
      (flatten feed)
      (recur (conj feed (api/week-feed (first feed-list))) (rest feed-list)))))

(defn send-update [email username]
  (let [message (apply str (get-email-feed username))]
    (postal/send-message {:host "smtp.gmail.com"
               :user "hnfollow"
               :pass "hackernewsfollow"
               :ssl true}
              {:from "hnfollow@gmail.com"
               :to [email]
               :subject (str "hn-follow update for " username) ;What do we want to send as a subject?
               :body message})))

;(send-update "crewbie4life@gmail.com" "qzcx")



