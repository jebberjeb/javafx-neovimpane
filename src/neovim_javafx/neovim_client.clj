(ns neovim-javafx.neovim-client
  (:require
    [clojure.string :as string]
    [neovim-client.1.api :as api]
    [neovim-client.1.api-ext :as api-ext]
    [neovim-client.nvim :as client.nvim]
    [neovim-javafx.text-grid :as text-grid]))

(defn get-nvim-embedded
  "Launch and return a connection to an embedded Neovim instance."
  ([]
   (get-nvim-embedded 1))
  ([api-version]
   (let [neovim (.exec (Runtime/getRuntime) "nvim --embed")
         in (.getInputStream neovim)
         out (.getOutputStream neovim)]
     (client.nvim/new* api-version in out false))))

(defn get-nvim-socket
  "Connect to a running Neovim instance, for debugging purposes."
  ([]
   (get-nvim-socket 1 "127.0.0.1" 7777))
  ([api-version host port]
   (client.nvim/new api-version host port)))

;; state is {:cursor [row col] :grid text-grid :highlight {...}}
(defmulti handle-event (fn [state [event-type & _]] event-type))

(defmethod handle-event :default
  [state msg]
  (println "--- IGNORED EVENT ---")
  (println msg)
  (println "---------------------")
  state)

;; (highlight_set ({}) ({reverse true, foreground 240, bold true}))
(defmethod handle-event "highlight_set"
  [state [_ & attr-maps]]
  ;; We're only interested in the last item in the message, and then we need
  ;; to dig it out of the list it's in.
  (assoc state :highlight (first (last attr-maps))))

;; (cursor_goto (6 0))
(defmethod handle-event "cursor_goto"
  [state [_ position]]
  (assoc state :cursor position))

;; TODO add event text
(defmethod handle-event "resize"
  [state [_ [width height]]]
  ;; TODO this only supports the initial resize, but I'm not sure if actual
  ;; resize events will redraw the whole grid, or not.
  (assoc state :grid (text-grid/new-grid width height)))

;; (put ( ) (1) (2))
(defmethod handle-event "put"
  [{:keys [cursor highlight] :as state} [_ & chars]]
  (let [[row col] cursor
        ;; Each char is wrapped in a list, per the event.
        chars' (map first chars)]
    (-> state
        (update :grid text-grid/put (int row) (int col) chars' highlight)
        (assoc :cursor [row (+ col (count chars'))]))))

(defmethod handle-event "eol_clear"
  [state _]
  (let [[row col] (:cursor state)]
    (update state :grid text-grid/clear-rest-of-line (int row) (int col))))

(defmethod handle-event "clear"
  [{:keys [grid] :as state} _]
  (let [[first-row & _] grid
        height (text-grid/height grid)
        width (text-grid/width grid)]
    {:grid (text-grid/new-grid width height)
     :cursor [0 0]}))

(defmethod handle-event "set_scroll_region"
  [state [_ scroll-region]]
  (assoc state :scroll-region scroll-region))

(defmethod handle-event "scroll"
  [{:keys [scroll-region] :as state} [_ [distance]]]
  (try
    (let [[top bottom left right] scroll-region]
      (update state :grid text-grid/scroll (int top) (int bottom) (int left)
              (int right) (int distance)))
    (catch Throwable t
      (println t)
      state)))

;; (2 redraw ((highlight_set ({})) (cursor_goto (14 58)) (put (k)) (cursor_goto (4 0))))
(defn handle-redraw
  [state [_ _ payload :as redraw-msg]]
  (prn (take 10 redraw-msg))
  (reduce handle-event
          state
          payload))

(defn get-nvim
  []
  (let [nvim (get-nvim-embedded)]
    nvim))

(defn attach-ui
  "Connect the UI to Neovim.

  `redraw-fn` is a function of state, which should be a "
  [neovim-conn height width redraw-fn]
  (let [state (atom {:cursor [0 0]
                     :highlight {}})]
    (client.nvim/register-method!
      neovim-conn
      "redraw"
      (fn [redraw-event]
        (swap! state handle-redraw redraw-event)
        (redraw-fn @state)))
    ;; TODO not sure what, if anything these flags are doing. We could probably
    ;; add an attribute to state indicating whether or not we should _hide_
    ;; the bottom two lines (status bar and command line, for me), but I'm not
    ;; sure if some of this is config specific.
    (api/ui-attach neovim-conn height width {"rgb" false
                                             "ext_tabline" true
                                             "ext_cmdline" true})))

(def get-current-buffer-text-async api-ext/get-current-buffer-text-async)
(def get-current-buffer-text api-ext/get-current-buffer-text)
(def set-current-buffer-text api-ext/set-current-buffer-text)

(defn type-key-async
  "Input a key to neovim."
  [nvim key f]
  (api/input-async
    nvim key f))

(defn cursor-position-in-string
  "Returns the location of the cursor in the grid string, as a number, rather
  than a row col tuple."
  [{:keys [grid cursor]}]
  (let [[row col] cursor]
    ;; `inc` to account for the \n on each row.
    (+ (* row (inc (text-grid/width grid))) col)))


