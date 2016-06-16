(ns room_graphs.utils
  (:require [clojure.string]))

(defn to-minutes
  "Convert a string in form hh:mm [AP]M to number of
  minutes past midnight"
  [time-string]
  (let [[_ hr minute am-pm] (re-matches #"(?i)(\d\d?):(\d\d)\s*([AP])\.?M\.?" time-string)
        hour (+ (mod (js/parseInt hr) 12) (if (= (.toUpperCase am-pm) "A") 0 12))]
    (+ (* hour 60) (js/parseInt minute))))

(defn pad
  "Zero-pad a number of hours or minutes to two digits"
   [n] (if (< n 10) (str "0" n) (.toString n)))

(defn to-am-pm
  "Convert minutes past midnight to hh:mm [AP]M format"
  [total-minutes]
  (let [h (quot total-minutes 60)
        m (mod total-minutes 60)
        hour (if (= (mod h 12) 0) 12 (mod h 12))
        suffix (if (< h 12) "AM" "PM")]
    (str hour ":" (pad m) " " suffix)))

(defn to-24-hr
  "Convert minutes past midnight to four-digit
  24-hour time"
  [total-minutes]
  (str (pad (quot total-minutes 60)) (pad (mod total-minutes 60))))

(defn to-day-list
  "Convert string like MTTH to a sequence of
  day numbers; example would be (1 2 4)"
  [day-str]
  (let [daymap {"M" 1 "T" 2 "W" 3 "R" 4 "F" 5 "S" 6 "U" 7}
        day-single (clojure.string/replace
                    (clojure.string/replace day-str #"SU" "U") #"TH" "R")]
       (map (fn [item] (get daymap item)) (clojure.string/split day-single ""))))

(defn pairs
  "Helper function for get-query-string"
  [acc item]
  (conj acc (into [] (rest item))))

(defn get-query-string
  "Convert URL query string into a key/value map"
  []
  (let [qstr (.substr (.-search (.-location js/window)) 1)
        matches (re-seq #"([^&=]+)=([^&]*)" qstr)]
       (reduce pairs {} matches)))
