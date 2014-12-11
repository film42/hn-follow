;Emails updates to subscribers.
(ns hn-follow.core.mailer
  (:require [hn-follow.core.api :as api]
            [postal.core :as postal]
            [hn-follow.core.account :as account]))

;get an list of interaction feed objects from the last week
(defn get-email-feed [username]
  (loop [feed []
         feed-list (map api/get-user (:follow (account/following username)))]
    (if (empty? feed-list)
      (sort-by :time
        (flatten feed))
      (recur (conj feed (api/week-feed (first feed-list))) (rest feed-list)))))

;send and email with a email feed for the previous week's activity.
(defn send-email-update [email username]
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

(defn send-registered-users-update-email []
  (loop [users ((account/all) :users)]
    (let [next-user (first users)
          email (:email (account/following next-user))]
      (if (not (nil? email))
        (println (str "update sent to " next-user "mailer response message: "
         (send-email-update email next-user))))
      (if (not(empty?(rest users)))
        (recur (rest users))))))

