(ns supah-chill-app.handler
  (:require
   [bidi.bidi :as bidi]
   [bidi.ring :refer [make-handler resources-maybe]]
   [hiccup.page :as hiccup]
   [ring.util.response :as response]
   [ring.middleware.transit :refer [wrap-transit-response
                                    wrap-transit-body
                                    wrap-transit-params]]
   [supah-chill-app.server-parser :as server-parser]))

(defn template [html-string]
  (hiccup/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible"
            :content "IE=edge"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    [:title "Supah Chill App, Dude"]
    (hiccup/include-css "/css/main.css")
    (hiccup/include-css "/css/material.css")
    (hiccup/include-css "https://fonts.googleapis.com/css?family=Roboto:300,400,500")
    (hiccup/include-css "https://fonts.googleapis.com/icon?family=Material+Icons")]
   [:body
    [:div#app.u-fullscreen--absolute.mdc-typography {} html-string]
    (hiccup/include-js "/js/main.js")]))

(defn om-handler
  [request]
  (let [{om-query :transit-params} request]
    {:status 200
     :body   (server-parser/parser {:state {}} om-query)}))

(defn page-handler
  [request]
  (-> (template "")
      (response/response)
      (response/content-type "text/html")))

(def api-routes
  ["/" {""   page-handler
        "om" om-handler
        "js" (resources-maybe {:prefix "js/"})}])

(def app
  (-> (make-handler api-routes)
      (wrap-transit-response)
      (wrap-transit-params)))
