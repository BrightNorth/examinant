[![Clojars Project](http://clojars.org/brightnorth/examinant/latest-version.svg)](http://clojars.org/brightnorth/examinant)

# Examinant

Examinant is a lightweight library that wraps the
[RemoteWebDriver](https://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/remote/RemoteWebDriver.html)
class from the Selenium Java API so that it can be used to run parallel tests against a remote testing service such as
[SauceLabs](https://saucelabs.com/).  Unlike [clj-webdriver](https://github.com/semperos/clj-webdriver), very little
attempt has been made to wrap the Java methods exposed by the API with Clojure functions.  The remote support in that
library doesn't expose the capabilities required to fully utilise remote testing services, though.

Examinant requires the use of [clojure.test](https://clojure.github.io/clojure/clojure.test-api.html), but in an
extremely uninstrusive way that means it can easily be used with [Midje](https://github.com/marick/Midje)-based
projects.


## Installation

Add the following dependency to your project.clj file:

    [examinant "0.1.0"]


## Usage

Neither clojure.test nor Midje currently supports parallel testing.  As Examinant runs tests in parallel in their own
threads (using Clojure [future](https://clojuredocs.org/clojure.core/future)s), remote tests cannot be split up using
`deftest` or `fact`; they must all be executed as a single test; for that we use clojure.test, but Midje will happily
execute that test for us when we run a `lein midje`.


```clj
(ns example.test
  (:import [org.openqa.selenium By WebElement]
           [org.openqa.selenium.remote RemoteWebDriver])
  (:require [clojure.test :refer [deftest is]]
            [examinant.core :refer [remote-tests wait-until]]))


;; The url of the remote provider (with credentials)
(def url "http://username:access-key@ondemand.saucelabs.com/wd/hub")


;;Our browser specifications (using Sauce Labs platform names), as a vector of maps
(def browser-specs [{:browserName "chrome" :version "38" :platform "Windows 8.1"}
                    {:browserName "safari" :version "8" :platform "OS X 10.10"}
                    {:browserName "android" :version "4.4" :platform "LINUX"
                     :device-orientation :portrait :deviceName "Google Nexus 7 HD Emulator"}
                    {:browserName "iPhone" :version "8.1" :platform "OS X 10.9"
                     :device-orientation :portrait}])


;; Each test is just a function taking the RemoteWebDriver as an argument; note the use of the clojure.test/is macro
(defn google-logo
  "Checks whether the google logo has the correct title"
  [^RemoteWebDriver driver]
  (.get driver "http://www.google.co.uk")
  (let [^WebElement logo-div (.findElementById driver "hplogo")
        title (.getAttribute logo-div "title")]
    (is (= title "Google"))))


;; Add all the tests to a vector
(def tests [google-logo])


;; Run the tests in parallel against the remote provider using Examinant
(deftest remote-google-tests
  (remote-tests url browser-specs tests))
```

Examinant will output any failures in the usual clojure.test format, and will count and aggregate them too; the output
may be a bit annoying to read because parallel tests may interleave their logging, but Examinant will include the
capabilities that were in use in each test alongside the output.


## Limitations & Improvements

* Only tested with Sauce Labs; probably works with [BrowserStack](http://www.browserstack.com/) too.
* Tests may dump all their output in an interleaved fashion; that's the price we pay for parallel test execution.
* The `remote-tests` function should probably be refactored into something that can be used in a clojure.test
`use-fixtures` or a Midje `with-state-changes` block, for more idiomatic test expression.


## License

Copyright © 2014 Bright North Limited

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
