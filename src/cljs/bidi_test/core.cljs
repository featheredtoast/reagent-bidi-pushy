(ns bidi-test.core
  (:require [reagent.core :as reagent :refer [atom]]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]))

(enable-console-print!)

(def app-state (atom {:text "Hello Chestnut!"}))

(def app-routes [
                 "/" {(bidi/alts "" "index.html") :root
                      "foo" :foo
                      true :not-found}])

(defn component-foo []
  [:div {:style {:color "green"}}
   [:h1 "foo"]
   [:div (:text @app-state)]
   [:li
    [:ul
     [:a {:href (bidi/path-for app-routes :root)} "root"]]
    [:ul
     [:a {:href "/index.html"} "root alt"]]
    [:ul
     [:a {:href (bidi/path-for app-routes :foo)} "foo"]]
    [:ul
     [:a {:href "/foo"} "foo alt"]]
    [:ul
     [:a {:href "/somewhere else"} "not found"]]]])

(defn component-root []
  [:div {:style {:color "blue"}}
   [:h1 "hi"]
   [:div (:text @app-state)]
   [:li
    [:ul
     [:a {:href (bidi/path-for app-routes :root)} "root"]]
    [:ul
     [:a {:href "/index.html"} "root alt"]]
    [:ul
     [:a {:href (bidi/path-for app-routes :foo)} "foo"]]
    [:ul
     [:a {:href "/foo"} "foo alt"]]
    [:ul
     [:a {:href "/somewhere else"} "not found"]]]])

(defn foo-panel []
  (reagent/render-component [component-foo]
                            (.getElementById js/document "app")))

(defn root-panel []
  (reagent/render-component [component-root]
                            (.getElementById js/document "app")))

(defmulti dispatch (fn [{:keys [handler] :as match}] handler))

(defmethod dispatch :root [_]
  (swap! app-state assoc :text "at root")
  (root-panel))

(defmethod dispatch :foo [_]
  (swap! app-state assoc :text "at foo")
  (foo-panel))

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
  (pushy/pushy set-page! (partial bidi/match-route app-routes)))

(pushy/start! history)

(defn on-figwheel-reload []
  (print "reloading!"))
