(ns hanjuku.admin
  (:use compojure.core
        [clojure.tools.logging :only [info debug warn error]]
        ;; for view
        [hiccup.core :only [html]]
        [hiccup.form :only (form-to label text-field text-area hidden-field drop-down submit-button)]        
        [hiccup.page :only [html5 include-css include-js]])
  (:import org.bson.types.ObjectId)
  (:require 
            [clojure.string :as str]
            [monger.collection :as mc]
            [ring.util.response :as response]))

(defn admin-layout [& {:keys [content]}]
  (html
    [:head
     [:title "Blog Admin"]
     (include-css 
                  "//netdna.bootstrapcdn.com/twitter-bootstrap/2.2.1/css/bootstrap-combined.min.css"
                  "/css/styles.css")]
    [:body
     [:div.navbar.navbar-inverse.navbar-fixed-top
      [:div.navbar-inner
       [:div.container
        [:a.brand {:href "/admin"} "Blog Admin"]]]]
      content
     (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js")
     (include-js "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/js/bootstrap.min.js")
     (include-js "/js/cljs.js")
     ]))

(defn index [request]
  (admin-layout
    :content
    [:div.container 
     [:h2 "New Post"]
     (when-let [flash-message (-> request :flash)]
       [:p flash-message])
     (form-to [:post "/admin/create-post"]
      (text-field {:placeholder "Title"} "title")
      [:br]
      (text-field {:placeholder "Slug"} "slug")
      [:br]         
      (text-area "body")
      [:br]
      (submit-button {:class "btn"} "Submit"))
     [:h2 "List Posts"]
     [:ul
      (for [{:keys [title slug]} (mc/find-maps "blogpost")]
        [:li 
         [:a {:href (str "/admin/edit-post/" slug)} title]])]]))

(defn create-post [params]
  (mc/insert "blogpost" {:title (params :title)
                         :body (params :body)
                         :slug (params :slug)})
  (assoc (response/redirect "/admin") :flash "Created new blogpost."))

(defn update-post [params]
  (mc/update-by-id "blogpost" (ObjectId. (params :id))
                   {:title (params :title)
                    :body (params :body)
                    :slug (params :slug)})
  (response/redirect "/admin"))

(defn delete-post [params]
  (mc/remove-by-id "blogpost" (ObjectId. (params :id)))
  (response/redirect "/admin"))

(defn edit-post [slug]
  (let [{:keys [title slug body _id]} (mc/find-one-as-map "blogpost" {:slug slug})]
  (admin-layout
    :content
    [:div.container
      (form-to [:post "/admin/update-post"]
       (hidden-field "id" _id)
       (text-field {:placeholder "Title"} "title" title)
       [:br]
       (text-field {:placeholder "Slug"} "slug" slug)
       [:br]         
       (text-area "body" body)
       [:br]
       (submit-button {:class "btn"} "Update"))
      (form-to [:post "/admin/delete-post"]
       (hidden-field "id" _id)
       (submit-button {:class "btn"} "Delete"))])))