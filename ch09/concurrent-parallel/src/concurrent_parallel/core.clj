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

;; ### Delays

;; Delays allow one to define a task without having to execute it or
;; to require the result imediately.

(def jackson-5-delay
  (delay (let [message "Just call my name and I'll be there"]
           (println "First deref: " message)
           message)))

;; At first glance, one migt expect the message to print when the
;; value is defined. In actuality, the `delay` delays the evaluation
;; of the `let` form.

;; One can evaluate the delay and get its result by:
;;
;; - Dereferencing the value
;; - Using `force`
;;
;; Using `force` behaves identically to `deref` but it communicates
;; more clearly that you are causing a task to start instead of
;; waiting for a task to finish.

(force jackson-5-delay)

;; Like futures, a delay is
;;
;; - Run only once
;; - Its value is cached

@jackson-5-delay

;; One way to use a delay is to fire off a statement the **first time**
;; one future of a group of futures finishes. For example, pretend you
;; have an app that uploads a set of headshots to a headshot-sharting
;; site and notifies the owner as soon as the firsnt one is up.

 (def gimli-headshots ["serious.jpg" "fun.jpg" "playful.jpg"])

(defn email-user
  [email-address]
  (println "Sending headshot notification to" email-address))

(defn upload-document
  "Needs to be implemented"
  [headshot]
  true)

(let [notify (delay (email-user "and-my-axe@gmail.com"))]
  (doseq [headshot gimli-headshots]
    (future (upload-document headshot)
            (force notify))))

;; This example uses `let` to bind `notify` to a delay. The body of the
;; delay, `(email-user "and-my-axe@gmail.com")` is **not** evaluated when
;; the delay is created. Instead, the `delay` bedy is evaluated the
;; first time one of the created futures created by the `doseq form
;; evaluates `(force notify)` three times, the `delay` is only evaluated
;; **once**.

;; This technique can help protect you from the mutual exclusion
;; "Concurrency Goblin." In this example, the delay guards the email
;; server resource. Because the body of a delay is guaranteed to be
;; evaluated **only once**,
;;
;; On the other hand, no thread can ever use the delay to send an email
;; again. This constraint might be too drastic for most situations, but
;; it works perfectly in cases like this.
