(ns bidi-test.core
  (:require [reagent.core :as reagent :refer [atom]]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]))

(enable-console-print!)

(def app-state (atom {:text "Hello Chestnut!"}))

(def app-routes [
                 "/" {(bidi/alts "" "index.html") :root
                      (bidi/alts "foo" "fooalt") :foo
                     true :not-found}])

(defmulti dispatch (fn [{:keys [handler] :as match}] handler))

(defmethod dispatch :root [_]
  (print "hi from root")
  (swap! app-state assoc :text "at root"))

(defmethod dispatch :foo [_]
  (print "hi from foo")
  (swap! app-state assoc :text "at foo"))

(defmethod dispatch :not-found [_]
  (print "totally not found")
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

(defn component []
  [:div
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
     [:a {:href "/fooalt"} "foo alt"]]
    [:ul
     [:a {:href "/somewhere else"} "not found"]]]])

(defn app []
  (reagent/render-component [component]
                            (.getElementById js/document "app")))
(app)
