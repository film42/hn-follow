(ns hn-follow.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :refer :all]
            [overtone.at-at :as aa] ; Forked
            [hn-follow.core.api :as api]
            [hn-follow.core.account :as account]
            [hn-follow.core.views :as views]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def hn-defaults
  (assoc api-defaults :static {:resources "public"}
                      :cookies   true
                      :session   {:flash true
                                  :cookie-attrs {:http-only true}}))

(defn str->int [s]
  (if (string? s)
    (let [v (read-string s)]
      (if (integer? v) v 0))
    0))

(defn json
  ([resp] (json resp 200) )
  ([resp status]
     {:status status
      :headers {"content-type" "application/json"}
      :body (generate-string resp {:escape-non-ascii true})}))

;;
;; Process HN Updates (async)
;;
(def hn-tp (aa/mk-pool))
(aa/interspaced 15000 api/sync-updates hn-tp :desc "Check for API Updates")
(aa/show-schedule hn-tp)

;;
;; Application Routes
;;
(defroutes app-routes
  (GET "/" []
       (views/home-page))

  (context "/api" []
           (GET "/i/:user" {params :params}
                (let [page (str->int (params :page))
                      offset (* 5 (dec page))] ;; Dec page so 1->0,2->5
                  (json {:interactions
                         (-> (params :user) api/get-user (api/interaction-tree offset 5))})))
           
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
