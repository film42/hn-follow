(ns hn-follow.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :refer :all]
            [hn-follow.core.api :as api]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn json
  ([resp] (json resp 200) )
  ([resp status]
     {:status status
      :headers {"content-type" "application/json"}
      :body (generate-string resp)}))

(defroutes app-routes
  (GET "/" [] "Hello World")

  (GET "/i/:user" [user]
       (json {:interactions
              (-> user api/get-user (api/interaction-tree 5))}))
  
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
