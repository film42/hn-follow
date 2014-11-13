(ns hn-follow.core.views
  (:require [hiccup.core :refer :all]))

(defn home-page []
  (html
   [:head
    [:meta {:http-equiv= "Content-Type" :content= "text/html; charset=UTF-8"}]
    [:meta {:name "referrer" :content "origin"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:link {:rel "stylesheet" :type "txt/css" :href "/news.css"}]

    [:script {:type "text/javascript" :src "/jquery-1.11.1.min.js"}]
    [:script {:type "text/javascript" :src "/moment.min.js"}]
    [:script {:type "text/javascript" :src "/follow.js"}]
    [:script {:type "text/javascript" :src "/ga.js"}]

    [:title "HN Follow"]]
   
   [:body
    [:div.page
     [:div.center
      ;; Headings
      [:div.heading]

      ;; Comments
      [:div.comments
       [:ol.comment-list "Loading..."]]

      ;; Forms
      [:div.form
       ;; Go to user form
       [:form.goto
        [:p
         "Go to your follower list. Or check out the "
         [:a {:href "/?user=hn-top-10"} "HN Top 10"]
         " list"]

        [:p "Username: " [:input {:type "text" :name "user"}]]
        [:input {:type "submit" :value "Go"}]]

       ;; Registration or edit form
       [:p.info "Create a username and select the people you'd like to follow."]
       [:form.register
        [:p.user-section "Username: " [:input {:type "text" :name "user"}]]
        [:p.password-section "Password:&nbsp; " [:input {:placeholder "Optional" :type "password" :name "password"}]]
        [:p.new-password-section "New Password: " [:input {:type "password" :name "new_password"}]]
        [:p.new-password-section "Change Password " [:input {:type "checkbox" :name "new_password_check_box"}]]

        [:p "Folllow up to 10 HN users:"]
        [:ol
         (for [i (range 1 11)]
           [:li [:input {:type "text" :name (str "follow" i)}] [:br]])]
        [:input {:type "submit" :value "Add/ Update"}]]

       ;; Close forms and wrappers and body
       ]]]]))
