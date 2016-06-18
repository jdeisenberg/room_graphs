(ns room_graphs.core
    (:require-macros [hiccups.core :as hiccups :refer [html]])
    (:require [room_graphs.utils :as utils]
              [room_graphs.roster-data :as data]
              [goog.dom :as dom]
              [goog.dom.forms :as forms]
              [goog.events :as events]
              [hiccups.runtime :as hiccupsrt]
              [clojure.browser.repl :as repl]))

(defonce conn (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(defn day-times
  "Expand days and start/end times into a set of
  vectors; e.g. (day-times #{} [1,3] [200 400]) ->
  #{[1 200 400][3 200 400]}"
  [times days start-time end-time]
  (into times (for [d days] (vector d start-time end-time))))

(defn process-section
  "Function called by reduce; creates a map with the
  room name as key and a set of day-times as value.
  Times are fifteen-minute intervals with 0 being 7 a.m."
  [acc section-item]
  (let [{:keys [days start-time end-time room building]} section-item
        day-list (utils/to-day-list days)
        start-minutes (quot (- (utils/to-minutes start-time) (* 7 60)) 15)
        end-minutes (quot (- (+ 14 (utils/to-minutes end-time)) (* 7 60)) 15)
        acc-times (get acc room)]
    (if acc-times
     (assoc acc room (day-times acc-times day-list start-minutes end-minutes))
     (assoc acc room (day-times #{} day-list start-minutes end-minutes)))))

(defn make-svg
 "Create SVG rectangles for the given set of day-times"
 [timeset]
 (html
   [:svg {:width 380 :height 135}]
   (map (fn [[day start end]])
     (let [x (+ 20 (* start 5))])
     y (* day 15)
     w (* 5 (- end start))
     [:rect {:x x :y y :width w :height 15}])) timeset
     [:use {:xlink:href "#grid"}])

(defn display-sections
  "Create a table with one row for each room. The key for
  section-map is a room name; the value is a day-times set."
  [section-map]
  (let [key-list (sort (keys section-map))]
      (html
          (into [:table]
              (for [k key-list]
                [:tr [:td [:span.room k]]
                 [:td (make-svg (get section-map k))]])))))

(defn display-section-map [section-map]
    (let [section-html (display-sections section-map)]
        (set! (.-innerHTML (.getElementById js/document "roomTable")) section-html)))

(defn update-graph
  "When menu changes, display the room usage
  for the chosen building by creating and displaying a section-map of
  all courses for the chosen building that have a non-null start time"
  [evt]
  (let [building (forms/getValue (dom/getElement "building"))
        building-sections (filter (fn [item] (and (= (:building item) building) (not= "" (:start-time item))))
                           data/section-list)
        section-map (reduce process-section {} building-sections)]
       (if (= building "")
        (dom/setTextContent (dom/getElement "roomTable") "")
        (display-section-map section-map))))

(let [menu (dom/getElement "building")]
  (dom/setProperties menu #js{"value" ""})
  (events/listen (dom/getElement "building") "change" update-graph))
