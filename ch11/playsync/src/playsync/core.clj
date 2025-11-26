(ns playsync.core
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]))

;; Create a process that simply prints the message that it receives

;; > ...You use the `chan` function to create a *channel* named
;; > `echo-chan`. Channel communicate *messages*. You can *put* a
;; > messages on a channel and *take* messages off a channel.
;; > Processes *wait* for the completion of a put and take - these
;; > are messages that processes respond to. You can think of processes
;; > as having two rules:
;; >
;; > 1. When trying to put a message on a channel or take a message
;; >    off of it, wait and do nothing until the put or take succeeds
;; > 2. Whe the put or take succeeds, continue executing.

(def echo-chan (chan))

;; > On the next line, you used `go` to create a new process.
;; > Everything with the `go` expression - called a *go block* --
;; > runs concurrently on a separate thread. Go blocks run your
;; > processes on a thread pool that contains a number of threads
;; > equal to two plus the number of cores on your machine, which
;; > means that your program doesn't have to create a new thread
;; > for each process.

(go
  ;; Everything within the `go` expression is a *go block*.
  ;; The `<!` is a take operation that **does not** cause the thread
  ;; to block waiting for a value (but the thread **is suspended**
  ;; while waiting).
  ;;
  ;; > In the expression, `(<! echo-chan)`, `<!` is the *take*
  ;; > function. It listens to the channel you give it as an argument,
  ;; > and the process it belongs to waits until another process puts
  ;; > a message on the channel. When `<!` [receives] a value, the
  ;; > value is returned  and the `println` expression is executed.
  (println (<! echo-chan)))

;; Put an item on the channel waiting if the channel is full.
;;
;; > The expression, `(>!! echo-chan "ketchup")`, *puts* the string
;; > "ketchup" on `echo-chan` and returns `true`. When you put a
;; > message on a channel, the process blocks until another process
;; > takes the message. In this case, the REPL process didn't have
;; > to wait at all, because there was already a process listening to
;; > the channel, waiting to take something off it.
(>!! echo-chan "ketchup")

;; > However, if you do the folloing, your REPL will block indefinitely.
;; >
;; > `(>!! (chan) "mustard")`
;; >
;; > You've created a new channel and put something on it, but there's
;; > no process listening to that channel. Processes don't just wait
;; > to receive message; they also wait for messages they put on a
;; > channel to be taken.

;; Buffering


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
