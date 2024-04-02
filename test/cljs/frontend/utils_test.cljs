(ns cljs.frontend.utils-test
  (:require [frontend.utils :as utils]
            [clojure.test :refer :all]))

(deftest test-format-date
  (testing "test format date"
    (is (= (utils/format-date (js/Date. "2021-01-01")) "2021-01-01"))
    (is (= (utils/format-date (js/Date. "2021-01-10")) "2021-01-10"))
    (is (= (utils/format-date (js/Date. "2021-10-01")) "2021-10-01"))
    (is (= (utils/format-date (js/Date. "2021-10-10")) "2021-10-10"))))