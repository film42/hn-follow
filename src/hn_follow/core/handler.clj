(ns hn-follow.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [hn-follow.core.api :as api]
            [hn-follow.core.account :as account]
            [hn-follow.core.views :as views]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def hn-defaults
  (assoc api-defaults :static {:resources "public"}
                      :cookies   true
                      :session   {:flash true
                                  :cookie-attrs {:http-only true}}))

(defn json
  ([resp] (json resp 200) )
  ([resp status]
     {:status status
      :headers {"content-type" "application/json"}
      :body (generate-string resp {:escape-non-ascii true})}))

;;
;; HN Updates Poller (async)
;;
(def hn-poller (api/poll-updates))
(api/register hn-poller api/reload-items)
(api/register hn-poller api/reload-users)
(api/start hn-poller)
(println "Started HN Update Poller")

;;
;; Application Routes
;;
(defroutes app-routes
  (GET "/" []
       (views/home-page))

  (context "/api" []
           (GET "/i/:user" [user]
                (json {:interactions
                       (-> user api/get-user (api/interaction-tree 5))}))
           
           (POST "/u" {body :body}
                 (let [request (parse-string (slurp body) true)]
                   (json (account/update request))))

           (GET "/users" []
                (json (account/all)))

           (GET "/a/:username" [username]
                (json (account/following username))))

  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes hn-defaults))
