(ns room_graphs.core
    (:require-macros [hiccups.core :as hiccups :refer [html]])
    (:require [room_graphs.utils :as utils]
              [room_graphs.roster-data :as data]
              [goog.dom :as dom]
              [goog.dom.forms :as forms]
              [goog.events :as events]
              [hiccups.runtime :as hiccupsrt]
              [clojure.browser.repl :as repl]))

;; (defonce conn (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(defn day-times
  "Expand days and start/end times into a set of
  vectors; e.g. (day-times #{} [1,3] [200 400]) ->
  #{[1 200 400][3 200 400]}"
  [times days start-time end-time]
  (apply conj times (for [d days] (vector d start-time end-time))))

(defn process-section
  "Function called by reduce; creates a map with the
  room name as key and a set of day-times as value.
  Times are fifteen-minute intervals with 0 being 7 a.m."
  [acc item]
  (let [[_ _ _ _ _ day-string start end room _ _ building] item
        days (utils/to-day-list day-string)
        start-time (quot (- (utils/to-minutes start) (* 7 60)) 15)
        end-time (quot (- (+ 14 (utils/to-minutes end)) (* 7 60)) 15)
        info (get acc room)]
    (if info
     (assoc acc room (day-times info days start-time end-time))
     (assoc acc room (day-times #{} days start-time end-time)))))

(defn make-svg
  "Create SVG rectangles for the given set of day-times"
  [timeset]
 (str "<svg width='380' height='135'>"
  (loop [t timeset
         result ""]
    (if (empty? t) result
     (let [[day start end] (first t)
           x (+ 20 (* start 5))
           y (* day 15)
           w (* 5 (- end start))]
      (recur (rest t) (str result "\n<rect x='" x "' y='" y "'
                        width='" w "' height='15'/>")))))
  "<use xlink:href='#grid'></svg>"))


(defn display-row
   "Create a table row with the room
   name in the left cell and the grid of
   times and dates in the right cell"
   [item info]
   (html
    [:tr [:td [:span.room item]]
     [:td (make-svg info)]]))

(defn display-sections
  "Create a table with one row for each room. The key for
  section-map is a room name; the value is a day-times set."
  [section-map]
  (let [key-list (sort (keys section-map))]
      (html
          [:table
              (loop [k key-list ;; loop through room names
                     result ""] ;; accumulating an HTML/SVG string
                (if (empty? k)
                 result
                 (let [key1 (first k)]
                  (recur (rest k) (str result (display-row key1
                                               (get section-map key1)))))))])))

(defn display-section-map [section-map]
    (let [section-html (display-sections section-map)]
        (set! (.-innerHTML (.getElementById js/document "roomTable")) section-html)))

(defn update-graph
  "When menu changes, display the room usage
  for the chosen building by creating and displaying a section-map of
  all courses for the chosen building that have a non-null start time"
  [evt]
  (let [building (forms/getValue (dom/getElement "building"))
        building-sections (filter (fn [item] (and (= (get item 11) building) (not= "" (get item 6))))
                           data/section-list)
        section-map (reduce process-section {} building-sections)]
       (if (= building "")
        (dom/setTextContent (dom/getElement "roomTable") "")
        (display-section-map section-map))))

(let [menu (dom/getElement "building")]
  (dom/setProperties menu #js{"value" ""})
  (events/listen (dom/getElement "building") "change" update-graph))
