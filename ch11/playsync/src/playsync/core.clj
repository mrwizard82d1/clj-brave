(ns playsync.core
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go chan buffer
                     close! thread alts! alts!! timeout]]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(println "Starting `playsync.core`")

;; Create a(n unbuffered) channel
(def echo-chan (chan))

;; Print an item **taken** from a channel **waiting** (parking not blocking)
;; until an item is put on that channel.
;;
;; The `go` expression shunts this work to another thread (from a thread pool).
;; Consequently, the REPL thread continues working (allowing one to type more).
(go (println (<! echo-chan)))

;; Put an item onto `echo-chan` blocking until another process takes
;; the item. Because of the waiting nature of the put and take operations,
;; two processes must collaborate in this process; that is, one must
;; create "communicating sequential processes."
(>!! echo-chan "ketchup")

;; Uncomment the following expression and the process executing the
;; expression **will block** until another process "arrives" to take
;; the item off the channel.
;; (>!! echo-chan "mustard")

;; Create a buffered channel (with 2 slots)
(def echo-buffer (chan 2))

;; Put an item on the channel **without** waiting (because the channel
;; is buffered.
(>!! echo-buffer "ketchup")

;; Put another item on the same channel (still no waiting)
(>!! echo-buffer "more ketchup")

;; A third put would **block** (but I don't want to block the REPL thread).
;; (>!! echo-buffer "more ketchup")

;; But a blocking take will remove items
(println (<!! echo-buffer))
(println (<!! echo-buffer))

;; But don't try to take any others, or you will **block**
;; (println (<!! echo-buffer))

(println "Ending `playsync.core`")
