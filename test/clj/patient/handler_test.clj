(ns clj.patient.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [patient.handler :refer [parse-order-params
                                     validate-order-by]])) 

(deftest test-parse-order-params
  (testing "parse-order-params function"
    (is (= (parse-order-params "column1_asc,column2_desc") []))
    (is (= (parse-order-params "full-name_asc") [[:full-name :asc]]))
    (is (= (parse-order-params "full-name_asc,test_desc") [[:full-name :asc]]))
    (is (= (parse-order-params "") []))))

(deftest test-valid-order-by
  (testing "validate-order-by function"
    (is (validate-order-by [:id :asc]))
    (is (validate-order-by [:id :asc :full-name :desc :address :asc :dob :desc :gender :asc :oms :desc :created-at :asc]))
    (is (not (validate-order-by [:test :asc])))))