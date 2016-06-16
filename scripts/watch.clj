(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'room_graphs.core
   :output-to "out/room_graphs.js"
   :output-dir "out"})
