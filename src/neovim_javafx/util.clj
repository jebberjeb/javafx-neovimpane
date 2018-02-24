(ns neovim-javafx.util
  (:import
    (javafx.beans.value ChangeListener)
    (javafx.event EventHandler)
    (javafx.scene.text Text Font))
  (:require
    [neovim-client.1.api :as api]
    [neovim-javafx.neovim-client :as neovim-client]))

(defn later [f] (javafx.application.Platform/runLater f))

(defn get-bounds
  []
  (.getLayoutBounds
    (doto
      (Text. "M")
      (.setFont (Font/font "Monospaced" 14)))))

(def char-height
  (memoize
    (fn [] (.getHeight (get-bounds)))))

(def char-width
  (memoize
    (fn [] (.getWidth (get-bounds)))))

(defn get-rows
  [component]
  (int (/ (.getHeight component) (char-height))))

(defn get-cols
  [component]
  (int (/ (.getWidth component) (char-width))))

(defn key-event-handler
  "Returns a key event handler.

  `f` a function of the event target."
  [neovim-conn f]
  (reify EventHandler
    (handle [_ e]
      (let [key (-> e .getText)]
        (neovim-client/type-key-async
          neovim-conn
          key
          (fn [_] (f (.getTarget e))))))))

(defn size-changed-listener
  [neovim-conn pane]
  (reify ChangeListener
    (changed [_ _ old-value new-value]
      ;; TODO where should this actually live? Should this be in neovim-client?
      (api/ui-try-resize-async
        neovim-conn (get-cols pane) (get-rows pane) (fn [_] nil)))))
