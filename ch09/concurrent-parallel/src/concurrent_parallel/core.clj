(ns concurrent-parallel.core)

;; ## Futures, delays, and promisses

;; ### Futures

;; A `future` defines a task and places it on anothr thread to run
;; **without** requiring its result immediately.

(future (Thread/sleep 4000)
        (println "I'll print after 4 seconds"))
(println "I'll print immediately")

;; One can use futures to run tasks on a separate thread and then
;; forget about them; however, the `future` function actuall returs
;; a **reference** to the value to be calculated. One can use this
;; value to request the result. If the result is available, the
;; requesting thread simply continues on using that value, However,
;; if the future value is **not** available, the requesting thread
;; will **wait** for the value to become available.

;; Remember, a future's body executes exactly once and the result
;; of the future is cached.

(let [result (future (println "This prints once")
                     (+ 1 1))]
  (println "deref: " (deref result))
  (println "@: " @result))

;; Dereferencing a future will block of thi future has not finished
;; running:
(let [result (future (Thread/sleep 3000)
                     (+ 1 1))]
  (println "The result is: " @result)
  (println "It will be at least 3 seconds before I print"))

;; Sometimes, you want to limit how long you will wait for a future.
;;
;; This code will wait 10 **mulliseconds** for the future to return
;; a value. If no value is available within that amount of time, the
;; result of the expression is 5.
(deref (future (Thread/sleep 1000) 0) 10 5)

;; Finally, one can query a future to determine if a value is
;; available using `realized?`.
(realized? (future (Thread/sleep 1000)))

(let [f (future)]
  @f
  (realized? f))

(let [f (future)]
  @f)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
