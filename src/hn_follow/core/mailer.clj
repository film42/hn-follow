;Emails updates to subscribers.
(ns hn-follow.core.mailer
  (:require [hn-follow.core.api :as api]
            [hn-follow.core.views :as views]
            [postal.core :as postal]
            [hn-follow.core.account :as account]))

;get an list of interaction feed objects from the last week
(defn get-email-feed [username]
  (let [following (:follow (account/following username))]
    (loop [feed []
           feed-list (pmap api/get-user following)]
      (if (empty? feed-list)
        (sort-by :time (flatten feed))
        (recur
         (conj feed (api/week-feed (first feed-list))) (rest feed-list))))))

;send and email with a email feed for the previous week's activity.
(defn send-email-update [email username]
  (let [user (api/get-user username)
        interactions (get-email-feed username)]
    (postal/send-message {:host "smtp.gmail.com"
                          :user "hnfollow"
                          :pass "hackernewsfollow"
                          :ssl true}
                         {:from "hnfollow@gmail.com"
                          :to [email]
                          :subject (str "HN Follow weekly digest for " username)
                          :body [:alternative {:type "text/html"
                                               :content (views/email-template user interactions)}]})))

(defn send-registered-users-update-email []
  (let [users (:users (account/all))]
    (pmap
     #(let [user (account/following %)]
        ;; Send email
        (when (not (nil? (:email user)))
          (send-email-update (:email user) %)
          (println (str "Digest sent to " %)))) users))
  nil)
