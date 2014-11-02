(ns hn-follow.core.account)

(def accounts (atom {}))

(def max-account-size 10)

(defn- save [request]
  (let [username (keyword (request :username))
        follow (request :follow)]
    (swap! accounts assoc username follow)
    true))

(defn- success [reason]
  (println accounts)
  {:status :success
   :reason reason})

(defn- error [reason]
  {:status :error
   :reason reason})

(defn following [username]
  {:username username
   :follow (or (@accounts (keyword username)) [])})

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
