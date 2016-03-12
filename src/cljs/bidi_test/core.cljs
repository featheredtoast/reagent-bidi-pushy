(ns bidi-test.core
  (:require [reagent.core :as reagent :refer [atom]]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]))

(enable-console-print!)

(def app-state (atom {:text "Hello Chestnut!"}))

(def app-routes ["/" {"" :home
                      "foo" :foo
                      "#foo" :foo}
                 true :not-found])

(defmulti dispatch (fn [{:keys [handler] :as match}] handler))

(defmethod dispatch :root [_]
  (print "hi from root"))

(defmethod dispatch :foo [_]
  (print "hi from foo"))

(defmethod dispatch :not-found [_]
  (print "totally not found"))

(defmethod dispatch :default [_] (dispatch {:handler :root}))

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
     [:a {:href "/"} "home"]]
    [:ul
     [:a {:href "/foo"} "foo"]]
    [:ul
     [:a {:href "/#foo"} "#foo"]]]])

(defn app []
  (reagent/render-component [component]
                            (.getElementById js/document "app")))
(app)