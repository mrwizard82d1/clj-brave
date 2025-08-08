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

;; Similarly, a parking take would remove items.
(>!! echo-buffer "ketchup")
(>!! echo-buffer "more ketchup")

(go (println (<! echo-buffer))
    (println (<! echo-buffer)))

;; Each process performing a "parking put" will wait forever...
(def hi-chan (chan))
(doseq [n (range 1000)]
  (go (>! hi-chan (str "Hi " n))))

;; ...until another process takes values.
(drop 995 (repeatedly 1000 (fn [] (<!! hi-chan))))

;; A better option than `go`, if the producer or consumer will take
;; a "long time", is `thread`. (Notice the "blocking take".)
(thread (println (<!! echo-chan)))
(Thread/sleep 100)
(>!! echo-chan "mustard")

;; When `thread`'s process stops, the process's return value is
;; put on the channel returned by `thread`.
;;
;; The reason for using `thread` instead of `go` is to avoid
;; "clogging/hogging" your thread pool threads.
(let [t (thread "chill")]
  (<!! t))

;; A good summary.
;;
;; """`go, thread, chan, <!, <!!, >!`, and `>!!` are the core
;; tools you'll use for creating and communicating with processes.
;; Both put and take will cause a process to wait until its
;; complement is performed on the given channel, `go` allows you
;; to use the parking variants of put and take, which could improve
;; performance. You should use the blocking variants, along with
;; `thread`, if you're performing long-running tasks before the
;; put or take.

;; The Hot Dog Machine Process You've Been Longing For

;; A function that takes "payment" and gives hot dogs.
(defn hot-dog-machine
  []
  (let [in (chan)
        out (chan)]
    ;; Take anything and put out a hot day
    (go (<! in)
        (>! out "hot dog'"))
    [in out]))

;; I'm hungry. Time to get a hot dog.
;;
;; Money for nothin' /
;; And my hot dogs "for free"
(let [[in out] (hot-dog-machine)]
  (>!! in "pocket lint")
  (<!! out))

;; Let's try to do better with this version.
(defn hot-dog-machine-v2
  [hot-dog-count]
  (let [in (chan)
        out (chan)]
    (go (loop [hc hot-dog-count]
          (if (> hc 0)
            (let [input (<! in)]
              (if (= 3 input)
                (do (>! out "hot dog")                      ;; dispense a hot dog
                    (recur (dec hc)))                       ;; and recur
                (do (>! out "wilted lettuce")               ;; dispense wilted lettuce
                    (recur hc))))                           ;; and also recur
            (do (close! in)
                (close! out)))))
    [in out]))

;; Let's test our new-fangled hot dog machine
(let [[in out] (hot-dog-machine-v2 2)]
  (>!! in "pocket lint")                                    ;; Try some more pocket line
  (println (<!! out))                                       ;; Drat!

  (>!! in 3)                                                ;; Grumble, grumble grumble
  (println (<!! out))

  (>!! in 3)                                                ;; Umm. tasty!
  (println (<!! out))

  (>!! in 3)                                                ;; Must have more cookies - er - hot dogs
  (<!! out))                                                ;; Hey! What about my money!


(println "Ending `playsync.core`")
