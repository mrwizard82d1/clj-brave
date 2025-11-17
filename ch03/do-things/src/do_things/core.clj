(ns do-things.core)

1
"a string"
["a" "vector" "of" "strings"]

(+ 1 2 3)
(str "It was the panda " "in the library " "with a dust buster")

;; `if`

(if true
  "By Zeus's hammer!"
  "By Aquaman's trident!")

(if false
  "By Zeus's hammer!"
  "By Aquaman's trident!")

(if false
  "By Odin's Elbow!")

;; `do`

(if true
  (do (println "Success!")
      "By Zeus's hammer!")
  (do (println "Failure!")
      "By Aquaman's trident!"))

;; `when`

(when true
  (println "Success!")
  "abra cadabra")

;; `nil`, `true`, `false`, Truthiness, Equality and Boolean expressions

(nil? 1)
(nil? nil)

(if "bears eat beets"
  "bears beets Battlestar Galactica")

(if nil
  "This won't be the result because `nil` is falsey."
  "nil is falsey")

;; Clojure's equality operator is `=`

(= 1 1)
(= nil nil)
(= 1 2)
(= 1 1.0)

;; `and` and `or`

(or false nil :large_I_mean_venti :why_cant_I_just_say_large)
(or (= 0 1) (= "yes" "no"))
(or nil)
(or)

(and :free_wifi :hot_coffee)
(and :feelin_super_cool nil false)
(and)

;; Naming values with `def`
(def failed-protagonist-names
  ["Larry Potter" "Doreen the Explorer" "The Incredible Bulk"])
failed-protagonist-names

;; Don't write code like this that changes values
(def severity :mild)
(def error-message "OH GOD! IT'S A DISASTER! WE'RE ")
(if (= severity :mild)
  (def error-message (str error-message "MILDLY INCONVENIENCED!"))
  (def error-message (str error-message "DOOOOOOMED!")))
error-message

;; An alternative accomplishing this result
(defn error-message
  [severity]
  (str "OH GOD! IT'S A DISASTER! WE'RE "
       (if (= severity :mild)
         "MILDLY INCONVENIENCED!"
         "DOOOOOOMED!")))
(error-message :mild)
(error-message :mildew)

;;
;; Data structures
;;

;; Numbers

;; Here's an integer, float, and a ratio
93
1.2
1/5

;; Strings

"Lord Voldemort"
"\"He who must not be named\""
"\"Great cow of Moscow!\" - Hermes Conrad"

;; Clojure only supports string concatenation

(def name "Chewbacca")
(str "\"Uggllglglglglglglglglll\" - " name)

;; Maps

{}
{:first-name "Charlie"
 :last-name "McFishwich"}
{"string-key" +}

{:name {:first "John"
        :middle "Jacob"
        :last "Jingleheimerschmidt"}}

(hash-map :a 1 :b 2)

(get {:a 0 :b 1} :b)
(get {:a 0 :b {:c "ho hum"}} :b)
(get {:a 0 :b 1} :c)
(get {:a 0 :b 1} :c "unicorns?")

(get-in {:a 0 :b {:c "ho hum"}} [:b :c])

({:name "The human Coffeepot"} :name)

;; Keywords

:a
:rumplestiltsken
:34
:_?

(:a {:a 1 :b 2 :c 3})
;; Same as
(get {:a 1 :b 2 :c 3} :a)
;; Accepts a default
(:d {:a 1 :b 2 :c 3} "No gnome knows homes like Noah knows")

;; Vectors

[3 2 1]

(get [3 2 1] 0)

(get ["a" {:name "Pugsley Winterbottom"} "c"] 1)

(vector "creepy" "full" "moon")

(conj [1 2 3] 4)

;; Lists

'(1 2 3 4)
(nth '(:a :b :c) 0)
(nth '(:a :b :c) 2)

(list 1 "two" {3 4})

(conj '(1 2 3) 4)

;; Sets

#{"kurt vonnegut" 20 :icicle}

(hash-set 1 1 2 2)

(conj #{:a :b} :a)

(set [3 3 3 4 4])

;; "Querying" set members using `contains?`, `get`, and a keyword
(contains? #{:a :b} :a)
(contains? #{:a :b} 3)
(contains? #{nil} nil)

(:a #{:a :b})
(get #{:a :b} :a)
(get #{:a :b} :c)
(get #{:a :b} :c :unknown)
