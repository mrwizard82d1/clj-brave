(ns do-things.core)

;; # Do Things: A Clojure Crash Course

;; ## Basics

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

;; ## Data structures

;; ### Numbers

;; Here's an integer, float, and a ratio
93
1.2
1/5

;; ### Strings

"Lord Voldemort"
"\"He who must not be named\""
"\"Great cow of Moscow!\" - Hermes Conrad"

;; Clojure only supports string concatenation

(def name "Chewbacca")
(str "\"Uggllglglglglglglglglll\" - " name)

;; ### Maps

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

;; ### Keywords

:a
:rumplestiltsken
:34
:_?

(:a {:a 1 :b 2 :c 3})
;; Same as
(get {:a 1 :b 2 :c 3} :a)
;; Accepts a default
(:d {:a 1 :b 2 :c 3} "No gnome knows homes like Noah knows")

;; ### Vectors

[3 2 1]

(get [3 2 1] 0)

(get ["a" {:name "Pugsley Winterbottom"} "c"] 1)

(vector "creepy" "full" "moon")

(conj [1 2 3] 4)

;; ### Lists

'(1 2 3 4)
(nth '(:a :b :c) 0)
(nth '(:a :b :c) 2)

(list 1 "two" {3 4})

(conj '(1 2 3) 4)

;; ### Sets

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

;; ## Functions

;; ### Calling Functions

(+ 1 2 3 4)
(* 1 2 3 4)
(first [1 2 3 4])

;; A function is just another value
(or + -)

((or + -) 1 2 3)

((and (= 1 1) +) 1 2 3)

((first [+ 0]) 1 2 3)

;; The following `lists` are **invalid** function calls because the
;; first item in the `list` is **not** a function.
;; (1 2 3 4)
;; ("test" 1 2 3)

;; Clojure supports _higher order functions_; that is, functions that
;; accept or return (or both) functions as arguments.
(inc 1.1)
(map int [0 1 2 3])

;; Remember that Clojure evaluations function arguments recursively
;; before passing these arguments to other functions.
(+ (inc 199) (/ 100 (- 7 2)))

;; ### Function Calls, Macro Calls, and Special Forms

;; THe main feature that makes "special" forms special is that, unlike
;; function calls, **they do not always evaluate all of their operands.**
;; For example, the `if` special form evaluates either the true expression
;; or the false expression **but not both**.
(+ (if true 2 nil) 3)

;; Similarly, macros evaluate their operands **differently** than
;; function calls.

;; ### Defining functions

;; An example
(defn too-enthusiastic
  "Return a cheer that might be a bit too enthusiastic"
  [name]
  (str "OH. MY. GOD! " name " YOU ARE MOST LIKELY THE BEST "
       "MAN SLASH WOMAN EVER. I LOVE YOU AND WE SHOULD RUN AWAY SOMEWHERE"))

(too-enthusiastic "Zelda")

;; Let's dive into the docstring, parameters, and function body

;; #### The Docstring

;; The _docstring_ describes and documents its containing function.
;;
;; One can view the docstring in the REPL by invoking `(doc fn-name)`;
;; for example, `(doc map)`. The docstring can also be used by other
;; documentation generation tools.

;; #### Parameters and Arity

(defn no-params
  []
  "I take no parameters!")
(no-params)

(defn one-param
  [x]
  (str "I take one parameter: " x))
(one-param 3)

(defn two-params
  [x y]
  (str "Two parameters! That's nothing! Pah! I will smoosh tem "
       "together to spite you: " x y))
(two-params 3 4)

;; Functions also support _arity overloading_.
(defn multi-arity
  ;; 3-arity arguments and body
  ([first-arg second-arg third-arg]
   (println "I have three" first-arg second-arg third-arg))
  ;; 2-arity arguments and body
  ([first-arg second-arg]
   (println "Now I have two" first-arg second-arg))
  ;; 1-arity arguments and body
  ([first-arg]
   (println "I can handle one" first-arg)))

(multi-arity 1 2 3)
(multi-arity 1 2)
(multi-arity 1)

;; Arity overloading is one way to provide default values for arguments.
(defn x-chop
  "Describe the kind of chop you're inflicting on someone"
  ([name chop-type]
   (str "I " chop-type " chop " name "! Take that!"))
  ([name]
   (x-chop name "karate")))

(x-chop "Kanye West" "pork")
(x-chop "Kanye East")

;; You can also make each arity do something completely different
;; (but don't?)
(defn weird-arity
  ([]
   "Destiny dressed you this morning, my friend, and now fear is
    trying to pull off your pants. If you give up, if you give in,
    you're gonna' end up naked with Fear just standing there laughing
    at your dangling unmentionables! - The Tick")
  ([number]
   (inc number)))

(weird-arity 3)
(weird-arity)

;; Additionally, Clojure function definitions supports a
;; _rest parameter_; that is, an argument that will consume any and all
;; additional function arguments.
(defn codger-communication
  [whippersnapper]
  (str "Get off my lawn, " whippersnapper "!!!"))

(defn codger
  [& whippersnappers]
  (map codger-communication whippersnappers))

(codger "Billy" "Anne-Marie" "The Incredible Bulk")

;; Remember that the rest parameter **must** come last.
(defn favorite-things
  [name & things]
  (str "Hi, " name ", here are my favorite things: "
       (clojure.string/join ", " things)))
(favorite-things "Doreen" "gum" "shoes" "kara-te")

;; Finally, Clojure hase a more sophisticated way of defining parameters,
;; called _destructuring_.

;; #### Destructuring

;; Return the first element of a collection
(defn my-first
  [[first-thing]] ; Notice that `first-thing` is **within** a vector
  first-thing)
(my-first ["oven" "bike" "war-axe"])

;; When destructing a vector or list argument, you can name as many
;; elements as you want and also use rest parameters.
(defn chooser
  [[first-choice second-choice & unimportant-choices]]
  (println (str "Your first choice is: " first-choice))
  (println (str "Your second choice is: " second-choice))
  (println (str "We're ignoring the rest of your choices. "
                "Here they are in case you need to cry over them: "
                (clojure.string/join ", " unimportant-choices))))

(chooser ["Marmalade" "Handsome Jack" "Pigpen" "Aquaman"])

;; You can also destructure maps by providing a as a paremeter.
(defn announce-treasure-location
  [{lat :lat lng :lng}]
  (println (str "Treasure lat: " lat))
  (println (str "Treasure lng: " lng)))
(announce-treasure-location {:lat 28.22 :lng 81.33})

;; We often just want to break keywords out of a map. Clojure provides
;; a shorter syntax to accomplish that goal.
(defn announce-treasure-location
  [{:keys [lat lng]}]
  (println (str "Treasure lat: " lat))
  (println (str "Treasure lng: " lng)))
(announce-treasure-location {:lat 28.22 :lng 81.33})

;; In addition, you can retain access to the original map by using
;; the `:as` keyword
(defn receive-treasure-location
  [{:keys [lat lng] :as treasure-location}]
  (let [steer-ship! (fn [location]
                      (println (str "Go that-a-way! " location)))]
   (println (str "Treasure lat: " lat))
   (println (str "Treasure lng: " lng))
   (steer-ship! treasure-location)))
(receive-treasure-location {:lat 28.22 :lng 81.33})

;; #### Function body

(defn illustrative-function
  []
  (+ 1 304) ;; does nothing (no side-effects)
  30 ;; Similar
  "joe") ;; returnd value
(illustrative-function)

;; Here's another function body that uses an `if` expression
(defn number-comment
  [x]
  (if (> x 6)
    "Oh my gosh! What a big number you have, my dear!"
    "That number's OK, I guess."))
(number-comment 6)
(number-comment 7)

;; ### Anonymous functions

(map (fn [name] (str "Hi, " name))
     ["Darth Vader" "Mr. Magoo"])

((fn [x] (* x 3)) 8)

(def my-special-multiplier (fn [x] (* x 3)))
(my-special-multiplier 12)

;; A more compact way to create anonymous functions.
(#(* % 3) 8)

(map #(str "Hi, " %)
     ["Darth Vader" "Mr. Magoo"])

;; Multiple arguments
(#(str %1 " and " %2) "cornbread" "butter beans")

;; Use `%&` to handle a rest parameter.
(#(identity %&) 1 "blarg" :yip)

;; ### Returning functions

;; You've already seen that functions can **return other functions***.
;; The returned functions are _closures_. This term means that they
;; can access all variables that were in scope when the function was
;; created.
(defn inc-maker
  "Create a custom incrementor"
  [inc-by]
  #(+ % inc-by))

(def inc3 (inc-maker 3))

(inc3 7)
