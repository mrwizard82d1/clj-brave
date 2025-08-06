(ns atoms-refs-vars.atoms)

(println "Starting with `atoms`")

;; Name an `atom` with a specific value
(def fred (atom {:cuddle-hunger-level 0
                 :percent-deteriorated 0}))

@fred
(println)

;; Eventually logging the state of a zombie with `println`
(let [zombie-state @fred]
  (if (>= (:percent-deteriorated zombie-state) 50)
    (future (println (:percent-deteriorated zombie-state)))))
(println)


;; (Atomically) changing the state of an atom with swap!
(swap! fred
       (fn [current-state]
         (merge-with + current-state {:cuddle-hunger-level 1})))
@fred
(println)

;; Atomically changing **both** `:cuddle-hunger-level` and `:percent-deteriorated`
(swap! fred
       (fn [current-state]
         (merge-with + current-state {:cuddle-hunger-level 1
                                      :percent-deteriorated 1})))

@fred
(println)

;; Passing arguments to the `atom` update function
(defn increase-cuddle-hunger-level
  [zombie-state increase-by]
  (merge-with + zombie-state {:cuddle-hunger-level increase-by}))

;; One can apply this function **outside** of a `swap!` function.
(increase-cuddle-hunger-level @fred 10)
;;
;; Only the **returned value*8 differs from `@fred~.`
@fred

;; However, ano can use this function as an argument to `swap!`.
(swap! fred increase-cuddle-hunger-level 10)

;; By using this function as an argument to `swap``, one **changes**
;; the value referred to by the atom.`
@fred
(println)

;; One could express the whole thing using `update-in`. Here's an
;; example of usage (outside an `atom`).
(update-in {:a {:b 3}} [:a :b] inc)
(update-in {:a {:b 3}} [:a :b] + 10)
(println)

;; Here's how to use `update-in` to change the state of `@fred`
(swap! fred update-in [:cuddle-hunger-level] + 10)
@fred

(println)

;; Atoms allow one to **retain** past state if so delined.
(let [num (atom 1)
      s1 @num] ;; state at time "1"
  (swap! num inc)
  (println "State 1:" s1)
  (println "Current state:" @num))

(println)

(println "Ending with `atoms`")
