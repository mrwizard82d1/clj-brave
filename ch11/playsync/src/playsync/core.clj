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

;; ## Buffering

;; Create buffered channels

(def echo-buffer (chan 2))

;; Since we've created a channel with 2 "slots", we can `put` two
;; items into the channel **without** blocking.
(>!! echo-buffer "ketchup")
(>!! echo-buffer "ketchup")

;; If we attempt to put more "ketchup" on the channel, we will
;; **block** until another process takes at least one item from
;; the channel.
;;
;; `(>!! echo-buffer "ketchup")`
;;
;; This expression would block (the REPL) until some other process
;; takes an item from `echo-buffer` freeing a "slot" for another item.

;; In addition to the "blocking" buffer we just created, `core.async`
;; supports creating:
;;
;; - *sliding* buffers using `sliding-buffer`
;; - *dropping* buffers using `dropping-buffer`
;;
;; A ``sliding-buffer`` drops values in a first-in, first-out fashion. A
;; `dropping-buffer` discards values in a last-in, first-out fashion.
;; Neither a `sliding-buffer` nor a `dropping-buffer` will ever cause
;; `<!!` to block.

;; ## Blocking and parking

;; Both put and take have "one-exclamation" and "two-exclamation"
;; versions. But how does one decide which is appropriate?
;;
;; The simple answer:
;;
;; Operation      Inside go block     Outside go block
;; =========      ===============     ================
;; put            `>!` or `>!!`       `>!!`
;; take           `<!` or `<!!`       `<!!``

;; Because go blocks use a fixed size thread pool, you can create
;; 1000 go processes but only use a handful of threads. The following
;; code creates 1000 go processes that are waiting for
;; **some other process** to consume these values.
(def hi-chan (chan))
(doseq [n (range 1000)]
  (go (>! hi-chan (str "hi " n))))

;; However, notice that the values taken from the channel are printed
;; in a **arbitrary** order.
(do
 (doseq [n (range 1000)]
   (print (str " " (<!! hi-chan) "!")))
 (println))

;; Two types of waiting occur in a Clojure program: *parking* and
;; *blocking*. Blocking occurs when a thread stops execution until
;; the task is **complete**. When blocked, a thread from the thread
;; pool is **consumed** and not available for any other work.
;;
;; Parking releases the thread so it can do other work. For example,
;; imagine you have one thread but two processes, Process A and
;; Process B. Process A runs on the thread and then waits for a
;; put or take to complete. Clojure then moves Process A off the
;; thread and moves Process B onto the thread. If Process B waits
;; **and** Process A's put or take is finished, Clojure will then
;; move Process B **off the thread** and put Process A back on it.
;;
;; > Parking allows the instructions from multiple processes to
;; > interleave on a single thread, similar to the way that using
;; > multiple threads allows interleaving on a **single core**
;; > (Emphasis added.)
;;
;; Remember that parking is only possible **within go blocks** and
;; it's only possible when you use `>!` and `<!` (*parking put* and
;; *parking take*, respectively).
;;
;; Furthermore, `>!!` and `<!!` are **blocking** put and take,
;; respectively.

;; ## Thread

;; > There are definitely times when you'll want to use blocking
;; > instead of parking, like when your process will take a long
;; > time before putting or taking, and for those occassions you
;; > should use `thread`.
(thread (println (<!! echo-chan)))
(>!! echo-chan "mustard")

;; Whereas `future` provides similar functionality (creating a thread
;; on which work is performed), `future` returns an object that you
;; can dereference at some future time, `thread` returns a **channel**.
;; When the process associated with `thread` stops, the return value
;; of the process is put on the channel returned by `thread`.

(let [t (thread "chili")]
  (<!! t))

;; In this example, the process stops immediately. It's return value
;; is "chili" which gets put on the channel bound to `t`. We take the
;; value from `t` returning the string, "chili".

;; > The reason you should use `thread` instead of a go block when
;; > you're performing a long-running task is so you don't clog your
;; > thread pool. Imagine you're running four processes that download
;; > humongous files, save them, and then put the file paths on a
;; > channel. While the processes are downloading the files and saving
;; > these files, Clojure can't park their threads. It can park the
;; > thread only at the last step, when the process puts the files'
;; > paths on a channel. Therefore, if you thread pool has only four
;; > threads, all four threads will be used for downloading, and no
;; > other process will be allowed to run until one of the downloads
;; > finishes.

;; The function, `go`, `thread`, `chan`, `<!`, `<!!`, `>!`, and `>!!`
;; are the core tools you'll use for creating and communicating with
;; processes. Both put and take cause a process to wait until its
;; complement is performed on a given channel (a rendezvous).
;;
;; The function, `go`, allows one to use the parking variants of put
;; and take. Using the parking variants **may** improve performance.
;; However, if you're performing "long running" tasks, use the
;; blocking variants along with `thread`.

;; ## The hot dog machine process you've been longing for

;; An incorrect iteration of our hot dog machine.

(defn hot-dog-machine
  []
  (let [in (chan)
        out (chan)]
    (go (<! in)
        (>! out "hot dog"))
    [in out]))

(let [[in out] (hot-dog-machine)]
  (>!! in "pocket lint")
  (<!! out))

;; A correct iteration of our hot dog machine

(defn hot-dog-machine-v2
  [hot-dog-count]
  (let [in (chan)
        out (chan)]
    (go (loop [hc hot-dog-count]
          (if (> hc 0)
            (let [input (<! in)]
              ;; Dispense a hot dog only if I received $3
              (if (= 3 input)
                (do (>! out "hot dog")
                    ;; Continue operating with one less hot dog
                    (recur (dec hc)))
                ;; Otherwise, dispense wilted lettuce
                (do (>! out "wilted lettuce")
                    (recur hc))))
            ;; When no more hot dogs, shut down everything (in detail,
            ;; close all the channels).
            (do (close! in)
                (close! out)))))
    [in out]))

;; Let's give the upgraded hot dog machine a whirl
(let [[in out] (hot-dog-machine-v2 2)]
  ;; First try: more pocket lint
  (>!! in "pocket lint")
  (println (<!! out))

  ;; Second try: a grudging $3
  (>!! in 3)
  (println (<!! out))

  ;; Delish. Another!
  (>!! in 3)
  (println (<!! out))

  ;; Third time's a charm!?
  (>!! in 3)
  (<!! out))

;; An illustrative pipeline of channels connecting the output of one
;; to the input of another.
(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (go (>! c2 (clojure.string/upper-case (<! c1))))
  (go (>! c3 (clojure.string/reverse (<! c2))))
  (go (println (<! c3)))
  (>!! c1 "redrum"))

;; ## alts!!

;; > The `core.async` function `alts!!` lets you use the result of
;; > the **first** successful channel operation among a collection
;; > of operations. We did something similar to this with delays
;; > and futures in "Delays" on page 198. In that example, we
;; > uploaded a set of headshots to a headshot sharing site and
;; > notified the headshot owner when the first photo was uploaded.
;; > Here's how you'd do the same with `alt!!`:

#_(defn upload
    [headshot c]
    (go (Thread/sleep (rand 100))
        (>! c headshot)))

(rand 100)

#_(let [c1 (chan)
        c2 (chan)
        c3 (chan)]
    (upload "serious.jpg" c1)
    (upload "fun.jpg" c2)
    (upload "sassy.jpg" c3)
    (let [[headshot channel] (alts!! [c1 c2 c3])]
      (println "Sending headshot notification for" headshot)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
