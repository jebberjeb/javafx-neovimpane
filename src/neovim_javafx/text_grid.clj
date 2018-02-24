(ns neovim-javafx.text-grid
  "A matrix representation of text which attempts to match Neovim's UI API."
  (:require [clojure.string :as string]))

(defn width
  [grid]
  (count (first grid)))

(defn height
  [grid]
  (count grid))

(defn new-grid
  "Returns a text grid of the size specified."
  [width height]
  (vec (repeat height (vec (repeat width {:char " "
                                          :highlight {}})))))

(defn put*
  [grid row col char-map]
  (assoc-in grid [row col] char-map))

(defn put
  "Starting at `row`, `col`, add `chars` to the `row`. `highlight` is a map
  of attributes about the char."
  ([grid row col char-maps]
   (try
     (let [[new-grid _] (reduce (fn [[grid' col'] char-map]
                                  [(put* grid' row col' char-map)
                                   (inc col')])
                                [grid col]
                                char-maps)]
       new-grid)
     (catch IndexOutOfBoundsException e
       (println
         (format "PUT IGNORING OUT OF BOUNDS REQUEST ROW: %s COL: %s CHARS: %s"
                 row col chars))
       grid)))
  ([grid row col chars highlight]
   (put grid row col (map (fn [char]
                            {:char char :highlight highlight})
                          chars))))

(defn clear-rest-of-line
  "Replaces the remainder of line `row` with spaces, starting from `col`."
  [grid row col]
  (let [row-width (width grid)]
    (put grid row col (repeat (- row-width col) " ") {})))

(defn scroll-row
  "Moves a section of a row, and replace it with whitespace. A negative
  `distance` is down, positive is up."
  [grid row left right distance]
  (try
    (let [section (subvec (nth grid row) left (inc right))]
      (-> grid
          (put row left (repeat (count section) " ") {})
          ;; TODO here's where things get tricky. `secion` will actually be
          ;; a vector of maps. So, we need a way to transfer the section.
          (put (- row distance) left section)))
    (catch IndexOutOfBoundsException e
      (println
        (format "SCROLL-ROW IGNORING OUT OF BOUNDS REQUEST ROW: %s DISTANCE: %s"
                row distance))
      grid)))

(defn scroll
  [grid top bottom left right distance]
  (reduce (fn [grid' row]
            (scroll-row grid' row left right distance))
          grid
          (if (pos? distance)
            (range (+ top distance) (inc bottom))
            (range (+ bottom distance) (dec top) -1))))
