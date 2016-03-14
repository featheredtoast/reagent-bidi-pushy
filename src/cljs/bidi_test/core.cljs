(ns bidi-test.core
  (:require [reagent.core :as reagent :refer [atom]]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]))

(enable-console-print!)

(def app-routes [
                 "/" {(bidi/alts "" "index.html") :root
                      "foo" :foo
                      true :not-found}])

(def app-state (atom {:text "Hello Chestnut!"
                      :root-link (bidi/path-for app-routes :root)
                      :foo-link (bidi/path-for app-routes :foo)}))

(def match-route (partial bidi/match-route app-routes))

(defn component-links [root-link foo-link]
  [:li
    [:ul
     [:a {:href root-link} "root"]]
    [:ul
     [:a {:href "/index.html"} "root alt"]]
    [:ul
     [:a {:href foo-link} "foo"]]
   [:ul
     [:a {:href "/somewhere else"} "not found"]]])

(defn component-foo []
  [:div {:style {:color "green"}}
   [:h1 "foo"]
   [:div (:text @app-state)]])

(defn component-root []
  [:div {:style {:color "blue"}}
   [:h1 "root"]
   [:div (:text @app-state)]])

(defn component-main []
  [:div
   [(:view @app-state)]
   [component-links (:root-link @app-state) (:foo-link @app-state)]])

(defn render-app []
  (reagent/render-component [component-main]
                            (.getElementById js/document "app")))

(defmulti dispatch (fn [{:keys [handler] :as match}] handler))

(defmethod dispatch :root [_]
  (swap! app-state assoc :text "at root")
  (swap! app-state assoc :view component-root))

(defmethod dispatch :foo [_]
  (swap! app-state assoc :text "at foo")
  (swap! app-state assoc :view component-foo))

(defmethod dispatch :not-found [_]
  (swap! app-state assoc :text "not found"))

(defmethod dispatch :default [_]
  (print "default handler, submit to not found")
  (dispatch {:handler :not-found}))

(defn set-page! [match]
  (swap! app-state assoc :page match)
  (println match)
  (dispatch match))

(def history
  (pushy/pushy set-page! match-route))

(pushy/start! history)

(print (str "checking match-route: " (match-route (bidi/path-for app-routes :foo))))

(render-app)

(defn on-figwheel-reload []
  (print "reloading!")
  (pushy/stop! history)
  (pushy/start! history))
