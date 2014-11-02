(ns hn-follow.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [hn-follow.core.api :as api]
            [hn-follow.core.account :as account]
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
      :body (generate-string resp)}))

(defroutes app-routes
  (GET "/" []
       (slurp "resources/public/follow.html"))

  (context "/api" []
           (GET "/i/:user" [user]
                (json {:interactions
                       (-> user api/get-user (api/interaction-tree 5))}))
           
           (POST "/u" {body :body}
                 (let [request (parse-string (slurp body) true)]
                   (json (account/update request))))

           (GET "/f" []
                (json (account/all)))

           (GET "/a/:username" [username]
                (json (account/following username))))

  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes hn-defaults))
