# Examinant

Examinant is a lightweight library that wraps the
[RemoteWebDriver](https://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/remote/RemoteWebDriver.html)
from the Selenium Java API so that it can be used to run parallel tests against a remote testing service such as
[SauceLabs](https://saucelabs.com/).  Unlike [clj-webdriver](https://github.com/semperos/clj-webdriver), very little
attempt has been made to wrap the Java methods exposed by the API with Clojure functions.

Examinant requires the use of [clojure.test](https://clojure.github.io/clojure/clojure.test-api.html), but in an
extremely uninstrusive way that means it can easily be used with [Midje](https://github.com/marick/Midje)-based
projects.


## Usage

Neither clojure.test nor Midje currently supports parallel testing.  As tests are run in parallel in their own threads
(using Clojure [future](https://clojuredocs.org/clojure.core/future)s), remote tests cannot be split up using `deftest`
or `fact`; they must all be executed as a single test; for that we use clojure.test, but Midje will happily execute that
test for us when we run a `lein midje`:

```clj
(ns example.test
  (:import [org.openqa.selenium By WebElement]
           [org.openqa.selenium.remote RemoteWebDriver])
  (:require [clojure.test :refer [deftest is]]
            [examinant.core :refer [remote-tests wait-until]]))
```

First though, we define the URL we will use to access the remote testing service, which includes our credentials for
that service:

```clj
(def url "http://username:access-key@ondemand.saucelabs.com/wd/hub")
```


Next, we define our browser specifications (which are used to create Selenium
[DesiredCapabilities](https://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/remote/DesiredCapabilities.html)
objects).  In these examples we've used the Sauce Labs format:

```clj
;; Browser specifications are just an array of maps, defined by the remote provider
(def browser-specs [{:browserName "chrome" :version "38" :platform "Windows 8.1"}
                    {:browserName "safari" :version "8" :platform "OS X 10.10"}
                    {:browserName "android" :version "4.4" :platform "LINUX" :device-orientation :portrait
                     :deviceName "Google Nexus 7 HD Emulator"}
                    {:browserName "iPhone" :version "8.1" :platform "OS X 10.9" :device-orientation :portrait}])
```

Now we need some actual tests.  Each test is just a function that takes a `RemoteWebDriver` as an argument (note that
we don't actually need to include the type hinting, but it's shown here to make it clear what's happening, and it helps
in the IDE too):

```clj
(defn google-logo
  "Checks whether the google logo has the correct title"
  [^RemoteWebDriver driver]
  (.get driver "http://www.google.co.uk")
  (let [^WebElement logo-div (.findElementById driver "hplogo")
        title (.getAttribute logo-div "title")]
    (is (= title "Google"))))

(def tests [google-logo])
```

We've used the clojure.test `is` macro in this test, so that it reports failures in the usual way.  In this example we
only have one test function, but we can run as many as we like by adding more into the `tests` vector.

Finally we hook in to examinant to parallelise and run our tests:

```clj


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
