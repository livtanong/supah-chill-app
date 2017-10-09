(ns supah-chill-app.core
  (:require
   [om.dom :as dom]
   [om.next :as om :refer [defui]]
   [supah-chill-app.services :as services]
   #?@(:cljs [[cljs-http.client :as http]
              [goog.dom :as gdom]
              [cljs.core.async :refer [timeout <!]]]))
  #?(:cljs
     (:require-macros
      [cljs.core.async.macros :refer [go]])))

(defui Item
  static om/Ident
  (ident [_ {:keys [item/id]}]
    [:item/by-id id])
  static om/IQuery
  (query [_]
    [:item/id
     :item/name
     {:item/subitems [:subitem/id]}]))

(defui Supah
  static om/IQuery
  (query [_]
    [:app/current-user
     {:app/items (om/get-query Item)}])
  Object
  (componentWillMount [this]
    (let [{:keys [app/current-user]} (om/props this)]
      #?(:cljs (js/console.log current-user))))
  (render [this]
    (let [{:keys [app/current-user app/items]} (om/props this)]
      (dom/div
       nil
       "testing"
       current-user
       (case items
         :not-loaded "Not Loaded"
         :loading "Loading"
         (str items))))))

(defmulti read om/dispatch)

(defmethod read :app/items
  [{:keys [state query ast]} k _]
  (let [st                        @state
        {access_token :app/access_token
         item-idents  :app/items} st]
    (if (some? item-idents)
      {:value (om/db->tree query item-idents st)}
      {:value             :not-loaded
       :remote            (update ast :params assoc
                                  :app/access_token access_token)
       :om-remote         true
       :foo               false
       :that-other-server ast})
    ))

(defmethod read :default
  [{:keys [state]} k _]
  (let [st @state]
    {:value (if-let [d (get st k)]
              d
              :not-found)}))

(def parser (om/parser {:read read}))

(def init-state
  {:app/items        nil
   :app/access_token "asdf"
   :app/current-user "AJ"})

(defn mock-handler
  ([url]
   (mock-handler url nil))
  ([url params]
   (case url
     "derp" (with-meta
              {:status 200
               :body   [{:item/id       0
                         :item/name     "Sausage"
                         :item/subitems [{:subitem/id 2}]}
                        {:item/id   1
                         :item/name "Ham"}]}
              {:params params}))))

(defmulti remote-handler om/dispatch)

(defmethod remote-handler :app/items
  [{:keys [cb http]} k {:as params :keys [app/access_token]}]
  (go
    (let [{:as   res
           :keys [status
                  body]} (<! (services/get
                              http
                              "om"
                              {:header {"Authorization" (str "Token " access_token)}}))]
      (case status
        200 (cb {:app/items body})
        #?(:cljs (js/console.log "unexpected status" res))))))

(defn make-send
  [services]
  (fn [{:as   remotes
        :keys [remote om-remote]} cb]
    (when remote
      (doseq [sub-ast (:children (om/query->ast remote))]
        (let [{:keys [dispatch-key
                      params]} sub-ast
              env              (merge services {:ast          sub-ast
                                                :cb           cb
                                                :remote-query remote})]
          (remote-handler env dispatch-key params))))
    (when om-remote
      (go
        (let [{:as   res
               :keys [status
                      body]} (<! (http/post
                                  "om"
                                  {:transit-params om-remote}))]
          (case status
            200 (cb body)
            #?(:cljs (js/console.error res)))))
      #?(:cljs (js/console.log "om-remote" om-remote)))))

(def mock-http
  (reify
    services/IHTTP
    (get [this url]
      (go (<! (timeout 1000))
          (mock-handler url)))
    (get [this url params]
      (go (<! (timeout 1000))
          (mock-handler url params)))
    (post [this url]
      (go (<! (timeout 1000))
          (mock-handler url)))
    (post [this url params]
      (go (<! (timeout 1000))
          (mock-handler url params)))))

(def reconciler
  (om/reconciler
   {:state   init-state
    :parser  parser
    :remotes [:remote :om-remote]
    :send    (make-send {:http (services/HTTPService.)})}))

#?(:cljs
   (defn init []
     (om/add-root! reconciler Supah (gdom/getElement "app"))))
