;Emails updates to subscribers.
(ns hn-follow.core.mailer
  (:require [postal.core :refer :all]))

(defn get-update [username last-update]

  )

(defn SendUpdate [email username last-update]
  (let [message (get-update username last-update)]
    (send-message {:host "smtp.gmail.com"
               :user "crewbie4life"
               :pass "XXX"
               :ssl :yes!!!11}
              {:from "crewbie4life@gmail.com"
               :to ["film42@gmail.com"]
               :subject "hn-follow update" ;What do we want to send as a subject?
               :body message})
    )
  )

(send-message {:host "smtp.gmail.com"
               :user "crewbie4life"
               :pass "XXX"
               :ssl :yes!!!11}
              {:from "crewbie4life@gmail.com"
               :to ["film42@gmail.com"]
               :subject "postal.core email"
               :body "Hey, so I sent you this with clojure ^.^"})