(ns supah-chill-app.server-parser
  (:require
   [om.next :as om]))

(defmulti read om/dispatch)

(defmethod read :app/items
  [{:keys [conn query]} _ _]
  {:value [{:item/id       0
            :item/name     "Sausage"
            :item/subitems [{:subitem/id 2}]}
           {:item/id   1
            :item/name "Ham"}]})

(def parser (om/parser {:read read}))
