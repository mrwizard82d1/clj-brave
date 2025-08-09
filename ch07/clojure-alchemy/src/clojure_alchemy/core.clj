(ns clojure-alchemy.core)

;; This expression simply returns a string
(read-string "(1 + 1)")

;; This eexpression raises an error
(eval (read-string "(1 + 1)"))

;; This expression rearranges what was read
(let [infix (read-string "(1 + 1)")]
  (list (second infix)
        (first infix)
        (last infix)))

;; And then we evaluation that rearranged expression
(eval
 (let [infix (read-string "(1 + 1)")]
   (list (second infix)
         (first infix)
         (last infix))))

;; Evaluating a defined macro
(defmacro ignore-last-operand
  [function-call]
  (butlast function-call)) ;; Does not include the last argument
(ignore-last-operand (+ 1 2 10))

;; This will **not** print anything
(ignore-last-operand (+ 1 2 (println "look at me!!!")))

;; `macroexpand` returns the results of evaluating a macro
(macroexpand '(ignore-last-operand (+ 1 2 10)))
(macroexpand '(ignore-last-operand (+ 1 2 (println "look at me!!!"))))

;; A simple infix macro
(defmacro infix
  [infixed]
  (list (second infixed)
        (first infixed)
        (last infixed)))
(infix (1 + 2))

;; Macros enable "syntactic expansion"
;; The -> "function" is actually a mcro

;; One must "read" the function body "inside out" or "left-to-right".
(def read-resource
  "Read a resource into a string"
  [path]
  (read-string (slurp (clojure.java.io/resource path))))

;; The "thread first operator" rearranges this expression so it reads more
;; naturally: top-to-bottom and left-to-right.
(defn read-resources
  "Read a resource into a string"
  [path]
  (-> path
      clojure.java.io/resource
      slurp
      read-string))
