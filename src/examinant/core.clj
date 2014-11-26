(ns examinant.core
  (:import [org.openqa.selenium.remote DesiredCapabilities RemoteWebDriver])
  (:require [clojure.java.io :refer [as-url]]
            [clojure.test :refer [testing]]
            [clojure.tools.logging :refer [debug info error]]))


(defn create-driver
  "Creates a new RemoteWebDriver connected to the specified url, with the specified capabilities"
  [url browser-spec]
  (let [set-capability (fn [desired-capabilities browser-spec]
                         (.setCapability desired-capabilities (name (first browser-spec)) (second browser-spec))
                         desired-capabilities)
        desired-capabilities (reduce set-capability (DesiredCapabilities.) browser-spec)]
    (debug "Creating RemoteWebDriver with capabilities:" desired-capabilities)
    (RemoteWebDriver. (as-url url) desired-capabilities)))


(defn remote-tests
  "Runs each of the specified tests once for each browser-spec, using the remote server at url"
  [url browser-specs tests]
  (try
    (let [result-futures (for [test tests
                               browser-spec browser-specs]
                           (let [driver (create-driver url browser-spec)]
                             (future (testing (str "Capabilities: " browser-spec)
                                       (test driver)
                                       (.quit driver)))))]
      (doall (map #(try
                    (deref %)
                    (catch Throwable t
                      (error t "Failed to complete remote test:"))) result-futures)))
    (catch Throwable t
      (error t "Error in examinant"))
    (finally
      (shutdown-agents))))
