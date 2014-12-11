(ns hn-follow.core.views
  (:require [hiccup.core :refer :all]))

;;
;; Site Template
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn layout [& content]
  (html
   [:head
    [:meta {:http-equiv= "Content-Type" :content= "text/html; charset=UTF-8"}]
    [:meta {:name "referrer" :content "origin"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:link {:rel "stylesheet" :type "text/css" :href "/news.css"}]

    [:script {:type "text/javascript" :src "/jquery-1.11.1.min.js"}]
    [:script {:type "text/javascript" :src "/moment.min.js"}]
    [:script {:type "text/javascript" :src "/follow.js"}]
    [:script {:type "text/javascript" :src "/ga.js"}]

    [:title "HN Follow"]]
   
   [:body
    [:div.page
     [:div.center
      content]]]))

(defn go-to-user-form []
  (html
   [:form.goto
    [:p
     "Go to your follower list. Or check out the "
     [:a {:href "/?user=hn-top-10"} "HN Top 10"]
     " list"]
    
    [:p "Username: " [:input {:type "text" :name "user"}]]
    [:input {:type "submit" :value "Go"}]]))

(defn register-form []
  (html
   [:p.info "Create a username and select the people you'd like to follow."]

   ;; Fields Section
   [:form.register
    [:p.user-section
     "Username: "
     [:input {:type "text" :name "user"}]]
    [:p.password-section
     "Password:&nbsp; "
     [:input {:placeholder "Optional" :type "password" :name "password"}]]
    [:p.new-password-section
     "New Password: "
     [:input {:type "password" :name "new_password"}]]
    [:p.new-password-section
     "Change Password "
     [:input {:type "checkbox" :name "new_password_check_box"}]]

    ;; User List Section
    [:p "Folllow up to 10 HN users:"]
    [:ol
     (for [i (range 1 11)]
       [:li
        [:input {:type "text" :name (str "follow" i)}]
        [:br]])]
    [:input {:type "submit" :value "Add/ Update"}]]))

(defn home-page []
  (layout
  
   ;; Headings 
   [:div.heading]

   ;; Comments
   [:div.comments
    [:ol.comment-list "Loading..."]]

   ;; Forms
   [:div.form
    ;; Go to user form
    (go-to-user-form)

    ;; Registration or edit form
    (register-form)

    ;; Close forms and wrappers and body
    ]))

;;
;; Email Template
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn email-template [user interactions]
  (html
   "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
   [:html
    [:body
     [:div {:style "font-family: Verdana; font-size: 10pt; color: #000000;"}
      [:center
       [:table {:border 0 :cellpadding 0 :cellspacing 0 :width "100%" :style "background-color: #F6F6EF;"}
        ;; Header
        [:thead {:style "background-color: #FF6600;"}
         [:tr 
          [:td {:style "padding: 0.5em;"} "HN Follow - Weekly Updates for " (user "id")]]]

        ;; Content
        [:tbody
         [:tr
          [:td {:style "padding: 0.5em;"}
           [:ol 
            (for [item interactions]
              [:li
               [:p {:style "font-size: 9pt;"}
                [:font {:color "#000000"}
                 ;; Heading
                 [:p {:style "color: #828282; font-size: 8pt;"}
                  [:a {:style "color: #828282; font-size: 8pt;"
                       :href (str "https://news.ycombinator.com/user?id=" (user "id"))} (user "id")]
                  " | "
                  [:a {:style "color: #828282; font-size: 8pt;"
                       :href (str "https://news.ycombinator.com/item?id=" (:id item))} "Link"]
                  " | "
                  [:a {:style "color: #828282; font-size: 8pt;"
                       :href (str "https://news.ycombinator.com/item?id=" (:parent item))} "Root"]
                  [:u
                   [:a {:style "color: #828282; font-size: 8pt;"
                        :href (:url item)} (:title item)]]]

                 ;; Comment
                 (:text item)]]])]]]]]

       ;; Footer
       [:div {:style "color: #888888; font-size: 8pt; margin-top: 1em; margin-left: auto; margin-right: auto;"}
        [:span
         [:a {:href (str "https://hn-follow.desh.es?user=" (user "id") "&edit=true")
              :style "#000000"} "Unsubscribe"]]]]]]]))
