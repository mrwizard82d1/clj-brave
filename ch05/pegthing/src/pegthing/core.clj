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

;; To model the board, we use a map with numerical keys corresponding
;; to each board position and with values containing information about
;; that board position's connections.
;;
;; Here is a concrete instance of that data structure for a 5-row
;; board in which each hole contains a peg.
;; {
;;  {:pegged true, :connections {6 3, 4 2}},
;;  {:pegged true, :connections {9 5, 7 4}},
;;  {:pegged true, :connections {10 6, 8 5}},
;;  {:pegged true, :connections {13 8, 11 7, 6 5, 1 2}},
;;  {:pegged true, :connections {14 9, 12 8}},
;;  {:pegged true, :connections {15 10, 13 9, 4 5, 1 3}},
;;  {:pegged true, :connections {9 8, 2 4}},
;;  {:pegged true, :connections {10 9, 3 5}},
;;  {:pegged true, :connections {7 8, 2 5}},
;;  {:pegged true, :connections {8 9, 3 6}},
;;  {:pegged true, :connections {13 12, 4 7}},
;;  {:pegged true, :connections {14 13, 5 8}},
;;  {:pegged true, :connections {15 14, 11 12, 6 9, 4 8}},
;;  {:pegged true, :connections {12 13, 5 9}},
;;  {:pegged true, :connections {13 14, 6, 10}}
;;  :rows 5}
;;
;; Remember that users will use a *alphabetic character* to represent
;; each "hole" in the board, but, internally, we represent each
;; position **numerically**. This representation allows us to "take
;; advantage of some mathematical properties of the board layout when
;; validating and making moves."
;;
;; In this map, the `:connections` keys are followed by another map
;; In the `:connections` map, each key represents a **legal destination**
;; and each value represents **the position to be jumped over**.
;;
;; The first few expressions in this section deal with
;; **triangular numbers**
;; (https://en.wikipedia.org/wiki/Triangular_number).

(defn tri*
  "Generates a lazy sequence of triangular numbers"
  ([] (tri* 0 1))
  ([sum n]
   (let [new-sum (+ sum n)]
     (cons new-sum (lazy-seq (tri* new-sum (inc n)))))))

;; Create this lazy sequence and bind

(take 5 (tri*))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
