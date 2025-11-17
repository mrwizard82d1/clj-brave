(ns writing-macros.core)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;; Anotomy of a macro
;;


;; `when` implemented in terms of `if` and `do`
(macroexpand
  '(when boolean-expression
    expression-1
    expression-2
    expression-3))

;; The `infix` macro rearranges a simple arithmetic infix expression
;; to reouired prefix.
(defmacro infix
  "Use this macro when you pine for the notation of your childhood"
  [infixed]
  (list (second infixed)
        (first infixed)
        (last infixed)))

(infix (1 + 1))

(macroexpand '(infix (1 + 1)))

;; You can also use argument destructuring in macros just like in functions
(defmacro infix-2
  [[operand1 op operand2]]
  (list op operand1 operand2))

(infix-2 (1 + 1))

;; You can also create multi-arity macros. An example and the built-in
;; `and` and `or` "functions".
(defmacro and
  "Evaluates exprs one at a time, from left to right. If a form
  returns logical false (nil or false), and returns that value and
  doesn't evaluate any of the other expressions, otherwise it returns
  the value of the last expr. `(and)` returns true"
  {:added "1.0"}
  ([] true)
  ([x] x)
  ([x & next]
   `(let [and# ~x]
      (if and# (and ~@next) and#))))

(and)
(and 3)
(and 3 5)

;; Building Lists for Evaluation
;;


;; Distinguishing Symbols and Values


;; (defmacro my-print-whoopsie
;;   [expression]
;;   (list let [result expression]
;;         (list println result)
;;         result))

(defmacro my-print
  [expression]
  (list 'let ['result expression]
        (list 'println 'result)
        'result))

(my-print (+ 1 2))

;; Simple quoting

;; A simple function call with no quoting
(+ 1 2)

;; Adding `quote` at the beginning returns an **unevaluated** data structure
(quote (+ 1 2))

;; Normally, evaluating the `+` symbol yields the plus **function**
+

;; However, quoting the symbol just yields the plus symbol itself
(quote +)

;; Evaluating an unbound symbol raises an exception
;; sweating-to-the-oldies

;; But quoting the symbol returns a symbol **even if** that symbol
;; has a value
(quote sweating-to-the-oldies)

;; The single quote character is a **reader macro** for `(uote X)`
'(+ 1 2)
'dr-jekyll-and-richard-simmons

;; An example of quoting at work is the definition of the
;; `when` macro
(defmacro when
  "Evaluates test. zif logical true, evalues body in an implicity do."
  {:added "1.0"}
  [test & body]
  (list 'if test (cons 'do body)))

;; An example of what that returned list might look like
(macroexpand '(when (the-cows-come :home)
                (call me :pappy)
                (slap me :silly)))

;; Here's another example of a built-in macro, `unless`
(defmacro unless
  "Inverted `if`"
  [test & branches]
  (conj (reverse branches) test 'if))

(macroexpand '(unless (done-been slapped? me)
                (slap me :silly)
                (say "I reckon that'll learn me")))

;; Syntax Quoting
;;

;; Simply quoting symbols **does not** include the namespace
'+

;; Specifically include the namespace and it, too, will be included
'clojure.core/+

;; Syntax quoting **always** includes the namespace
`+

;; Both quoting and syntax quating recursively quote all elements,
;; but syntax quoting, again, results in fully qualified elements
'(+ 1 2)

`(+ 1 2)

;; Remember the reason for syntax quating: to help avoid namespace collisions.

;; Additionally, syntax quoting suppors **unquoting** using the tilde (~).
;; Remember that unquoting applies to the next **form** in the expression and
;; that the result of unquoting a form is that Clojure then **evaluates* that
;; form **before** substitution.
`(+ 1 ~(inc 1))

;; Without unuoting, syntax quating returns the **unevaluated** form with
;; fully qualified names.
`(+ 1 (inc 1))

;; Syntax quoting and unquoting allow one to create lists more clearly
;; and concisely.
(list '+ 1 (inc 1))

`(+ 1 ~(inc 1))

;; Using Syntax Quoting in a Macro
;;

;; The original version of a macro with **no** syntax quoting
(defmacro code-critic
  "Phrases courtesy Hermes Conrad from Futurama"
  [bad good]
  (list 'do
        (list 'println
              "Great squid of Madrid, this is bad code:"
              (list 'quote bad))
        (list 'println
              "Sweet gorilla of Manilla, this is good code:"
              (list 'quote good))))

(code-critic (1 + 1) (+ 1 1))

;; This "style" works but is neither sleek nor concise.
;; But with syntax quoting...
(defmacro code-critic
  "Phrases courtesy Hermes Conrad from Futurama"
  [bad good]
  `(do (println "Great squid of Madrid, this is bad code:"
                (quote ~bad))
       (println "Sweet gorilla of Manilla, this is good code:"
                (quote ~good))))

;; And behaves the same
(code-critic (1 + 1) (+ 1 1))

;; Refactoring a Macro and Unquote SplicingA
;;

;; We can still clean up the implementation of the `code-critic` macro.
;; Start by extracting the calls to `println` into a function.
(defn criticize-code
  [criticism code]
  `(println ~criticism (quote ~code)))

(defmacro code-critic
  [bad good]
  `(do ~(criticize-code "Cursed bacteria of Liberia, this is bad code:" bad)
       ~(criticize-code "Sweet sacred boa of Western and Eastern Samoa, this is good code"
                        good)))

(code-critic (1 + 1) (+ 1 1))

;; There's still room for improvement. Because the code has almost identical
;; calls to `criticize-code`, we can use `map` to apply this function to a
;; collection of values.
(defmacro code-critic
  [bad good]
  `(do ~(map #(apply criticize-code %)
             [["Great squid of Madrid, this is bad code:" bad]
              ["Sweet gorilla of Manilla, this is good code:" good]])))

;; But when we use our "improved" macro...,
;; ...we encounter a `NullPointerException`.
;; (code-critic (1 + 1) (+ 1 1))

;; The "problem": `map` returns a list of the results of the two `println`
;; calls. But the **returned value** of `println` is **`null`*8. Consqueontly,
;; we get a list consisting of two `null` values which is, in turn,
;; evaluated, resulting in a reference to `null` (the first element of the
;; list).

;; Unquote splicing was invented to solve this precise situaton.

;; Simply unquoting **does not** produce what we want
`(+ ~(list 1 2 3))

;; But unqote splicing splices in our unquoted result
`(+ ~@(list 1 2 3))

;; We can use unquote splicing to solve our definition of the
;; `code-critic` macro.
(defmacro code-critic
  [bad good]
  `(do ~@(map #(apply criticize-code %)
              [["Sweet lion of Zion, this is bad code:" bad]
               ["Great cow of Moscov, this is good code:" good]])))

(code-critic (1 + 1) (+ 1 1))

;; Things to watch out for
;;

;; Variable Capture
(def message "Good job!")
(defmacro with-mischief
  [& stuff-to-do]
  (concat (list 'let ['message "Oh, big deal!"])
          stuff-to-do))

;; I expect this to contain "Good job!" but...
;; I have "captured" `message` internally.
(with-mischief
  (println "Here's how I feel about that thing you did:"
           message))

;; Using syntax quoting causes an execption.
(def message "Good job!")
(defmacro with-mischief
  [& stuff-to-do]
  `(let [message "Oh, big deal!"]
     ~@stuff-to-do))

;; (with-mischief
;;   (println "Here's how I feel about that thing you did: " message))

;; One way to avoid this issue is to use `gensym`. Invoking `gensym` produces
;; unique symbols on each successive call.
(gensym)

(gensym)

;; Another option is to pass a prefix for the generated symbol
;; s.
(gensym 'message)

(gensym 'message)

;; We can use `gensym` to make `with-mischief` less mischievous.
(defmacro without-mischief
  [& stuff-to-do]
  (let [macro-message (gensym 'message)]
    `(let [~macro-message "Oh, big deal!"]
       ~@stuff-to-do
       (println "I still need to say: " ~macro-message))))

(without-mischief
  (println "Here's how I feel about that thing you did: " message))

;; Using `gensym` in this manner is so common that we can actually use
;; an `auto-gensym`.
`(blarg# blarg#)

`(let [name# "Larry Potter"] name#)

;; Double Evaluation
;;

;; Another "gotcha'" occurs when a form passed to a macro as an argument
;; is evaluated more than one times.
(defmacro report
  [to-try]
  `(if ~to-try
     (println (quote ~to-try) "was successful:" ~to-try)
     (println (quote ~to-try) "was not successful:" ~to-try)))

;; Notice that `Thread/sleep` sleeps for the specified milliseconds
;; One might expect this code to wait 1 seconds, but, because of the
;; macro, it sleesp for **2 seconds**.
(report (do (Thread/sleep 1000) (+ 1 1)))
