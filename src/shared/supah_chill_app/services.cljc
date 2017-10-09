(ns supah-chill-app.services
  (:refer-clojure :exclude [get #?(:clj read)])
  (:require
   #?@(:clj [[clojure.core.async :refer [go <! timeout]]]
       :cljs [[cljs-http.client :as http]])))

(defprotocol IHTTP
  (get [this url] [this url params])
  (post [this url] [this url params]))

(defrecord HTTPService []
  IHTTP
  (get [this url]
    #?(:cljs (http/get url)
       :clj (go (<! (timeout 1000))
                {:foo "1"})))
  (get [this url params]
    #?(:cljs (http/get url params)
       :clj (go (<! (timeout 1000))
                {:foo "1"})))
  (post [this url]
    #?(:cljs (http/post url)
       :clj (go (<! (timeout 1000))
                {:foo "1"})))
  (post [this url params]
    #?(:cljs (http/post url params)
       :clj (go (<! (timeout 1000))
                {:foo "1"}))))
