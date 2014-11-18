(ns hn-follow.core.poller)

(defprotocol IPoller
  "Poller interface for polling an API request or other observable function"
  (start [_] "Start the agent poller")
  (stop [_] "Stop the agent poller")
  (errors [_] "Agent errors from poller")
  (register [_ cb] "Register a callback with the poller"))

(defn poller
  "Run a poller loop for some agent using some observable function `f` and some delay time in seconds."
  [f delay]
  (let [cond-var (atom true)
        state (atom {})
        callbacks (atom [])
        status (agent {})]
    ;; Create new poller object. Reify to closure the let-block values
    (reify IPoller
      ;; Start the poller
      (start [_]
        (send-off status
         (fn [_] (while @cond-var
                  (let [resp (f)]
                    ;; Alert registered callbacks of messages
                    (when-not (or (nil? resp) (= resp @state))
                      (reset! state resp)
                      (doseq [cb @callbacks]
                        (cb @state))))
                  ;; Wait for delay duration                
                  (Thread/sleep (* delay 1000))))))

      ;; Stop the poller from running
      (stop [_]
        (reset! cond-var false)
        (not @cond-var))

      (errors [_]
        (agent-errors status))
      
      ;; Register a callback
      (register [_ cb] (swap! callbacks conj cb)))))
