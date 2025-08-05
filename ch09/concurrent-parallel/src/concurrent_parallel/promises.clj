(ns concurrent-parallel.promises)

(println "Learn about `promise` and `deliver`")

;; Create a `promise` and `deliver` a result to it
(def my-promise (promise))

(deliver my-promise (+ 1 2))

@my-promise

;; Find the smoothest yak butter
(def yak-butter-international
  {:store "Yak Butter International"
   :price 90
   :smoothness 90})

(def butter-than-nothing
  {:store "Butter Than Nothing"
   :price 150
   :smoothness 83})

;; This butter meets our requirements
(def baby-got-yak
  {:store "Baby Got Yak"
   :price 94
   :smoothness 99})

;; The API call waits one (1) second before returning a result simulating
;; an actual network call
(defn mock-api-call
  [result]
  (Thread/sleep 1000)
  result)

(defn satisfactory?
  "If the butter meets our criteria, return the butter else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))

;; Time a call that evaluates **all** mock API calls (takes ~3000 ms)
(time (some (comp satisfactory? mock-api-call)
            [yak-butter-international
             butter-than-nothing
             baby-got-yak]))
(println)

;; Time a call that evaluates **all** mock API calls using promise, delay and future
(time
 (let [butter-promise (promise)]
   (doseq [butter [yak-butter-international butter-than-nothing baby-got-yak]]
     (future (if-let [satisfactory-butter (satisfactory? (mock-api-call butter))]
               (deliver butter-promise satisfactory-butter))))
   (println "And the winner is:" @butter-promise)))
(println)

;; But what happens if no yak butter is satisfactory?
;; By default, the promise would **block forever**. To avoid this issue,
;; include a timeout when dereferencing the promise.
(let [p (promise)]
  (deref p 100 "timed out"))
(println)

;; One can register a **callback** with a `promise` (similar to JavaScript)
(let [ferengi-wisdom-promise (promise)]
  (future (println "Here's some Ferengi wisdom:" @ferengi-wisdom-promise))
  (Thread/sleep 100)
  (deliver ferengi-wisdom-promise "Whisper your way to success."))
(println)

(println "End `promise` and `deliver`")
