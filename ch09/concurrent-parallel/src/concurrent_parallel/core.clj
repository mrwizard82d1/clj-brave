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

;; ### Promises

;; _Promises_ express the idea that I expect a result **without yet**
;;
;; - Defining the task that should produce the result
;; - Determining when that task should run
;;
;; One creates a promisg using the `promise` function. One delivers a
;; result to a promise using `deliver`. Finally, you obtain the result
;; by dereferencing.

(def my-promise (promise))

(deliver my-promise (+ 1 2))

@my-promise

;; Remember, if one dereferences a `promise` **before** delivering a
;; value, the code will **block** until a result is available.
;;
;; You can only deliver a result to a promise **once**.

;; One use for a promise is to find the first (in time) satisfactory
;; element in a collection of data generated at different times.

;; Premium yak butter

(def yak-butter-international
  {:store "Yak Butter International"
   :price 90
   :smoothness 90})

(def butter-than-nothing
  {:store "Butter Than Nothing"
   :price 150
   :smoothness 83})

;; Here is the butter that meets our requirements.
(def baby-got-yak
  {:store "Baby Got Yak"
   :price 94
   :smoothness 99})

(defn mock-api-call
  [result]
  ;; Simulate a long-running function call
  (Thread/sleep 1000)
  result)

(defn satisfactory?
  "If the butter meets our criterian, return the butter, else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))

;; Making a call with `some` demonstrates how to apply the i
;; ``satisfactory?` predicate and return the first truthy result. It
;; should take about second.`

(time (some (comp satisfactory? mock-api-call)
            [yak-butter-international butter-than-nothing baby-got-yak]))

;; One can use a promise and futures to check each option on a different
;; thread. With multiple cores, this implention should reduce the time it
;; takes to about one second.

(time
 ;; We begin by creating a `promise` to deliver a value
 (let [butter-promise (promise)]
   (doseq [butter [yak-butter-international
                   butter-than-nothing
                   baby-got-yak]]
     ;; Create three futures to calculate the promise ruselt
     (future (if-let [satisfactory-butter (satisfactory? (mock-api-call butter))]
               (deliver butter-promise satisfactory-butter))))
   ;; Dereference the promise causing the program to wait unit a
   ;; satisfactory butter is found.
   (println "And the winner is:" @butter-promise)))

;; This code demonstrates a way to protect yourself from the "reference cell
;; Concurrency Goblin" because promises can only be written to once (first
;; writer wins). Consequently, these constructs prevent the kind of
;; inconsistent state that arises from nondeterministic reads and writes.

;; You might be wondering what happens if **no** yak butter option is
;; satisfactory. This circumstance would result in the dereference
;; blocking the current thread **forever**. To avoid that, you can
;; include a timeout.

;; The following expression waits for 100 ms to resolve the promise
;; Since no result will be delivered, this code returns the text,
;; "timed out".
(let [p (promise)]
  (deref p 100 "timed out"))

;; Finally, one can use promises to register callbacks achieving the
;; same functionality available in JavaScript. Here's how:

(let [ferengi-wisdom-promise (promise)]
  ;; The future begins executing immediately; however, dereferencing
  ;; the primes **blockes** until it is available.
  (future (println "Here's some Ferengi wisdom:" @ferengi-wisdom-promise))
  ;; Simualate a delay by sleeping
  (Thread/sleep 100)
  ;; Deliver the promise
  (deliver ferengi-wisdom-promise "Whisper your way to success."))
