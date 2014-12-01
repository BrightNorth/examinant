(defproject brightnorth/examinant "0.1.3-SNAPSHOT"

  :description "A library for running parallel remote WebDriver tests on remote infrastructure"

  :url "http://github.com/BrightNorth/examinant"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-release "1.1.3"]]

  :lein-release {:deploy-via :clojars}

  :dependencies [[clj-webdriver "0.6.1"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.seleniumhq.selenium/selenium-java "2.44.0"]])
