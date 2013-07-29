(ns hanjuku.admin
  (:use compojure.core
        [clj-time.core :exclude [extend]]
        [clj-time.format]
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
                  "/css/admin.css")]
    [:body
     [:div.container
      [:header [:a.button.circle {:href "/admin"} "Admin"]]
      content]
     (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js")
     (include-js "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/js/bootstrap.min.js")
     (include-js "/js/cljs.js")
     ]))

(defn index [request]
  (admin-layout
    :content
    [:div.row
     (when-let [flash-message (-> request :flash)]
       [:p flash-message])     
     [:div.span2
      [:a.button {:href "/admin/new-post"} "New Blog Post"]]
     [:div.span7
     [:ul#post-listing
      (for [{:keys [title slug]} (mc/find-maps "blogpost")]
        [:li 
         [:a {:href (str "/admin/edit-post/" slug)} title]])]]]))

(defn generate-slug [title]
  (str/lower-case (str/replace (str/replace title #"\s+" "-") #"[\W+&&[^-]]" "")))

(defn convert-date [date]
  (let [simple-date (formatters :date)]
    (unparse simple-date date)))

(defn new-post []
  (admin-layout
    :content
    [:div#post-form
     (form-to [:post "/admin/create-post"]
      (text-field {:placeholder "Title"} "title")
      [:br]
      (text-area {:placeholder "Start typing here (in markdown)..."} "body")
      [:br]
      (submit-button {:class "button"} "Submit"))]))

(defn create-post [params]
  (mc/insert "blogpost" {:title (params :title)
                         :body (params :body)
                         :slug (generate-slug (params :title))
                         :date (convert-date (now))})
  (assoc (response/redirect "/admin") :flash "Created new blogpost."))

(defn update-post [params]
  (mc/update-by-id "blogpost" (ObjectId. (params :id))
                   {:title (params :title)
                    :body (params :body)
                    :slug (params :slug)
                    :date (params :date)})
  (response/redirect "/admin"))

(defn delete-post [params]
  (mc/remove-by-id "blogpost" (ObjectId. (params :id)))
  (response/redirect "/admin"))

(defn edit-post [slug]
  (let [{:keys [title slug date body _id]} (mc/find-one-as-map "blogpost" {:slug slug})]
  (admin-layout
    :content
    [:div#post-form
      (form-to [:post "/admin/update-post"]
       (hidden-field "id" _id)
       (text-field {:placeholder "Title"} "title" title)
       [:br]
       (text-field {:placeholder "Slug"} "slug" slug)
       [:br]    
       (text-field {:placeholder "Date"} "date" date)
       [:br]            
       (text-area "body" body)
       [:br]
       (submit-button {:class "btn"} "Update"))
      (form-to [:post "/admin/delete-post"]
       (hidden-field "id" _id)
       (submit-button {:class "btn"} "Delete"))])))