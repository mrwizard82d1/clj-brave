(ns concurrent-parallel.futures)

(println "\nStarting `concurrent-parallel.core`")

;; An example of a future
(future (Thread/sleep 4000)
        (println "I'll print after 4 seconds."))
(println "I'll print immediately.")
(println)

;; Dereferencing a future
(let [result (future (println "this prints once")
                     (+ 1 1))]
  (println "deref:" (deref result))
  (println "@:    " @result))
(println)

;; A future that takes time to resolve
(let [result (future (Thread/sleep 3000)
                     (+ 1 1))]
  (println "The result is: " @result)
  (println "It will be at least 3 seconds before I print"))
(println)

;; Short-circuiting a future result that takes "too long"
(deref (future (Thread/sleep 1000) 10 5))
(println)


;; Is a future realized?
(realized? (future (Thread/sleep 1000)))
(println)

(let [f (future)]
  @f
  (realized? f))
(println)

(println "Finished `concurrent-parallel.core`")
