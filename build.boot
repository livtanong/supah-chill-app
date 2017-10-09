(set-env!
 :source-paths #{"src/shared" "src/frontend" "src/backend"}
 :resource-paths #{"resources"}
 :dependencies
 '[
   ;; Clojure stuff
   [org.clojure/clojure           "1.9.0-beta1"]
   [org.clojure/clojurescript     "1.9.946"]
   [org.clojure/core.async        "0.3.443"]
   [org.clojure/test.check        "0.10.0-alpha2" :scope "test"]

   ;; Boot setup
   [adzerk/boot-cljs              "2.1.4"]
   [adzerk/boot-cljs-repl         "0.3.3"]
   [com.cemerick/piggieback       "0.2.1"  :scope "test"]
   [weasel                        "0.7.0"  :scope "test"]
   [org.clojure/tools.nrepl       "0.2.12" :scope "test"]
   [adzerk/boot-reload            "0.5.2"  :scope "test"]
   [pandeiro/boot-http            "0.8.3"  :scope "test"]

   ;; App dependencies
   [bidi                          "2.1.2"]
   [cljsjs/material-components    "0.19.0-0"]
   [cljs-http                     "0.1.43"]
   [com.levitanong/om-style       "0.1.2-SNAPSHOT"  :exclusions [org.omcljs/om]]
   [com.rpl/specter               "1.0.3"]
   [com.cognitect/transit-clj     "0.8.300"]
   [com.cognitect/transit-cljs    "0.8.239"]
   [compassus                     "1.0.0-alpha3"]
   [crisptrutski/boot-cljs-test   "0.3.3" :scope "test"]
   [garden                        "1.3.0"]
   [hiccup                        "1.0.5" :scope "test"]
   [kibu/pushy                    "0.3.7"]
   [org.omcljs/om                 "1.0.0-beta1"
                                  :exclusions [cljsjs/react cljsjs/react-dom]]

   ;; Server dependencies
   [ring/ring-core                "1.6.1"]
   [ring-transit                  "0.1.6"]

   ;; Other dependencies
   [devcards                      "0.2.3"]
   [binaryage/devtools            "0.9.4" :scope "test"]
   ])

(load-data-readers!)

(task-options!
 pom {:project 'supah-chill-app
      :version "0.1.0-SNAPSHOT"})

(require
 '[adzerk.boot-cljs :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload :refer [reload]]
 '[pandeiro.boot-http :refer [serve]]
 '[clojure.java.io :as io]
 '[crisptrutski.boot-cljs-test :refer [test-cljs]]
 '[supah-chill-app.handler])


(deftask add-material
  "Add material design component css and js into the fileset."
  []
  (sift   :add-resource #{"node_modules/material-components-web/dist"}
          :move         {#"^material-components-web.css"     "css/material.css"
                         #"^material-components-web.min.css" "css/material.min.css"
                         #"^material-components-web.js"      "js/material.js"
                         #"^material-components-web.min.js"  "js/material.min.js"}))


(deftask dev
  []
  (comp
   (serve  :reload true
           :httpkit true
           :handler 'supah-chill-app.handler/app
           :port 3002)
   (watch)
   (reload :on-jsload 'supah-chill-app.core/init)
   (cljs-repl)
   (cljs   :ids ["js/main"]
           :source-map true
           :compiler-options {:npm-deps       {:material-components-web "0.19.0"}
                              :install-deps   true
                              :language-in    :es6
                              :parallel-build true
                              :optimizations  :none
                              :preloads       ['devtools.preload]})
   (add-material)
   (notify :title "Supah Chill App Build"
           :visual true
           :audible true)
   (target)))
