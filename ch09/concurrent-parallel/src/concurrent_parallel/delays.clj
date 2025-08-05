(ns concurrent-parallel.delays)

(println "Starting `delays`")

;; A delay: so **nothing** happens when evaluated
;; Returns a `reference` that can be dereferenced to "force" the delay to
;; "evaluate".
(def jackson-5-delay
  (delay (let [message "Just call out my name and I'll be there"]
           (println "First deref:" message)
           message)))
(println)

;; Using `force` behaves identically as dereferencing the returned value
;; but communicates more clearly to readers your intent.
(force jackson-5-delay)
(println)

;; Subsequent dereferences return the same (and single) result.
@jackson-5-delay
(println)
(force jackson-5-delay)
(println)

;; "Upload" a set of headshots; notify owner when first is available.
(def gimli-headshots ["serious.jpg", "fun.jpg", "playful.jpg"])

(defn email-user
  [email-address]
  (println "Sending headshot notification to " email-address))

(defn upload-document
  [headshot]
  true)

(let [notify (delay (email-user "and-my-axe@gmail.com"))]
  (doseq [headshot gimli-headshots]
    (future (upload-document headshot)
            (force notify))))

(println "Ending `delays`")
