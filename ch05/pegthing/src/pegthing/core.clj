;; Code for a game called "Peg Thing" (see Cracker Barrel)

;; The program has four major tasks
;;
;; - Creating a new board
;; - Returning a board with the result of the players move
;; - Reprenting a board textually
;; - Handling user interaction
;;
;; Although my instinct is to place these four tasks into four
;; different namespaces, the author chose to put all this code in a
;; single namespace. I will follow his lead. (Note that we, the readers,
;; do not learn about namespaces until the next chapter of the book.)
;;
;; The authors raises an additional point. The code has a basic
;; *architecture* consisting of two layers.
;;
;; - User interaction (top-layer containing **all** the code
;;   side-effects.)
;; - Bottom layer (domain?) containing code to
;;   - Create a new board
;;   - Make moves
;;   - Create a textual representation
;;
;; Note that all dependencies point from the user interaction layer to
;; the bottom (domain) layer.
;;
;; Additionally, the author reminds the reader that, in the interest of
;; pedagogy, he has tried to break down tasks into small functions so
;; that each function "...does one tiny, understandable task."

(ns pegthing.core
  (require [clojure.set :as set])
  (:gen-class))

;; Allows functions to refer to these names **before** they have
;; been defined.
(declare successful-move prompt-move game-over query-rows)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
