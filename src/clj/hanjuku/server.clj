(ns hanjuku.server
  (:use compojure.core
        markdown.core
        monger.query
        [clj-time.core :exclude [extend]]
        [clj-time.format]        
        [clojure.tools.logging :only [info debug warn error]]
        ;; for view
        [hiccup.core :only [html]]
        [hiccup.page :only [html5 include-css include-js]])
  (:refer-clojure :exclude [sort find])
  (:require [org.httpkit.server :as server]
            [clojure.string :as str]
            [clojure.tools.nrepl.server :as nrepl]
            [cemerick.shoreleave.rpc :refer (defremote) :as rpc]
            [ring.middleware.basic-authentication :as auth]
            [ring.middleware.reload :as reload]
            [ring.middleware.flash :as flash]
            [ring.middleware.session :as session]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [monger.core :as mg]
            [monger.collection :as mc]
            [hanjuku.admin :as admin]))

;; state
(defonce prod? (atom (System/getenv "LEIN_NO_DEV")))

;; mongodb
(defn connect-mongo! []
  (let [uri (or (System/getenv "MONGOHQ_URL") "mongodb://localhost:27017/hjk")]
  (info "Connecting to mongo via" uri)
  (mg/connect-via-uri! uri)))

;; templates

(defn convert-friendly-date [date]
  (let [friendly-date-formatter (formatter "MMM dd, yyyy")
        simple-date-formatter (formatters :date)]
    (unparse friendly-date-formatter (parse simple-date-formatter date))))

(defn layout [& {:keys [content]}]
  (html
    [:head
     [:title "Hanjuku"]
     (include-css "//netdna.bootstrapcdn.com/twitter-bootstrap/2.2.1/css/bootstrap-combined.min.css"
                  "/css/styles.css")]
    [:body
     [:header
      [:div.container
       [:a {:href "/"}
        [:h1 "Hanjuku"]]
       [:p.description "ridiculously minimal blog engine"]]]
     [:div.container {:id "content"}
      content]
     (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js")
     (include-js "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/js/bootstrap.min.js")
     (include-js "/js/cljs.js")
     ]))

(defn list-posts [page-number]
  (with-collection "blogpost"
  (find {:status "published"})
  (fields [:title :slug :date :body])
  (sort (array-map :date -1))
  (limit 10)))

(defn index []
  (layout
    :content
      (for [{:keys [title slug date body]} (list-posts 1)]
        [:div.post
         [:h2 [:a {:href (str "/" slug)} title]]
         [:p.date (convert-friendly-date date)]
         [:div.body (md-to-html-string body)]
         [:hr]])))

(defn single-post [slug]
  (let [{:keys [title body]} (mc/find-one-as-map "blogpost" {:slug slug})]
  (layout
    :content
    [:div.post.single
     [:h2 title]
     [:div.body (md-to-html-string body)]])))

; routes

(defn authenticated? [name pass]
  (and (= name (or (System/getenv "ADMIN_USER") "foo"))
       (= pass (or (System/getenv "ADMIN_PW") "bar"))))

(defroutes app-routes
  (GET "/" [] (index))
  (context "/admins" []
         (auth/wrap-basic-authentication
           (routes
             (GET "/" request (admin/index request))
             (GET "/new-post" [] (admin/new-post))
             (GET "/edit-post/:slug" [slug] (admin/edit-post slug))
             (POST "/update-post" {params :params} (admin/update-post params))
             (POST "/create-post" {params :params} (admin/create-post params))
             (POST "/delete-post" {params :params} (admin/delete-post params)))
           authenticated?))
  (GET "/:slug" [slug] (single-post slug))  
  (route/resources "/")
  (route/not-found "Not Found"))

(def all-routes (-> app-routes flash/wrap-flash session/wrap-session rpc/wrap-rpc))

(def app
  (if @prod?
    (handler/site all-routes)
    (reload/wrap-reload (handler/site all-routes))))

;; init
(defn start-nrepl-server [port]
  (info "Starting nrepl server on port" port)
  (defonce server (nrepl/start-server :port port)))

(defn start-app [port]
  (info "Starting server on port" port)
  (server/run-server app {:port port :join? false}))

(defn -main [& args]
  (connect-mongo!)
  (when-not @prod? (start-nrepl-server 7888))
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (start-app port)))
