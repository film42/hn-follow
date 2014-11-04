(ns hn-follow.core.account
  (:require [hn-follow.core.cache :refer :all]))

(def max-account-size 10)

(defn- save [request]
  (let [username (keyword (request :username))
        follow (set (request :follow))]
    (db-set username follow)
    true))

(defn- success [reason]
  {:status :success
   :reason reason})

(defn- error [reason]
  {:status :error
   :reason reason})

(defn following [username]
  {:username username
   :follow (or (db-get (keyword username))
               [])})

(defn update [request]
  "Update the follower list of a user"
  (cond
   (not (contains? request :username))            (error "No username")
   (not (contains? request :follow))              (error "No followers list")
   (empty? (request :username))                   (error "Empty username")
   (> (count (request :follow)) max-account-size) (error "User list too big")
   :else (if (save request)
           (success "Successfully saved!")
           (error "Could not save"))))

(defn all []
  {:users
   (db-keys "[a-zA-Z0-9]*")})
