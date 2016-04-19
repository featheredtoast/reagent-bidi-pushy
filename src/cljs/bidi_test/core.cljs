(ns bidi-test.core
  (:require [reagent.core :as reagent :refer [atom]]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]))

(enable-console-print!)

(defonce old-history (atom {:history false}))

(when-let [old-history (:history @old-history)]
  (print (str "stopping old history... " old-history))
  (pushy/stop! old-history))

(def app-routes [
                 "/" {(bidi/alts "" "index.html") :root
                      "foo" :foo
                      true :not-found}])

(def app-state (atom {:text "Hello Chestnut!"
                      :root-link (bidi/path-for app-routes :root)
                      :foo-link (bidi/path-for app-routes :foo)}))

(def match-route (partial bidi/match-route app-routes))

(defn component-links [root-link foo-link]
  [:ul {:id "nav" :class "nav nav-pills nav-stacked"}
    [:li
     [:a {:href root-link} "root"]]
    [:li
     [:a {:href "/index.html"} "root alt"]]
    [:li
     [:a {:href foo-link} "foo"]]
   [:li
    [:a {:href "/somewhere else"} "not found"]]
   [:li
    [:a {:href "https://github.com/juxt/bidi"} "Bidi"]]])

(defn component-foo []
  [:div
   [:h1 "foo"]
   [:div (:text @app-state)]])

(defn component-root []
  [:div
   [:h1 "root"]
   [:div (:text @app-state)]
   [:div "oh man



can we have spaces here?? apparently not...



wait yes we can. with white-space pre



this is cool








now to test out all the crazy


crazy crazy 






things"]])

(defn component-main []
  [:div {:class "container-fluid"}
   [:nav {:class "navbar navbar-default navbar-fixed-top"}
    [:h3 {:style {"text-align" "center"}} "Really neat bidi tests!"]]
   [:div {:class "row-fluid"}
    [:div {:class "content"}
     [:div {:class "col-md-2"}
      [component-links (:root-link @app-state) (:foo-link @app-state)]]
     [:div {:class "main col-md-10"}
      [(:view @app-state)]]]]])

(defn render-app []
  (reagent/render-component [component-main]
                            (.getElementById js/document "app")))

(defmulti dispatch (fn [{:keys [handler] :as match}] handler))

(defmethod dispatch :root [_]
  (swap! app-state assoc :text "at root.
This is the root home page. It is also the default landing page
for example when you hit /ffff or something random like that.
The text will be different, but you get the idea. The app states are the same.
Thanks to compojure and bidi's routes /index.html is the catch all bucket!")
  (swap! app-state assoc :view component-root))

(defmethod dispatch :foo [_]
  (swap! app-state assoc :text "at foo. Consider this a second page or something.")
  (swap! app-state assoc :view component-foo))

(defmethod dispatch :not-found [_]
  (swap! app-state assoc :text "not found. Check out this 404 thingy.")
  (swap! app-state assoc :view component-root))

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
(swap! old-history assoc :history history)

(render-app)

(defn on-figwheel-reload []
  (print "reloading!"))
