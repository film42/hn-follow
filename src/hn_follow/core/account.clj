(ns hn-follow.core.account
  (:require [digest :refer [sha-256]]
            [hn-follow.core.cache :refer :all]))

(def max-account-size 10)

(defn- save [request]
  (let [prior (db-get (request :username))
        username (keyword (request :username))
        follow (set (request :follow))
        passd (sha-256 (request :password))
        new_passd (sha-256 (request :new_password))
        email (request :email)]
    (if (or (nil? prior)                  ;; No such prior user
            (nil? (prior :password))      ;; Prior user didn't have a password
            (= passd (prior :password)))  ;; Prior's password matches
      ;; Authenticated
      (do
        (db-set username {:follow follow
                          :password (if-not (nil? new_passd) new_passd passd)
                          :email email})
        true)
      ;; Failed to authenticate
      false)))

(defn- success [reason]
  {:status :success
   :reason reason})

(defn- error [reason]
  {:status :error
   :reason reason})

(defn following [username]
  (let [user (db-get (keyword username))]
    {:username username
     :follow (cond
              (set? user) user ;; Hack to allow the old way to keep working
              (not (nil? user)) (user :follow)
              :else [])
     :email (cond (set? user) nil
                  (nil? user) nil
                  :else (user :email))}))

(defn update [request]
  "Update the follower list of a user"
  (cond
   (not (contains? request :username))            (error "No username")
   (empty? (request :username))                   (error "Empty username")
   (not (contains? request :password))            (error "No password field provided")
   (not (contains? request :follow))              (error "No followers list")
   (not (sequential? (request :follow)))          (error "Followers must be an array")
   (empty? (request :follow))                     (error "Must have at least one follower")
   (> (count (request :follow)) max-account-size) (error "User list too big")
   :else (if (save request)
           (success "Successfully saved!")
           (error "Could not authenticate!"))))

(defn all []
  {:users
   (db-keys "[a-zA-Z0-9]*")})
